package org.apache.camel.component.jmx.beans;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnectionNotification;

public class SimpleBean extends NotificationBroadcasterSupport implements ISimpleMXBean {
	private int mSequence;
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
}
