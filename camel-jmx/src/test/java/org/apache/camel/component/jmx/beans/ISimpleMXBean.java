package org.apache.camel.component.jmx.beans;

public interface ISimpleMXBean {
	public String getStringValue();
	public void setStringValue(String aValue);
	public void touch();
	public void userData(String aUserData);
    public void triggerConnectionNotification();
    public void triggerMBeanServerNotification() throws Exception;
    public void triggerRelationNotification() throws Exception;
    public void triggerTimerNotification();
    public int getMonitorNumber();
}
