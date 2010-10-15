package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Hashtable;

import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.component.jmx.JMXEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

/**
 * Test behavior in the component for initializing an endpoint. Not much here beyond
 * checking that the code for the required and mutually exclusive params is working. 
 * 
 * @author markford
 */
public class JMXComponentTest {

	DefaultCamelContext context = new DefaultCamelContext();

	@Test
	public void withObjectProperties() throws Exception {

		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&key.propOne=prop1&key.propTwo=prop2");
		assertNotNull(ep);
		
		Hashtable<String,String> props = ep.getObjectProperties();
		assertEquals(2, props.size());
		assertEquals("prop1", props.get("propOne"));
		assertEquals("prop2", props.get("propTwo"));
		
		assertNull(ep.getObjectName());
	}
	
	@Test
	public void withObjectName() throws Exception {
		JMXEndpoint ep = (JMXEndpoint) context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName");
		assertNotNull(ep);
		
		assertEquals("theObjectName", ep.getObjectName());
		
		Hashtable<String,String> props = ep.getObjectProperties();
		assertNull(props);
	}
	
	@Test
	public void withObjectName_and_objectProperties() throws Exception {
	    try {
	        context.getEndpoint("jmx:platform?objectDomain=FooDomain&objectName=theObjectName&key.propOne=prop1");
	        fail("expected exception");
	    } catch(ResolveEndpointFailedException e) {
	        assertTrue(e.getCause() instanceof IllegalArgumentException);
	    }
	    
	}
	
	@Test
	public void withoutDomain() throws Exception {
		try {
			context.getEndpoint("jmx:platform?objectName=theObjectName");
			fail("missing domain should have caused failure");
		} catch (ResolveEndpointFailedException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}
	
	@Test
	public void withoutObjectName_and_objectProperties() throws Exception {
		try {
			context.getEndpoint("jmx:platform?objectDomain=theObjectDomain");
			fail("missing name should have caused failure");
		} catch (ResolveEndpointFailedException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
		}
	}
}
