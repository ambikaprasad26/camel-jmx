package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Hashtable;

import javax.management.ObjectName;

import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

/**
 * Tests for the endpoint. Most of the params in the endpoint are
 * set via the endpoint helper so there's no much beyond some sanity
 * checks here. 
 * 
 * @author markford
 *
 */
public class JMXEndpointTest {

	DefaultCamelContext context = new DefaultCamelContext();
	
	@Test
	public void setObjectName_throws_when_objectProperties_is_set() throws Exception {
	    JMXEndpoint ep = new JMXEndpoint("urn:ignored", new JMXComponent());
	    ep.setObjectProperties(new Hashtable());
	    try {
	        // should fault since objectName is mutex with objectProperties
	        ep.setObjectName("foo");
	        fail("expected exception");
	    } catch(IllegalArgumentException e) {
	    }
	}

	@Test
	public void defaultsToXml() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName");
		assertTrue(ep.isXML());
	}

	@Test
	public void format_raw() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName&format=raw");
		assertFalse(ep.isXML());
		assertEquals("raw", ep.getFormat());
	}
	
	@Test
	public void getJMXObjectName() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertEquals("FooDomain:name=theObjectName", on.toString());
	}
	
	@Test
	public void getJMXObjectName_withProps() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertEquals("FooDomain:name=theObjectName", on.toString());
	}
	
	@Test
	public void getJMXObjectName_cached() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertSame(on, ep.getJMXObjectName());
	}
	
	@Test
	public void platformServer() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		assertTrue(ep.isPlatformServer());
		assertEquals("platform", ep.getServerURL());
	}

	@Test
	public void remoteServer() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi?objectDomain=FooDomain&key.name=theObjectName");
		assertFalse(ep.isPlatformServer());
		assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", ep.getServerURL());

		ep = (JMXEndpoint) context.getEndpoint("jmx://service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi?objectDomain=FooDomain&key.name=theObjectName");
		assertFalse(ep.isPlatformServer());
		assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", ep.getServerURL());
	}
	
	@Test
	public void noProducer() throws Exception {
        JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
        try {
            ep.createProducer();
            fail("producer pattern is not supported");
        } catch(UnsupportedOperationException e) {
        }
	}
	
	@Test
	public void credentials() throws Exception {
        JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName&user=user1&password=1234");
        assertEquals("user1", ep.getUser());
        assertEquals("1234", ep.getPassword());
	}
}
