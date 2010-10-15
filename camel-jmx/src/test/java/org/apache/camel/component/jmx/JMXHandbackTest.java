package org.apache.camel.component.jmx;

import static org.junit.Assert.assertSame;

import java.net.URI;

import org.apache.camel.Message;
import org.apache.camel.component.jmx.JMXUriBuilder;
import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests that we get the handback object in the message header
 * 
 * @author markford
 */
public class JMXHandbackTest extends SimpleBeanFixture {

    URI hb;
    
    @Before
    public void setUp() throws Exception {
        hb = new URI("urn:some:handback:object");
        super.setUp();
    }

    @Test
    public void test() throws Exception {
        ISimpleMXBean simpleBean = getSimpleMXBean();
        simpleBean.userData("myUserData");
        
        waitForMessages();
        
        Message m = getMessage(0);
        URI uri = (URI) m.getHeader("jmx.handback");
        assertSame(hb, uri);
    }

    @Override
    protected JMXUriBuilder buildFromURI() {
        return super.buildFromURI().withHandback("#hb").withFormat("raw");
    }

    @Override
    protected void initRegistry() {
        super.initRegistry();
        getRegistry().put("hb", hb);
    }
}
