package org.apache.camel.component.jmx;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.EndpointHelper;

/**
 * Component for connecting JMX Notification events to a camel route.
 * The endpoint created from this component allows users to specify
 * an ObjectName to listen to and any JMX Notifications received from
 * that object will flow into the route.
 * 
 * @author markford
 */
public class JMXComponent extends DefaultComponent {

	@Override
	protected Endpoint createEndpoint(String aUri, String aRemaining,
			Map<String, Object> aParameters) throws Exception {
		JMXEndpoint endpoint = new JMXEndpoint(aUri, this);
		// use the helper class to set all of the properties
		EndpointHelper.setReferenceProperties(getCamelContext(), endpoint, aParameters);
		EndpointHelper.setProperties(getCamelContext(), endpoint, aParameters);
		
		endpoint.setServerURL(aRemaining);
		
		// we may have some extra params left over for the object properties hashtable
		// thse properties need to be consumed or the framework will throw an exception
		// for unused params
		if (!aParameters.isEmpty()) {
			Hashtable<String,String> objectProperties = new Hashtable();
			
			for(Iterator<Entry<String,Object>> it=aParameters.entrySet().iterator(); it.hasNext();) {
				Entry<String,Object> entry = it.next();
				if (entry.getKey().startsWith("key.")) {
					objectProperties.put(entry.getKey().substring("key.".length()), entry.getValue().toString());
					it.remove();
				}
			}
			
			endpoint.setObjectProperties(objectProperties);
		}
		
		if (endpoint.getObjectDomain() == null) {
			throw new IllegalStateException("must specify domain");
		}
		
		if (endpoint.getObjectName() == null && endpoint.getObjectProperties() == null) {
			throw new IllegalStateException("must specify object name or object properties");
		}
		return endpoint;
	}

}
