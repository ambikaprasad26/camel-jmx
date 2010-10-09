package com.massfords.camel.jmx;

import static org.junit.Assert.assertSame;

import java.net.URI;

import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;

import com.massfords.camel.jmx.beans.ISimpleMXBean;

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
        
        waitForMessages(1);
        
        Message m = getMessage(0);
        URI uri = (URI) m.getHeader("jmx.handback");
        assertSame(hb, uri);
    }

    @Override
    protected JMXUri buildFromURI() {
        return super.buildFromURI().withHandback("#hb").withFormat("raw");
    }

    @Override
    protected void initRegistry() throws Exception {
        super.initRegistry();
        getRegistry().put("hb", hb);
    }
}
