package org.apache.camel.component.jmx;

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

/**
 * Consumer will add itself as a NotificationListener on the object 
 * specified by the objectName param.
 * 
 * @author markford
 */
public class JMXConsumer extends DefaultConsumer implements NotificationListener {
    private static final Log LOG = LogFactory.getLog(JMXConsumer.class);

    /** connection to the mbean server (local or remote) */
    private MBeanServerConnection mServerConnection;
    /** used to format Notification objects as xml */
    private NotificationXmlFormatter mFormatter;

    /**
     * Ctor  
     * @param aEndpoint
     * @param aProcessor
     */
    public JMXConsumer(JMXEndpoint aEndpoint, Processor aProcessor) {
        super(aEndpoint, aProcessor);
        mFormatter = new NotificationXmlFormatter();
    }

	/** 
	 * {@inheritDoc}
	 * 
	 * Initializes the mbean server connection and starts listening for
	 * Notification events from the object.
	 * 
	 * @see org.apache.camel.impl.DefaultConsumer#doStart()
	 */
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

	/**
	 * {@inheritDoc}
	 * 
	 * Removes the notification listener
	 * 
	 * @see org.apache.camel.impl.DefaultConsumer#doStop()
	 */
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

	/**
	 * {@inheritDoc}
	 * 
	 * Processes the Notification received. The handback will be set as 
	 * the header "jmx.handback" while the Notification will be set as
	 * the body. 
	 * 
	 * If the format is set to "xml" then the Notification will be converted
	 * to XML first using {@link NotificationXmlFormatter}
	 * 
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
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
