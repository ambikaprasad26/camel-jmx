package org.apache.camel.component.jmx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Builder for JMX endpoint URI's. Saves you from having to do the string concat'ing
 * and messing up the param names
 * 
 * @author markford
 * 
 */
public class JMXUriBuilder {
    private Map<String,String> mQueryProps = new LinkedHashMap();
    private String mServerName = "platform"; 
    
    public JMXUriBuilder() {
    }
    
    public JMXUriBuilder(String aServerName) {
        setServerName(aServerName);
    }
    
    public JMXUriBuilder withFormat(String aFormat) {
        addProperty("format", aFormat);
        return this;
    }
    
    public JMXUriBuilder withUser(String aFormat) {
        addProperty("user", aFormat);
        return this;
    }

    public JMXUriBuilder withPassword(String aFormat) {
        addProperty("password", aFormat);
        return this;
    }

    public JMXUriBuilder withObjectDomain(String aFormat) {
        addProperty("objectDomain", aFormat);
        return this;
    }

    public JMXUriBuilder withObjectName(String aFormat) {
        addProperty("objectName", aFormat);
        return this;
    }

    public JMXUriBuilder withNotificationFilter(String aFilter) {
        addProperty("notificationFilter", aFilter);
        return this;
    }

    public JMXUriBuilder withHandback(String aHandback) {
        addProperty("handback", aHandback);
        return this;
    }

    /**
     * Converts all of the values to params with the "key." prefix so the 
     * component will pick up on them and set them on the endpoint. Alternatively,
     * you can pass in a reference to a Hashtable using the version of this
     * method that takes a single string.
     * @param aPropertiesSansKeyPrefix
     */
    public JMXUriBuilder withObjectProperties(Map<String,String> aPropertiesSansKeyPrefix) {
        for(Entry<String,String> entry : aPropertiesSansKeyPrefix.entrySet()) {
            addProperty("key." + entry.getKey(), entry.getValue());
        }
        return this;
    }
    
    /**
     * Your value should start with a hash mark since it's a reference to a value.
     * This method will add the hash mark if it's not present.
     * @param aReferenceToHashtable
     */
    public JMXUriBuilder withObjectPropertiesReference(String aReferenceToHashtable) {
        if(aReferenceToHashtable.startsWith("#"))
            addProperty("objectProperties", aReferenceToHashtable);
        else
            addProperty("objectProperties", "#" + aReferenceToHashtable);
        return this;
    }

    protected void addProperty(String aName, String aValue) {
        mQueryProps.put(aName, aValue);
    }
    
    public String getServerName() {
        return mServerName;
    }

    public void setServerName(String aServerName) {
        mServerName = aServerName;
    }
    
    public JMXUriBuilder withServerName(String aServerName) {
        setServerName(aServerName);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("jmx:").append(getServerName());
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
