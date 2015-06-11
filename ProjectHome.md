# Dontation to Apache Camel #
This component is now being maintained at Apache Camel. It will be part of the 2.6 release.

For more information, see: http://camel.apache.org/jmx.html

# JMX Component #
Component allows consumers to subscribe to an mbean's Notifications. The component supports passing the Notification object directly through the Exchange or serializing it to XML according to the schema provided within this project. This is a consumer only component. Exceptions are thrown if you attempt to create a producer for it.

# URI Format #

The component can connect to the local platform mbean server with the following URI:

```
jmx://platform?options
```


A remote mbean server url can be provided following the initial JMX scheme like so:

```
jmx:service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi?options
```

You can append query options to the URI in the following format, ?options=value&option2=value&...

# Endpoint Properties #

## Consumer Only Properties ##
| **Property** | **Required** | **Default** | **Description** |
|:-------------|:-------------|:------------|:----------------|
| format       | -            | xml         | Format for the message body. Either "xml" or "raw". If xml, the notification is serialized to xml. If raw, then the raw java object is set as the body. |
| user         | -            | -           | credentials for making a remote connection |
| password     | -            | -           | credentials for making a remote connection |
| objectDomain | yes          | -           | The domain for the mbean you're connecting to |
| objectName   | -            | -           | The name key for the mbean you're connecting to. This value is mutually exclusive with the object properties that get passed. (see below) |
| notificationFilter | -            | -           | Reference to a bean that implements the NotificationFilter. The #ref syntax should be used to reference the bean via the spring or camel registry. |
| handback     | -            | -           | Value to handback to the listener when a notification is received. This value will be put in the message header with the key "jmx.handback" |

## ObjectName Construction ##

The URI must always have the objectDomain property. In addition, the URI must contain either objectName or one or more properties that start with "key."

### Domain with Name property ###

When the objectName property is provided, the following constructor is used to build the ObjectName for the mbean:

```
ObjectName(String domain, String key, String value) 
```

The key value in the above will be "name" and the value will be the value of the objectName property.

### Domain with Hashtable ###

```
ObjectName(String domain, Hashtable<String,String> table)
```

The Hashtable is constructed by extracting properties that start with "key." The properties will have the "key." prefixed stripped prior to building the Hashtable. This allows the URI to contain a variable number of properties to identify the mbean.