package com.massfords.camel.jmx;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;
import javax.management.relation.RelationNotification;
import javax.management.remote.JMXConnectionNotification;
import javax.management.timer.TimerNotification;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import com.massfords.code.camel_jmx.NotificationEventType;
import com.massfords.code.camel_jmx.ObjectFactory;
import com.massfords.code.camel_jmx.RelationNotification.MBeansToUnregister;
import com.massfords.code.camel_jmx.RelationNotification.NewRoleValue;
import com.massfords.code.camel_jmx.RelationNotification.OldRoleValue;

public class NotificationXmlFormatter {

    private DatatypeFactory mDatatypeFactory;
    private Marshaller mMarshaller;
    private Lock mMarshallerLock = new ReentrantLock(false);

    public String format(Notification aNotification) throws NotificationFormatException {

        ObjectFactory of = new ObjectFactory();

        NotificationEventType jaxb = null;

        boolean wrap = false;

        if (aNotification instanceof AttributeChangeNotification) {
            AttributeChangeNotification ac = (AttributeChangeNotification) aNotification;

			jaxb = of.createAttributeChangeNotification()
				.withAttributeName(ac.getAttributeName())
				.withAttributeType(ac.getAttributeType())
				.withNewValue(ac.getNewValue() == null ? null : String.valueOf(ac.getNewValue()))
				.withOldValue(ac.getOldValue() == null ? null : String.valueOf(ac.getOldValue()));
		} else if (aNotification instanceof JMXConnectionNotification) {
			jaxb = of.createJMXConnectionNotification()
				.withConnectionId(((JMXConnectionNotification) aNotification).getConnectionId());
		} else if (aNotification instanceof MBeanServerNotification) {
			jaxb = of.createMBeanServerNotification()
				.withMBeanName(String.valueOf(((MBeanServerNotification) aNotification).getMBeanName()));
		} else if (aNotification instanceof MonitorNotification) {
			MonitorNotification mn = (MonitorNotification) aNotification;
			jaxb = of.createMonitorNotification()
				.withDerivedGauge(String.valueOf(mn.getDerivedGauge()))
				.withObservedAttribute(mn.getObservedAttribute())
				.withObservedObject(String.valueOf(mn.getObservedObject()))
				.withTrigger(String.valueOf(mn.getTrigger()));
		} else if (aNotification instanceof RelationNotification) {
			RelationNotification rn = (RelationNotification) aNotification;
			jaxb = of.createRelationNotification()
				.withObjectName(String.valueOf(rn.getObjectName()))
				.withRelationId(rn.getRelationId())
				.withRelationTypeName(rn.getRelationTypeName())
				.withRoleName(rn.getRoleName());
			if (rn.getNewRoleValue() != null) {
				List<String> roles = toStringList(rn.getNewRoleValue());
				NewRoleValue nrv = of.createRelationNotificationNewRoleValue().withObjectName(roles);
				((com.massfords.code.camel_jmx.RelationNotification) jaxb).withNewRoleValue(nrv);
			}
			if (rn.getOldRoleValue() != null) {
				List<String> roles = toStringList(rn.getOldRoleValue());
				OldRoleValue orv = of.createRelationNotificationOldRoleValue().withObjectName(roles);
				((com.massfords.code.camel_jmx.RelationNotification) jaxb).withOldRoleValue(orv);
			}
			if (rn.getMBeansToUnregister() != null) {
				List<String> roles = toStringList(rn.getMBeansToUnregister());
				MBeansToUnregister orv = of.createRelationNotificationMBeansToUnregister().withObjectName(roles);
				((com.massfords.code.camel_jmx.RelationNotification) jaxb).withMBeansToUnregister(orv);
			}
		} else if (aNotification instanceof TimerNotification) {
			jaxb = of.createTimerNotification().withNotificationId(((TimerNotification) aNotification).getNotificationID());
		} else {
			jaxb = of.createNotificationEventType();
			wrap = true;
		}
		
		// add all of the common properties
		jaxb.withMessage(aNotification.getMessage())
			.withSequence(aNotification.getSequenceNumber())
			.withSource(String.valueOf(aNotification.getSource()))
			.withTimestamp(aNotification.getTimeStamp())
			.withType(aNotification.getType());
        if (aNotification.getUserData() != null)
            jaxb.withUserData(String.valueOf(aNotification.getUserData()));

        try {
            DatatypeFactory df = getDatatypeFactory();
            Date date = new Date(aNotification.getTimeStamp());
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(date);
            jaxb.withDateTime(df.newXMLGregorianCalendar(gc));

            Object bean = wrap ? of.createNotificationEvent(jaxb) : jaxb;

            StringWriter sw = new StringWriter();

            try {
                mMarshallerLock.lock();
                getMarshaller(of.getClass().getPackage().getName()).marshal(bean, sw);
            } finally {
                mMarshallerLock.unlock();
            }
            return sw.toString();
        } catch (JAXBException e) {
            throw new NotificationFormatException(e);
        } catch (DatatypeConfigurationException e) {
            throw new NotificationFormatException(e);
        }
    }

    private DatatypeFactory getDatatypeFactory() throws DatatypeConfigurationException {
        if (mDatatypeFactory == null) {
            mDatatypeFactory = DatatypeFactory.newInstance();
        }
        return mDatatypeFactory;
    }

    private Marshaller getMarshaller(String aPackageName) throws JAXBException {
        if (mMarshaller == null) {
            mMarshaller = JAXBContext.newInstance(aPackageName).createMarshaller();
        }
        return mMarshaller;
    }

    private List<String> toStringList(List<ObjectName> objectNames) {
        List<String> roles = new ArrayList(objectNames.size());
        for (ObjectName on : objectNames) {
            roles.add(on.toString());
        }
        return roles;
    }
}
