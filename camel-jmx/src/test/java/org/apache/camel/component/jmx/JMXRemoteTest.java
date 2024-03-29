package org.apache.camel.component.jmx;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;

import javax.management.MBeanServerFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Test;

/**
 * Tests against a "remote" JMX server. Creates an RMI Registry at port 61000
 * and registers the simple mbean
 * 
 * Only test here is the notification test since everything should work the
 * same as the platform server. May want to refactor the exisiting tests to 
 * run the full suite on the local platform and this "remote" setup.
 * 
 * @author markford
 * 
 */
public class JMXRemoteTest extends SimpleBeanFixture {

    JMXServiceURL url;
    JMXConnectorServer connector;
    Registry registry;

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        connector.stop();
    }
    
    @Override
    protected void initServer() throws Exception {
        registry = LocateRegistry.createRegistry(61000);
        
        url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:61000/" + DOMAIN);
        // create MBean server
        server = MBeanServerFactory.createMBeanServer(DOMAIN);
        // create JMXConnectorServer MBean
        connector = JMXConnectorServerFactory.newJMXConnectorServer(url, Collections.EMPTY_MAP, server);
        connector.start();
    }

    @Override
    protected JMXUriBuilder buildFromURI() {
        String uri = url.toString();
        return super.buildFromURI().withServerName(uri);
    }

    @Test
    public void notification() throws Exception {
        getSimpleMXBean().touch();
        waitForMessages();
        assertMessageReceived(new File("src/test/resources/consumer-test/touched.xml"));
    }
}
