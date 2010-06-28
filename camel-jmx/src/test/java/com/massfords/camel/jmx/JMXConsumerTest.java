package com.massfords.camel.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.massfords.camel.jmx.beans.ISimpleMXBean;
import com.massfords.camel.jmx.beans.SimpleBean;

public class JMXConsumerTest {
	
	private static final String DOMAIN = "TestDomain";
	private static final String NAME = "name";
	private DefaultCamelContext mContext;
	private MockEndpoint mMockEndpoint;
	private long mStartTime;
	private int mDuration = 10;
	private TimeUnit mTimeUnit = TimeUnit.SECONDS;

	@Before
	public void setUp() throws Exception {
		
		SimpleBean simpleBean = new SimpleBean();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss");
		Date date = sdf.parse("2010-07-01T10:30:15");
		simpleBean.setTimestamp(date.getTime());
		
		registerBean(simpleBean, makeObjectName("simpleBean"));
		
		mContext = new DefaultCamelContext();
		mMockEndpoint = (MockEndpoint) mContext.getEndpoint("mock:sink");
		mContext.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				String uri = new StringBuilder("jmx:platform?objectDomain=").append(DOMAIN).append("&objectName=simpleBean").toString();
				from(uri).to(mMockEndpoint);
			}
		});
		mContext.start();
	}
	
	@After
	public void tearDown() throws Exception {
		if (!mContext.isStopped())
			mContext.stop();
		unregisterBean(makeObjectName("simpleBean"));
	}

	@Test
	public void testAttributeChange() throws Exception {
		
		ISimpleMXBean simpleBean = getMXBean(makeObjectName("simpleBean"));
		simpleBean.setStringValue("foo");
		
		waitForMessages(1);
		
		XmlFixture.assertXMLIgnorePrefix("failed to match", 
				XmlFixture.toDoc(new File("src/test/resources/consumer-test/attributeChange-0.xml")), 
				XmlFixture.toDoc(getBody(0, String.class)));
		
		// change the value again, should match with newValue=bar and oldValue=foo
		simpleBean.setStringValue("bar");
		waitForMessages(2);
		
		XmlFixture.assertXMLIgnorePrefix("failed to match", 
				XmlFixture.toDoc(new File("src/test/resources/consumer-test/attributeChange-1.xml")), 
				XmlFixture.toDoc(getBody(1, String.class)));
	}
	
	@Test
	public void testNotification() throws Exception {
		ISimpleMXBean simpleBean = getMXBean(makeObjectName("simpleBean"));
		simpleBean.touch();
		
		waitForMessages(1);

		XmlFixture.assertXMLIgnorePrefix("failed to match", 
				XmlFixture.toDoc(new File("src/test/resources/consumer-test/touched.xml")), 
				XmlFixture.toDoc(getBody(0, String.class)));
	}

	private void waitForMessages(int expectedCount) throws InterruptedException {
		while(mMockEndpoint.getReceivedCounter() < expectedCount && notTimedOut()) {
			Thread.sleep(100);
		}
		assertEquals("incorrect number of messages received", expectedCount, mMockEndpoint.getReceivedCounter());
	}
	
	private boolean notTimedOut() {
		if (mStartTime == 0) {
			mStartTime = System.currentTimeMillis();
			return true;
		}
		boolean timedOut = System.currentTimeMillis() - mStartTime < mTimeUnit.toMillis(mDuration);
		if(timedOut)
			fail("timed out waiting for operation to complete");
		return true;
	}

	private void registerBean(SimpleBean simpleBean, ObjectName aObjectName)
			throws Exception {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		mbeanServer.registerMBean(simpleBean, aObjectName);
	}
	
	private void unregisterBean(ObjectName aObjectName) throws Exception {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		mbeanServer.unregisterMBean(aObjectName);
	}
	
	private ISimpleMXBean getMXBean(ObjectName aObjectName) throws Exception {
		ISimpleMXBean simpleBean = JMX.newMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
				aObjectName, ISimpleMXBean.class);
		return simpleBean;
	}

	private ObjectName makeObjectName(String aName) throws Exception {
		ObjectName objectName = new ObjectName(DOMAIN, NAME, aName);
		return objectName;
	}

	private String getBody(int index, Class<String> type) {
		List<Exchange> exchanges = mMockEndpoint.getReceivedExchanges();
		String body = exchanges.get(index).getIn().getBody(type);
		assertNotNull(body);
		return body;
	}
}
