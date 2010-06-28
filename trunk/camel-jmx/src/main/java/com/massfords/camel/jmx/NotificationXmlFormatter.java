package com.massfords.camel.jmx;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;
import javax.management.relation.RelationNotification;
import javax.management.remote.JMXConnectionNotification;
import javax.management.timer.TimerNotification;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;

import com.massfords.code.camel_jmx.NotificationEventType;
import com.massfords.code.camel_jmx.ObjectFactory;
import com.massfords.code.camel_jmx.RelationNotification.MBeansToUnregister;
import com.massfords.code.camel_jmx.RelationNotification.NewRoleValue;
import com.massfords.code.camel_jmx.RelationNotification.OldRoleValue;

public class NotificationXmlFormatter {

	public String format(Notification aNotification) throws Exception {
		
		ObjectFactory of = new ObjectFactory();
		
		NotificationEventType jaxb = null;
	
		if (aNotification instanceof AttributeChangeNotification) {
			AttributeChangeNotification ac = (AttributeChangeNotification) aNotification;
			
			jaxb = of.createAttributeChangeNotification()
				.withAttributeName(ac.getAttributeName())
				.withAttributeType(ac.getAttributeType())
				.withNewValue(String.valueOf(ac.getNewValue()))
				.withOldValue(String.valueOf(ac.getOldValue()));
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
		}
		
		// add all of the common properties
		jaxb.withMessage(aNotification.getMessage())
			.withSequence(aNotification.getSequenceNumber())
			.withSource(String.valueOf(aNotification.getSource()))
			.withTimestamp(aNotification.getTimeStamp())
			.withType(aNotification.getType());
		if (aNotification.getUserData() != null)
			jaxb.withUserData(String.valueOf(aNotification.getUserData()));
		
		DatatypeFactory df = DatatypeFactory.newInstance();
		Date date = new Date(aNotification.getTimeStamp());
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		jaxb.withDateTime(df.newXMLGregorianCalendar(gc));
		
		JAXBContext context = JAXBContext.newInstance(jaxb.getClass());
		Marshaller marshaller = context.createMarshaller();
		
		StringWriter sw = new StringWriter();
		marshaller.marshal(jaxb, sw);
		
		return sw.toString();
	}

	private List<String> toStringList(List<ObjectName> objectNames) {
		List<String> roles = new ArrayList(objectNames.size());
		for(ObjectName on : objectNames) {
			roles.add(on.toString());
		}
		return roles;
	}
}
