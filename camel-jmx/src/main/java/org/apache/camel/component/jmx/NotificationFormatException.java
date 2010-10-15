package org.apache.camel.component.jmx;

/**
 * Reports a problem formatting the Notification. This will be a JAXB related issue.
 * 
 * @author markford
 */
public class NotificationFormatException extends Exception {
    public NotificationFormatException(Exception aCausedBy) {
        super(aCausedBy);
    }
}
