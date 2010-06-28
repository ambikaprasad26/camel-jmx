package com.massfords.camel.jmx.beans;

public interface ISimpleMXBean {
	public String getStringValue();
	public void setStringValue(String aValue);
	public void touch();
}
