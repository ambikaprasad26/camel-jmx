package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.management.monitor.CounterMonitor;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Registers another bean that monitors an attribute on the simple bean.
 * When the attribute changes, an event is fired that we'll assert on.
 * 
 * @author markford
 */
public class JMXMonitorTest extends SimpleBeanFixture {
    
    /** endpoint for the monitor bean */
    MockEndpoint mMonitorSink;

    @Test
    public void counter() throws Exception {

        assertEquals(0, mMonitorSink.getReceivedCounter());
        
        mMonitorSink.setExpectedMessageCount(1);
        getMockEndpoint().setExpectedMessageCount(2);

        ISimpleMXBean simpleBean = getSimpleMXBean();
        
        // we should get an event after the sequence reaches 2
        
        // changing the attrib increments the sequence
        simpleBean.setStringValue("foo");
        // changing the attrib increments the sequence again
        simpleBean.setStringValue("bar");
        
        // we should have gotten 2 attribute change messages
        waitForMessages();
        
        // and 1 monitor message
        assertTrue("messages didn't arrive within timeout", mMonitorSink.await(10, TimeUnit.SECONDS));
        
        Document monitorBody = XmlFixture.toDoc(mMonitorSink.getReceivedExchanges().get(0).getIn().getBody(String.class));
        // strip the timestamp and dateTime fields so we can assert w/o having them trip us up
        monitorBody = XmlFixture.stripTimestamp(monitorBody);
        XmlFixture.assertXMLIgnorePrefix("failed to match", 
                XmlFixture.toDoc(new File("src/test/resources/consumer-test/monitorNotification.xml")), 
                monitorBody);
    }
    
    @Override
    protected void initBean() throws Exception {
        // add the simple bean
        super.initBean();
        // add our monitor bean
        CounterMonitor monitor = new CounterMonitor();
        monitor.addObservedObject(makeObjectName("simpleBean"));
        monitor.setObservedAttribute("MonitorNumber");
        monitor.setNotify(true);
        monitor.setInitThreshold(1);
        monitor.setGranularityPeriod(500);
        registerBean(monitor, makeObjectName("counter"));
        monitor.start();
    }


    @Override
    protected void initContext() throws Exception {
        // add the route for the simple bean
        super.initContext();
        // add the route for the monitor bean
        mMonitorSink = (MockEndpoint) getContext().getEndpoint("mock:monitor");
        getContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(buildFromURI().withObjectName("counter").toString()).to(mMonitorSink);
            }
        });
    }
}
