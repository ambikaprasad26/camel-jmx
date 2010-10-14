package com.massfords.camel.jmx;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JMXConsumer extends DefaultConsumer implements NotificationListener {
    private static final Log LOG = LogFactory.getLog(JMXConsumer.class);

    private MBeanServerConnection mServerConnection;
    private NotificationXmlFormatter mFormatter;

    public JMXConsumer(JMXEndpoint aEndpoint, Processor aProcessor) throws Exception {
        super(aEndpoint, aProcessor);
        mFormatter = new NotificationXmlFormatter();
    }

	@Override
	protected void doStart() throws Exception {
		super.doStart();
		
		JMXEndpoint ep = (JMXEndpoint) getEndpoint();
		
		// connect to the mbean server
		if (ep.isPlatformServer()) {
			setServerConnection(ManagementFactory.getPlatformMBeanServer());			
		} else {
            JMXServiceURL url = new JMXServiceURL(ep.getServerURL());
            String[] creds = {ep.getUser(), ep.getPassword()};
            Map map = Collections.singletonMap(JMXConnector.CREDENTIALS, creds);
            JMXConnector connector = JMXConnectorFactory.connect(url, map);
            setServerConnection(connector.getMBeanServerConnection());
		}
		// subscribe
		NotificationFilter nf = ep.getNotificationFilter();
		
		ObjectName objectName = ep.getJMXObjectName();
		
		getServerConnection().addNotificationListener(objectName, this, nf, ep.getHandback());
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		JMXEndpoint ep = (JMXEndpoint) getEndpoint();
		getServerConnection().removeNotificationListener(ep.getJMXObjectName(), this);
	}

	protected MBeanServerConnection getServerConnection() {
		return mServerConnection;
	}

	protected void setServerConnection(MBeanServerConnection aServerConnection) {
		mServerConnection = aServerConnection;
	}

	@Override
	public void handleNotification(Notification aNotification, Object aHandback) {
        JMXEndpoint ep = (JMXEndpoint) getEndpoint();
        Exchange exchange = getEndpoint().createExchange(ExchangePattern.InOnly);
        Message message = exchange.getIn();
        message.setHeader("jmx.handback", aHandback);
        try {
            if (ep.isXML()) {
                message.setBody(mFormatter.format(aNotification));
            } else {
                message.setBody(aNotification);
            }
            getProcessor().process(exchange);
        } catch (NotificationFormatException e) {
            LOG.error("Failed to marshal notification", e);
        } catch (Exception e) {
            LOG.error("Failed to process notification", e);
        }
    }
}
