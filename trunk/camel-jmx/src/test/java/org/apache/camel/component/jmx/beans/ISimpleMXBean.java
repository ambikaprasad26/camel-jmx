package org.apache.camel.component.jmx.beans;

public interface ISimpleMXBean {
	public String getStringValue();
	public void setStringValue(String aValue);
	public void touch();
	public void userData(String aUserData);
    public void triggerConnectionNotification();
}
