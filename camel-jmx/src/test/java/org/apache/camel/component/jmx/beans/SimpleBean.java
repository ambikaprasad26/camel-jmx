package org.apache.camel.component.jmx.beans;

import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.relation.RelationNotification;
import javax.management.remote.JMXConnectionNotification;
import javax.management.timer.TimerNotification;

/**
 * Simple bean that is used for testing. 
 * 
 * @author markford
 */
public class SimpleBean extends NotificationBroadcasterSupport implements ISimpleMXBean {
	private int mSequence;
	/** Use the same timestamp every time so the assertions are easier */
	private long mTimestamp;
	
	private String mStringValue;

	public String getStringValue() {
		return mStringValue;
	}

	public void setStringValue(String aStringValue) {
		String oldValue = getStringValue();
		mStringValue = aStringValue;
		
		AttributeChangeNotification acn = new AttributeChangeNotification(
				this, mSequence++, mTimestamp, "attribute changed", "stringValue", "string", oldValue, mStringValue);
		sendNotification(acn);
	}
	
	public int getMonitorNumber() {
	    return getSequence();
	}
	

    public int getSequence() {
		return mSequence;
	}

	public void setSequence(int aSequence) {
		mSequence = aSequence;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long aTimestamp) {
		mTimestamp = aTimestamp;
	}

    @Override
    public void userData(String aUserData) {
        Notification n = new Notification("userData", this, mSequence++, mTimestamp, "Here's my user data");
        n.setUserData(aUserData);
        sendNotification(n);
    }

    @Override
	public void touch() {
		Notification n = new Notification("touched", this, mSequence++, mTimestamp, "I was touched");
		sendNotification(n);
	}
    
    @Override
    public void triggerConnectionNotification() {
        JMXConnectionNotification n = new JMXConnectionNotification("connection", this, 
                                        "conn-123", mSequence++, "connection notification", null);
        n.setTimeStamp(mTimestamp);
        sendNotification(n);
    }

    @Override
    public void triggerMBeanServerNotification() throws Exception {
        MBeanServerNotification n = new MBeanServerNotification("mbeanserver", this, mSequence++, new ObjectName("TestDomain", "name", "foo"));
        n.setTimeStamp(mTimestamp);
        sendNotification(n);
    }

    @Override
    public void triggerRelationNotification() throws Exception {
        List<ObjectName> list = new ArrayList();
        for(int i=1; i<=3; i++) {
            list.add(new ObjectName("TestDomain", "name", "mbean-" + i));
        }
        RelationNotification n = new RelationNotification(RelationNotification.RELATION_BASIC_CREATION, 
                                            new ObjectName("TestDomain", "name", "source"), mSequence++, mTimestamp, 
                                            "relation message", 
                                            "relation-id", 
                                            "relation.type", 
                                            new ObjectName("TestDomain", "name", "foo"), 
                                            list);
        sendNotification(n);
    }

    @Override
    public void triggerTimerNotification() {
        TimerNotification n = new TimerNotification("timer.notification", this, mSequence++, mTimestamp, "timer-notification", 100);
        sendNotification(n);
    }
}
