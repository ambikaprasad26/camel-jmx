package com.massfords.camel.jmx;

import java.util.Hashtable;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

public class JMXEndpoint extends DefaultEndpoint {

	/*
	 
	common-params 
		objectDomain=the-domain-value
		objectName=the-object-value
		key.<keyname>=the-key-value
		format=xml ?
		filterRef=the-filter-ref
		filterClass=the-filter-class

	jmx:platform
	
	jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
		user=the-user
		password=the-password
	
	 */
	private String mFormat = "xml";
	private String mUser;
	private String mPassword;
	private String mObjectDomain;
	private String mObjectName;
	private Hashtable<String,String> mObjectProperties;
	private String mServerURL;
	private NotificationFilter mNotificationFilter;
	private String mFilterClass;
	private Object mHandback;
	private ObjectName mJMXObjectName;

    public JMXEndpoint(String aEndpointUri, JMXComponent aComponent) {
    	super(aEndpointUri, aComponent);
    }

	@Override
	public Consumer createConsumer(Processor aProcessor) throws Exception {
		return new JMXConsumer(this, aProcessor);
	}

	@Override
	public Producer createProducer() throws Exception {
		throw new UnsupportedOperationException("producing JMX notifications is not supoprted"); 
	}
	
	@Override
	public boolean isSingleton() {
		return true;
	}

	public String getFormat() {
		return mFormat;
	}

	public void setFormat(String aFormat) {
		mFormat = aFormat;
	}
	
	public boolean isXML() {
		return "xml".equals(getFormat());
	}
	
	public boolean isPlatformServer() {
		return "platform".equals(getServerURL());
	}

	public String getUser() {
		return mUser;
	}

	public void setUser(String aUser) {
		mUser = aUser;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String aPassword) {
		mPassword = aPassword;
	}

	public String getObjectDomain() {
		return mObjectDomain;
	}

	public void setObjectDomain(String aObjectDomain) {
		mObjectDomain = aObjectDomain;
	}

	public String getObjectName() {
		return mObjectName;
	}

	public void setObjectName(String aObjectName) {
		mObjectName = aObjectName;
	}

	protected String getServerURL() {
		return mServerURL;
	}

	protected void setServerURL(String aServerURL) {
		mServerURL = aServerURL;
	}

	public NotificationFilter getNotificationFilter() {
		return mNotificationFilter;
	}

	public void setNotificationFilter(NotificationFilter aFilterRef) {
		mNotificationFilter = aFilterRef;
	}

	public String getFilterClass() {
		return mFilterClass;
	}

	public void setFilterClass(String aFilterClass) {
		mFilterClass = aFilterClass;
	}

	public Object getHandback() {
		return mHandback;
	}

	public void setHandback(Object aHandback) {
		mHandback = aHandback;
	}

	public Hashtable<String, String> getObjectProperties() {
		return mObjectProperties;
	}

	public void setObjectProperties(Hashtable<String, String> aObjectProperties) {
		mObjectProperties = aObjectProperties;
	}

	protected ObjectName getJMXObjectName() throws Exception {
		if (mJMXObjectName == null) {
			ObjectName on = buildObjectName();
			setJMXObjectName(on);
		}
		return mJMXObjectName;
	}

	protected void setJMXObjectName(ObjectName aCachedObjectName) {
		mJMXObjectName = aCachedObjectName;
	}

	private ObjectName buildObjectName() throws Exception {
		ObjectName objectName = null;
		if (objectName == null) {
			
			if (getObjectProperties() == null) {
				StringBuilder sb = new StringBuilder(getObjectDomain()).append(':').append("name=").append(getObjectName());
				objectName = new ObjectName(sb.toString());
			} else {
				objectName = new ObjectName(getObjectDomain(), getObjectProperties());
			}
		}
		return objectName;
	}
}
