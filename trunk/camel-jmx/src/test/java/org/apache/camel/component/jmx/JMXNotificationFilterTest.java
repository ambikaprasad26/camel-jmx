package org.apache.camel.component.jmx;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.apache.camel.Exchange;
import org.apache.camel.component.jmx.beans.ISimpleMXBean;
import org.junit.Test;


/**
 * Tests that the NotificationFilter is applied if configured
 * 
 * @author markford
 * 
 */
public class JMXNotificationFilterTest extends SimpleBeanFixture {
    
    /** we'll track the rejected messages so we know what got filtered */
    private LinkedHashSet<Notification> mRejected = new LinkedHashSet<Notification>();
    
    @Test
    public void testNotificationFilter() throws Exception {
        ISimpleMXBean bean = getSimpleMXBean();

        assertEquals("no notifications should have been filtered at this point", 0, mRejected.size());

        // we should only get 5 messages, which is 1/2 the number of times we touched the object.
        // The 1/2 is due to the behavior of the test NotificationFilter implemented below 
        getMockEndpoint().setExpectedMessageCount(5);
        for(int i=0; i<10; i++) {
            bean.touch();
        }
        
        waitForMessages();
        assertEquals("5 notifications should have been filtered", 5, mRejected.size());
        
        // assert that all of the rejected ones are odd and accepted ones even
        for(Notification rejected : mRejected) {
            assertEquals(1, rejected.getSequenceNumber() % 2);
        }
        
        for(Exchange received : getMockEndpoint().getReceivedExchanges()) {
            Notification n = (Notification) received.getIn().getBody();
            assertEquals(0, n.getSequenceNumber() % 2);
        }
    }

    @Override
    protected JMXUriBuilder buildFromURI() {
        // use the raw format so we can we can get the Notification and assert on the sequence
        return super.buildFromURI().withNotificationFilter("#myFilter").withFormat("raw");
    }

    @SuppressWarnings("serial")
	@Override
    protected void initRegistry() {
        super.initRegistry();
        
        // initialize the registry with our filter
        getRegistry().put("myFilter", new NotificationFilter() {

            @Override
            public boolean isNotificationEnabled(Notification aNotification) {
                // only accept even notifications
                boolean enabled = aNotification.getSequenceNumber() % 2 == 0;
                if (!enabled)
                    mRejected.add(aNotification);
                return enabled;
            }
        });
    }
    
}
