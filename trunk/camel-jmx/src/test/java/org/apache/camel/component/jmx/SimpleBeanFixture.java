package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.apache.camel.component.jmx.beans.SimpleBean;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.junit.After;
import org.junit.Before;


/**
 * MBean that is registered for the unit tests. The fixture will register a bean
 * and provide access to the mxbean so tests can invoke methods on the mxbean
 * to trigger notifications.
 * 
 * @author markford
 */
public class SimpleBeanFixture {

    /** domain to use for the mbean */
    protected static final String DOMAIN = "TestDomain";
    /** key for the object name */
    protected static final String NAME = "name";
    /** camel context to stand up for the test */
    private DefaultCamelContext mContext = new DefaultCamelContext();
    /** registry to store referenced beans (i.e. objectProperties or NotificationFilter) */
    private SimpleRegistry mRegistry = new SimpleRegistry();
    /** destination for the simple route created. */
    private MockEndpoint mMockEndpoint;

    @Before
    public void setUp() throws Exception {
    	initBean();
    	initRegistry();
    	initContext();
    	startContext();
    }

    protected void startContext() throws Exception {
        mContext.start();
    }

    @After
    public void tearDown() throws Exception {
    	if (!mContext.isStopped())
    		mContext.stop();
    	unregisterBean(makeObjectName("simpleBean"));
    }

    protected void waitForMessages() throws InterruptedException {
        waitForMessages(mMockEndpoint);
    }

    protected void waitForMessages(MockEndpoint aMockEndpoint) throws InterruptedException  {
        mMockEndpoint.await(10, TimeUnit.SECONDS);
        assertEquals("Expected number of messages didn't arrive before timeout", aMockEndpoint.getExpectedCount(), aMockEndpoint.getReceivedCounter());
    }

    /**
     * Registers the bean on the platform mbean server
     * @param aBean
     * @param aObjectName
     * @throws NotCompliantMBeanException 
     * @throws MBeanRegistrationException 
     * @throws InstanceAlreadyExistsException 
     */
    protected void registerBean(Object aBean, ObjectName aObjectName) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException  {
    	MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    	mbeanServer.registerMBean(aBean, aObjectName);
    }

    /**
     * Unregisters the bean
     * @param aObjectName
     * @throws InstanceNotFoundException 
     * @throws MBeanRegistrationException 
     */
    protected void unregisterBean(ObjectName aObjectName) throws MBeanRegistrationException, InstanceNotFoundException   {
    	MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
    	mbeanServer.unregisterMBean(aObjectName);
    }

    /**
     * Gets the mxbean for our remote object using the specified name
     * @param aObjectName
     */
    protected ISimpleMXBean getMXBean(ObjectName aObjectName)  {
    	ISimpleMXBean simpleBean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
    			aObjectName, ISimpleMXBean.class);
    	return simpleBean;
    }

    /**
     * Gets the mxbean for our remote object using the default name "simpleBean"
     * @throws MalformedObjectNameException
     */
    protected ISimpleMXBean getSimpleMXBean() throws MalformedObjectNameException  {
        return getMXBean(makeObjectName("simpleBean"));
    }

    /**
     * Makes an ObjectName for the given domain using our domain and the name attribute.
     * @param aName
     * @throws MalformedObjectNameException
     */
    protected ObjectName makeObjectName(String aName) throws MalformedObjectNameException {
    	ObjectName objectName = new ObjectName(DOMAIN, NAME, aName);
    	return objectName;
    }

    /**
     * Gets the body of the received message at the specified index
     * @param <T>
     * @param aIndex
     * @param aType
     */
    protected <T> T getBody(int aIndex, Class<T> aType) {
    	Message in = getMessage(aIndex);
        T body = in.getBody(aType);
    	assertNotNull(body);
    	return body;
    }

    /**
     * Gets the received message at the specified index 
     * @param aIndex
     */
    protected Message getMessage(int aIndex) {
        Exchange exchange = getExchange(aIndex);
        Message in = exchange.getIn();
        return in;
    }

    /**
     * Gets the received exchange at the specified index
     * @param aIndex
     */
    protected Exchange getExchange(int aIndex) {
        List<Exchange> exchanges = mMockEndpoint.getReceivedExchanges();
    	Exchange exchange = exchanges.get(aIndex);
        return exchange;
    }

    /**
     * Creates the bean and registers it within the mbean server.
     * Note that we're using a fixed timestamp here to simplify the assertions in the tests
     * @throws Exception
     */
    protected void initBean() throws Exception  {
        SimpleBean simpleBean = new SimpleBean();
    
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");
        Date date = sdf.parse("2010-07-01T10:30:15");
        simpleBean.setTimestamp(date.getTime());
        
        registerBean(simpleBean, makeObjectName("simpleBean"));
    }

    /**
     * Initializes the camel context by creating a simple route from our mbean
     * to the mock endpoint.
     * @throws Exception
     */
    protected void initContext() throws Exception {
        mMockEndpoint = (MockEndpoint) mContext.getEndpoint("mock:sink");
        mMockEndpoint.setExpectedMessageCount(1);
        mContext.setRegistry(getRegistry());
        mContext.addRoutes(new RouteBuilder() {
    
            @Override
            public void configure() throws Exception {
                from(buildFromURI().toString()).to(mMockEndpoint);
            }
        });
    }
    
    /**
     * Override this to control the properties that make up the endpoint
     */
    protected JMXUriBuilder buildFromURI() {
        JMXUriBuilder uri = new JMXUriBuilder().withObjectDomain(DOMAIN)
                            .withObjectName("simpleBean");
        return uri;
    }

    /**
     * Override this to put stuff into the registry so it's available to be 
     * referenced. (i.e. NotificationFilter or Hashtable<String,String> for ObjectProperties
     */
    protected void initRegistry() {
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

    /**
     * Assert that we've received the message and resets the mock endpoint
     * @param aExpectedFile
     * @throws Exception
     */
    protected void assertMessageReceived(File aExpectedFile) throws Exception {
        XmlFixture.assertXMLIgnorePrefix("failed to match", 
                XmlFixture.toDoc(aExpectedFile), 
                XmlFixture.toDoc(getBody(0, String.class)));
        resetMockEndpoint();
    }

    /**
     * Resets the mock endpoint so we can run another test. This will clear out any
     * previously received messages.
     */
    protected void resetMockEndpoint() {
        getMockEndpoint().reset();
    }
}