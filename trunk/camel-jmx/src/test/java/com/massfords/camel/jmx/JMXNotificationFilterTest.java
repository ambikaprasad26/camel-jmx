package com.massfords.camel.jmx;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.apache.camel.Exchange;
import org.junit.Test;

import com.massfords.camel.jmx.beans.ISimpleMXBean;

public class JMXNotificationFilterTest extends SimpleBeanFixture {
    
    private LinkedHashSet<Notification> mRejected = new LinkedHashSet<Notification>();
    
    @Test
    public void testNotificationFilter() throws Exception {
        // touch the bean a few times, we should only get 1/2 of the notifications since the filter rejects odd sequence numbers
        ISimpleMXBean bean = getSimpleMXBean();

        assertEquals("no notifications should have been filtered at this point", 0, mRejected.size());

        for(int i=0; i<10; i++) {
            bean.touch();
        }
        
        waitForMessages(5);

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
    protected JMXUri buildFromURI() {
        return super.buildFromURI().withNotificationFilter("#myFilter").withFormat("raw");
    }

    @Override
    protected void initRegistry() {
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
