package org.apache.camel.component.jmx;


import java.io.File;

import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests that trigger notification events on our simple bean without
 * requiring any special setup.
 * 
 * @author markford
 * 
 */
public class JMXConsumerTest extends SimpleBeanFixture {
    
    ISimpleMXBean simpleBean;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        simpleBean = getSimpleMXBean();
    }
    
	@Test
	public void attributeChange() throws Exception {
		
		getMockEndpoint().setExpectedMessageCount(1);
		simpleBean.setStringValue("foo");
        waitAndAssertMessageReceived("src/test/resources/consumer-test/attributeChange-0.xml");

        getMockEndpoint().setExpectedMessageCount(1);
		simpleBean.setStringValue("bar");
        waitAndAssertMessageReceived("src/test/resources/consumer-test/attributeChange-1.xml");
		
		// set the string to null
        getMockEndpoint().setExpectedMessageCount(1);
		simpleBean.setStringValue(null);
        waitAndAssertMessageReceived("src/test/resources/consumer-test/attributeChange-2.xml");
	}

    @Test
	public void notification() throws Exception {
		simpleBean.touch();
		waitAndAssertMessageReceived("src/test/resources/consumer-test/touched.xml");
	}
	
	@Test
	public void userData() throws Exception {
        simpleBean.userData("myUserData");
        waitAndAssertMessageReceived("src/test/resources/consumer-test/userdata.xml");
	}
	
	@Test
	public void jmxConnection() throws Exception {
        simpleBean.triggerConnectionNotification();
        waitAndAssertMessageReceived("src/test/resources/consumer-test/jmxConnectionNotification.xml");
	}
	
	@Test
	public void mbeanServerNotification() throws Exception {
        simpleBean.triggerMBeanServerNotification();
        waitAndAssertMessageReceived("src/test/resources/consumer-test/mbeanServerNotification.xml");
	}

	@Test
    public void relationNotification() throws Exception {
        simpleBean.triggerRelationNotification();
        waitAndAssertMessageReceived("src/test/resources/consumer-test/relationNotification.xml");
    }

	@Test
    public void timerNotification() throws Exception {
        simpleBean.triggerTimerNotification();
        waitAndAssertMessageReceived("src/test/resources/consumer-test/timerNotification.xml");
    }

	private void waitAndAssertMessageReceived(String aExpectedFilePath) throws InterruptedException, Exception {
        waitForMessages();
        assertMessageReceived(new File(aExpectedFilePath));
    }

}
