package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jmx.JMXUri;
import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.apache.camel.component.jmx.beans.SimpleBean;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.After;
import org.junit.Before;


public class SimpleBeanFixture {

    private static final String DOMAIN = "TestDomain";
    private static final String NAME = "name";
    private DefaultCamelContext mContext = new DefaultCamelContext();
    private SimpleRegistry mRegistry = new SimpleRegistry();
    private MockEndpoint mMockEndpoint;
    private long mStartTime;
    private int mDuration = 10;
    private TimeUnit mTimeUnit = TimeUnit.SECONDS;

    public SimpleBeanFixture() {
        super();
    }

    @Before
    public void setUp() throws Exception {
    	
    	initBean();
    	initRegistry();
    	initContext();
    }

    @After
    public void tearDown() throws Exception {
    	if (!mContext.isStopped())
    		mContext.stop();
    	unregisterBean(makeObjectName("simpleBean"));
    }

    protected void waitForMessages(int expectedCount) throws InterruptedException {
    	while(mMockEndpoint.getReceivedCounter() < expectedCount && notTimedOut()) {
    		Thread.sleep(100);
    	}
    	assertEquals("incorrect number of messages received", expectedCount, mMockEndpoint.getReceivedCounter());
    }

    protected boolean notTimedOut() {
    	if (mStartTime == 0) {
    		mStartTime = System.currentTimeMillis();
    		return true;
    	}
    	boolean timedOut = System.currentTimeMillis() - mStartTime < mTimeUnit.toMillis(mDuration);
    	if(timedOut)
    		fail("timed out waiting for operation to complete");
    	return true;
    }

    protected void registerBean(SimpleBean simpleBean, ObjectName aObjectName) throws Exception {
    	MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    	mbeanServer.registerMBean(simpleBean, aObjectName);
    }

    protected void unregisterBean(ObjectName aObjectName) throws Exception {
    	MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    	mbeanServer.unregisterMBean(aObjectName);
    }

    protected ISimpleMXBean getMXBean(ObjectName aObjectName) throws Exception {
    	ISimpleMXBean simpleBean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
    			aObjectName, ISimpleMXBean.class);
    	return simpleBean;
    }

    protected ISimpleMXBean getSimpleMXBean() throws Exception {
        return getMXBean(makeObjectName("simpleBean"));
    }

    protected ObjectName makeObjectName(String aName) throws Exception {
    	ObjectName objectName = new ObjectName(DOMAIN, NAME, aName);
    	return objectName;
    }

    protected <T> T getBody(int index, Class<T> type) {
    	Message in = getMessage(index);
        T body = in.getBody(type);
    	assertNotNull(body);
    	return body;
    }

    protected Message getMessage(int index) {
        Exchange exchange = getExchange(index);
        Message in = exchange.getIn();
        return in;
    }

    protected Exchange getExchange(int index) {
        List<Exchange> exchanges = mMockEndpoint.getReceivedExchanges();
    	Exchange exchange = exchanges.get(index);
        return exchange;
    }

    protected void initBean() throws ParseException, Exception {
        SimpleBean simpleBean = new SimpleBean();
    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");
        Date date = sdf.parse("2010-07-01T10:30:15");
        simpleBean.setTimestamp(date.getTime());
        
        registerBean(simpleBean, makeObjectName("simpleBean"));
    }

    protected void initContext() throws Exception {
        mMockEndpoint = (MockEndpoint) mContext.getEndpoint("mock:sink");
        mContext.setRegistry(getRegistry());
        mContext.addRoutes(new RouteBuilder() {
    
            @Override
            public void configure() throws Exception {
                from(buildFromURI().toString()).to(mMockEndpoint);
            }
        });
        mContext.start();
    }
    
    protected JMXUri buildFromURI() {
        JMXUri uri = new JMXUri().withObjectDomain(DOMAIN)
                            .withObjectName("simpleBean");
        return uri;
    }

    protected void initRegistry() throws Exception {
    }

    protected DefaultCamelContext getContext() {
        return mContext;
    }

    protected void setContext(DefaultCamelContext aContext) {
        mContext = aContext;
    }

    protected SimpleRegistry getRegistry() {
        return mRegistry;
    }

    protected void setRegistry(SimpleRegistry aRegistry) {
        mRegistry = aRegistry;
    }

    protected MockEndpoint getMockEndpoint() {
        return mMockEndpoint;
    }

    protected void setMockEndpoint(MockEndpoint aMockEndpoint) {
        mMockEndpoint = aMockEndpoint;
    }

    protected long getStartTime() {
        return mStartTime;
    }

    protected void setStartTime(long aStartTime) {
        mStartTime = aStartTime;
    }

    protected int getDuration() {
        return mDuration;
    }

    protected void setDuration(int aDuration) {
        mDuration = aDuration;
    }

    protected TimeUnit getTimeUnit() {
        return mTimeUnit;
    }

    protected void setTimeUnit(TimeUnit aTimeUnit) {
        mTimeUnit = aTimeUnit;
    }

    protected void assertMessageReceived(String expectedPath) throws Exception {
        XmlFixture.assertXMLIgnorePrefix("failed to match", 
                XmlFixture.toDoc(new File(expectedPath)), 
                XmlFixture.toDoc(getBody(0, String.class)));
        resetMockEndpoint();
    }

    protected void resetMockEndpoint() {
        getMockEndpoint().reset();
    }
}