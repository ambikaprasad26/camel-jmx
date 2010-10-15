package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;

import org.apache.camel.component.jmx.JMXUriBuilder;
import org.junit.Test;

/**
 * Various tests for the uri builder
 * 
 * @author markford
 *
 */
public class JMXUriBuilderTest {

    @Test
    public void defaultsToPlatform() throws Exception {
        assertEquals("jmx:platform", new JMXUriBuilder().toString());
    }
    
    @Test
    public void remote() throws Exception {
        assertEquals("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", new JMXUriBuilder("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi").toString());
    }
    
    @Test
    public void withServerName() throws Exception {
        assertEquals("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", new JMXUriBuilder().withServerName("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi").toString());
    }

    @Test
    public void format() throws Exception {
        assertEquals("jmx:platform?format=raw", new JMXUriBuilder().withFormat("raw").toString());
    }

    @Test
    public void credentials() throws Exception {
        assertEquals("jmx:platform?user=me&password=pass", new JMXUriBuilder().withUser("me").withPassword("pass").toString());
    }

    @Test
    public void objectName() throws Exception {
        assertEquals("jmx:platform?objectDomain=myDomain&objectName=oname", new JMXUriBuilder().withObjectDomain("myDomain").withObjectName("oname").toString());
    }

    @Test
    public void notificationFilter() throws Exception {
        assertEquals("jmx:platform?notificationFilter=#foo", new JMXUriBuilder().withNotificationFilter("#foo").toString());
    }

    @Test
    public void handback() throws Exception {
        assertEquals("jmx:platform?handback=#hb", new JMXUriBuilder().withHandback("#hb").toString());
    }

    @Test
    public void objectProperties() throws Exception {
        LinkedHashMap<String,String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        assertEquals("jmx:platform?key.one=1&key.two=2", new JMXUriBuilder().withObjectProperties(map).toString());
    }
    
    @Test
    public void withObjectPropertiesReference() throws Exception {
        assertEquals("jmx:platform?objectProperties=#op", new JMXUriBuilder().withObjectPropertiesReference("#op").toString());
    }

    @Test
    public void withObjectPropertiesReference_sansHashmark() throws Exception {
        assertEquals("jmx:platform?objectProperties=#op", new JMXUriBuilder().withObjectPropertiesReference("op").toString());
    }
}
