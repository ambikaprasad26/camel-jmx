package com.massfords.camel.jmx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JMXUri {
    private Map<String,String> mQueryProps = new LinkedHashMap();
    private String mServerName = "platform"; 
    
    public JMXUri() {
    }
    
    public JMXUri(String aServerName) {
        mServerName = aServerName;
    }
    
    public JMXUri withFormat(String aFormat) {
        addProperty("format", aFormat);
        return this;
    }
    
    public JMXUri withUser(String aFormat) {
        addProperty("user", aFormat);
        return this;
    }

    public JMXUri withPassword(String aFormat) {
        addProperty("user", aFormat);
        return this;
    }

    public JMXUri withObjectDomain(String aFormat) {
        addProperty("objectDomain", aFormat);
        return this;
    }

    public JMXUri withObjectName(String aFormat) {
        addProperty("objectName", aFormat);
        return this;
    }

    public JMXUri withNotificationFilter(String aFilter) {
        addProperty("notificationFilter", aFilter);
        return this;
    }

    public JMXUri withHandback(String aHandback) {
        addProperty("handback", aHandback);
        return this;
    }

    public JMXUri withObjectProperties(Map<String,String> aPropertiesSansKeyPrefix) {
        for(Entry<String,String> entry : aPropertiesSansKeyPrefix.entrySet()) {
            addProperty("key." + entry.getKey(), entry.getValue());
        }
        return this;
    }

    public void addProperty(String aName, String aValue) {
        mQueryProps.put(aName, aValue);
    }
    
    public JMXUri withProperty(String aName, String aValue) {
        addProperty(aName, aValue);
        return this;
    }
    
    public String getServerName() {
        return mServerName;
    }

    public void setServerName(String aServerName) {
        mServerName = aServerName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("jmx:").append(mServerName);
        if (!mQueryProps.isEmpty()) {
            sb.append('?');
            
            String delim = "";
            for(Entry<String, String> entry : mQueryProps.entrySet()) {
                sb.append(delim);
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                delim = "&";
            }
        }
        return sb.toString();
    }
}
