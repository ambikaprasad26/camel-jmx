package org.apache.camel.component.jmx;

import java.util.Hashtable;

import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.junit.Test;


/**
 * Tests that the objectName is created with the hashtable of objectProperties
 * 
 * @author markford
 * 
 */
public class JMXObjectPropertiesTest extends SimpleBeanFixture {
    
    @Test
    public void testObjectProperties() throws Exception {
        ISimpleMXBean bean = getSimpleMXBean();
        bean.touch();
        waitForMessages();
    }

    @Override
    protected JMXUriBuilder buildFromURI() {
        return new JMXUriBuilder().withObjectDomain(DOMAIN).withObjectPropertiesReference("#myTable");
    }

    @SuppressWarnings("serial")
	@Override
    protected void initRegistry() {
        Hashtable<String,String> ht = new Hashtable();
        ht.put("name", "simpleBean");
        getRegistry().put("myTable", ht);
    }
    
}
