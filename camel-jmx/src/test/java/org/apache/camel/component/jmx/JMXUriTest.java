package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.apache.camel.component.jmx.JMXUri;
import org.junit.Test;

public class JMXUriTest {

    @Test
    public void testPlatform() throws Exception {
        assertEquals("jmx:platform", new JMXUri().toString());
    }
    
    @Test
    public void testRemote() throws Exception {
        assertEquals("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", new JMXUri("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi").toString());
    }
    
    @Test
    public void testWithServerName() throws Exception {
        assertEquals("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", new JMXUri().withServerName("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi").toString());
    }

    @Test
    public void testFormat() throws Exception {
        assertEquals("jmx:platform?format=raw", new JMXUri().withFormat("raw").toString());
    }

    @Test
    public void testCredentials() throws Exception {
        assertEquals("jmx:platform?user=me&password=pass", new JMXUri().withUser("me").withPassword("pass").toString());
    }

    @Test
    public void testObjectName() throws Exception {
        assertEquals("jmx:platform?objectDomain=myDomain&objectName=oname", new JMXUri().withObjectDomain("myDomain").withObjectName("oname").toString());
    }

    @Test
    public void testNotificationFilter() throws Exception {
        assertEquals("jmx:platform?notificationFilter=#foo", new JMXUri().withNotificationFilter("#foo").toString());
    }

    @Test
    public void testHandback() throws Exception {
        assertEquals("jmx:platform?handback=#hb", new JMXUri().withHandback("#hb").toString());
    }

    @Test
    public void testObjectProperties() throws Exception {
        LinkedHashMap<String,String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        assertEquals("jmx:platform?key.one=1&key.two=2", new JMXUri().withObjectProperties(map).toString());
    }
}
