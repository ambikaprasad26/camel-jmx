package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.management.ObjectName;

import org.apache.camel.component.jmx.JMXEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

public class JMXEndpointTest {

	DefaultCamelContext context = new DefaultCamelContext();

	@Test
	public void test_defaultsToXml() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName");
		assertTrue(ep.isXML());
	}

	@Test
	public void test_format() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName&format=raw");
		assertFalse(ep.isXML());
		assertEquals("raw", ep.getFormat());
	}
	
	@Test
	public void test_getJMXObjectName() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertEquals("FooDomain:name=theObjectName", on.toString());
	}
	
	@Test
	public void test_getJMXObjectName_withProps() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertEquals("FooDomain:name=theObjectName", on.toString());
	}
	
	@Test
	public void test_getJMXObjectName_cached() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		ObjectName on = ep.getJMXObjectName();
		assertNotNull(on);
		assertSame(on, ep.getJMXObjectName());
	}
	
	@Test
	public void test_PlatformServer() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
		assertTrue(ep.isPlatformServer());
		assertEquals("platform", ep.getServerURL());
	}

	@Test
	public void test_RemoteServer() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi?objectDomain=FooDomain&key.name=theObjectName");
		assertFalse(ep.isPlatformServer());
		assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", ep.getServerURL());

		ep = (JMXEndpoint) context.getEndpoint("jmx://service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi?objectDomain=FooDomain&key.name=theObjectName");
		assertFalse(ep.isPlatformServer());
		assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", ep.getServerURL());
	}
	
	@Test
	public void test_noProducer() throws Exception {
        JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName");
        try {
            ep.createProducer();
            fail("producer pattern is not supported");
        } catch(UnsupportedOperationException e) {
        }
	}
	
	@Test
	public void test_credentials() throws Exception {
        JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.name=theObjectName&user=user1&password=1234");
        assertEquals("user1", ep.getUser());
        assertEquals("1234", ep.getPassword());
	}
}
