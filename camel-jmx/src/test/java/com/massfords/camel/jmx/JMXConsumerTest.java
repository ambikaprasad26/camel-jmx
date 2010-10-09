package com.massfords.camel.jmx;


import org.junit.Test;

import com.massfords.camel.jmx.beans.ISimpleMXBean;

public class JMXConsumerTest extends SimpleBeanFixture {
	
	@Test
	public void testAttributeChange() throws Exception {
		
		ISimpleMXBean simpleBean = getSimpleMXBean();

		simpleBean.setStringValue("foo");
		waitForMessages(1);
		assertMessageReceived("src/test/resources/consumer-test/attributeChange-0.xml");

		simpleBean.setStringValue("bar");
		waitForMessages(1);
        assertMessageReceived("src/test/resources/consumer-test/attributeChange-1.xml");
		
		// set the string to null
		simpleBean.setStringValue(null);
        waitForMessages(1);
        assertMessageReceived("src/test/resources/consumer-test/attributeChange-2.xml");
	}

    @Test
	public void testNotification() throws Exception {
		ISimpleMXBean simpleBean = getSimpleMXBean();
		simpleBean.touch();
		
		waitForMessages(1);

		String expected = "src/test/resources/consumer-test/touched.xml";
		assertMessageReceived(expected);
	}
	
	@Test
	public void testUserData() throws Exception {
        ISimpleMXBean simpleBean = getSimpleMXBean();
        simpleBean.userData("myUserData");
        
        waitForMessages(1);

        String expected = "src/test/resources/consumer-test/userdata.xml";
        assertMessageReceived(expected);
	}
	
	@Test
	public void testJMXConnection() throws Exception {
        ISimpleMXBean simpleBean = getSimpleMXBean();
        simpleBean.triggerConnectionNotification();
        
        waitForMessages(1);

        String expected = "src/test/resources/consumer-test/jmxConnectionNotification.xml";
        assertMessageReceived(expected);
	}
}
