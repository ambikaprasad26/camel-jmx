package com.massfords.camel.jmx;


import java.io.File;


import org.junit.Test;

import com.massfords.camel.jmx.beans.ISimpleMXBean;

public class JMXConsumerTest extends SimpleBeanFixture {
	
	@Test
	public void testAttributeChange() throws Exception {
		
		ISimpleMXBean simpleBean = getSimpleMXBean();
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
		ISimpleMXBean simpleBean = getSimpleMXBean();
		simpleBean.touch();
		
		waitForMessages(1);

		XmlFixture.assertXMLIgnorePrefix("failed to match", 
				XmlFixture.toDoc(new File("src/test/resources/consumer-test/touched.xml")), 
				XmlFixture.toDoc(getBody(0, String.class)));
	}
}
