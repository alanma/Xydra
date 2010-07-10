package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicEvent;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.impl.memory.MemoryFieldEvent;
import org.xydra.core.change.impl.memory.MemoryModelEvent;
import org.xydra.core.change.impl.memory.MemoryObjectEvent;
import org.xydra.core.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.core.change.impl.memory.MemoryTransactionEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


/**
 * Collection of methods to (de-)serialize variants of {@link XEvent} to and
 * from their XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class XmlEvent {
	
	private static final String XREPOSITORYEVENT_ELEMENT = "xrepositoryEvent";
	private static final String XMODELEVENT_ELEMENT = "xmodelEvent";
	private static final String XOBJECTEVENT_ELEMENT = "xobjectEvent";
	private static final String XFIELDEVENT_ELEMENT = "xfieldEvent";
	private static final String XTRANSACTIONEVENT_ELEMENT = "xtransactionEvent";
	private static final String XEVENTLIST_ELEMENT = "xevents";
	private static final String XNULL_ELEMENT = "xnull";
	
	private static final String REPOSITORYID_ATTRIBUTE = "repositoryId";
	private static final String MODELID_ATTRIBUTE = "modelId";
	private static final String OBJECTID_ATTRIBUTE = "objectId";
	private static final String FIELDID_ATTRIBUTE = "fieldId";
	private static final String FIELDREVISION_ATTRIBUTE = "fieldRevision";
	private static final String OBJECTREVISION_ATTRIBUTE = "objectRevision";
	private static final String MODELREVISION_ATTRIBUTE = "modelRevision";
	private static final String INTRANSACTION_ATTRIBUTE = "inTransaction";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String ACTOR_ATTRIBUTE = "actor";
	
	private static final String ADD_VALUE = "ADD";
	private static final String REMOVE_VALUE = "REMOVE";
	private static final String CHANGE_VALUE = "CHANGE";
	
	private static long getRevision(MiniElement xml, String elementName, String attribute,
	        boolean required) {
		
		String revisionString = xml.getAttribute(attribute);
		
		if(revisionString == null) {
			
			if(required)
				throw new IllegalArgumentException("<" + elementName + ">@" + attribute
				        + " attribute is missing");
			
			return XEvent.RevisionOfEntityNotSet;
		}
		
		try {
			return Long.parseLong(revisionString);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("<" + elementName + ">@" + attribute
			        + " does not contain a long, but '" + revisionString + "'");
		}
	}
	
	private static ChangeType getTypeAttribute(MiniElement xml, String elementName) {
		String typeString = xml.getAttribute(TYPE_ATTRIBUTE);
		if(typeString == null)
			throw new IllegalArgumentException("<" + elementName + ">@" + TYPE_ATTRIBUTE
			        + " is missing");
		if(typeString.equals(ADD_VALUE))
			return ChangeType.ADD;
		else if(typeString.equals(REMOVE_VALUE))
			return ChangeType.REMOVE;
		else if(typeString.equals(CHANGE_VALUE))
			return ChangeType.CHANGE;
		else
			throw new IllegalArgumentException("<" + elementName + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type, but '" + typeString + "'");
	}
	
	private static XID getXidAttribute(MiniElement xml, String attributeName, XID def) {
		String xidString = xml.getAttribute(attributeName);
		if(xidString == null)
			return def;
		return X.getIDProvider().fromString(xidString);
	}
	
	private static boolean getInTransactionAttribute(MiniElement xml) {
		String booleanString = xml.getAttribute(INTRANSACTION_ATTRIBUTE);
		return Boolean.parseBoolean(booleanString);
	}
	
	private static void checkElementName(MiniElement xml, String expectedName) {
		if(!xml.getName().equals(expectedName)) {
			throw new IllegalArgumentException("Given element " + xml + " is not an <"
			        + expectedName + "> element.");
		}
	}
	
	private static void checkHasNoChildren(MiniElement xml, String elementName) {
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + elementName + ">: <"
			        + it.next().getName() + ">");
	}
	
	private static XAddress getTarget(MiniElement xml, XAddress context) {
		
		XID repoId = getXidAttribute(xml, REPOSITORYID_ATTRIBUTE, context == null ? null : context
		        .getRepository());
		XID modelId = getXidAttribute(xml, MODELID_ATTRIBUTE, context == null ? null : context
		        .getModel());
		XID objectId = getXidAttribute(xml, OBJECTID_ATTRIBUTE, context == null ? null : (XI
		        .equals(modelId, context.getModel()) ? context.getObject() : null));
		XID fieldId = getXidAttribute(xml, FIELDID_ATTRIBUTE, context == null ? null : (XI.equals(
		        objectId, context.getObject()) ? context.getField() : null));
		
		return X.getIDProvider().fromComponents(repoId, modelId, objectId, fieldId);
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML. If the given element
	 *            represents a transaction, the context for the contained events
	 *            will be given by the transaction.
	 * @param defaultActor If the XML element does not specify an actor, this
	 *            will be filled in.
	 * @return The {@link XEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XEvent toEvent(MiniElement xml, XAddress context) throws IllegalArgumentException {
		String name = xml.getName();
		if(name.equals(XTRANSACTIONEVENT_ELEMENT)) {
			return toTransactionEvent(xml, context);
		} else {
			return toAtomicEvent(xml, context);
		}
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML. If the given element
	 *            represents a transaction, the context for the contained events
	 *            will be given by the transaction.
	 * @return The {@link XAtomicEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XAtomicEvent toAtomicEvent(MiniElement xml, XAddress context)
	        throws IllegalArgumentException {
		String name = xml.getName();
		if(name.equals(XFIELDEVENT_ELEMENT)) {
			return toFieldEvent(xml, context);
		} else if(name.equals(XOBJECTEVENT_ELEMENT)) {
			return toObjectEvent(xml, context);
		} else if(name.equals(XMODELEVENT_ELEMENT)) {
			return toModelEvent(xml, context);
		} else if(name.equals(XREPOSITORYEVENT_ELEMENT)) {
			return toRepositoryEvent(xml, context);
		} else if(name.equals(XNULL_ELEMENT)) {
			return null;
		} else {
			throw new IllegalArgumentException("Unexpected event element: <" + name + ">.");
		}
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML. The context for the
	 *            events contained in the transaction will be given by the
	 *            transaction.
	 * @return The {@link XTransactionEvent} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XTransactionEvent toTransactionEvent(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XTRANSACTIONEVENT_ELEMENT);
		
		XAddress target = getTarget(xml, context);
		
		if(target.getField() != null || (target.getModel() == null && target.getObject() == null))
			throw new IllegalArgumentException("TransactionEvent element " + xml
			        + " does not specify a model or object target.");
		
		long objectRev = getRevision(xml, XFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, target
		        .getObject() != null);
		long modelRev = getRevision(xml, XFIELDEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, target
		        .getObject() == null);
		
		XID actor = getXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toAtomicEvent(event, target));
		}
		
		return MemoryTransactionEvent.createTransactionEvent(actor, target, events, modelRev,
		        objectRev);
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML.
	 * @return The {@link XFieldEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XFieldEvent toFieldEvent(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XFIELDEVENT_ELEMENT);
		
		XAddress target = getTarget(xml, context);
		
		ChangeType type = getTypeAttribute(xml, XFIELDEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XFIELDEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE, true);
		long objectRev = getRevision(xml, XFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, false);
		long modelRev = getRevision(xml, XFIELDEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, false);
		
		XID actor = getXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		XValue oldValue = null;
		XValue newValue = null;
		Iterator<MiniElement> it = xml.getElements();
		if(type != ChangeType.ADD) {
			if(!it.hasNext())
				throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT
				        + "> is missing it's oldValue child element");
			MiniElement valueElement = it.next();
			oldValue = XmlValue.toValue(valueElement);
		}
		if(type != ChangeType.REMOVE) {
			if(!it.hasNext())
				throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT
				        + "> is missing it's oldValue child element");
			MiniElement valueElement = it.next();
			newValue = XmlValue.toValue(valueElement);
		}
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XFIELDEVENT_ELEMENT + ">: <"
			        + it.next().getName() + ">");
		
		if(type == ChangeType.ADD)
			return MemoryFieldEvent.createAddEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		else if(type == ChangeType.CHANGE)
			return MemoryFieldEvent.createChangeEvent(actor, target, oldValue, newValue, modelRev,
			        objectRev, fieldRev, inTransaction);
		else if(type == ChangeType.REMOVE)
			return MemoryFieldEvent.createRemoveEvent(actor, target, oldValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		else
			throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model and object to fill in if
	 *            not specified in the XML.
	 * @return The {@link XObjectEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XObjectEvent toObjectEvent(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XOBJECTEVENT_ELEMENT);
		
		if(context.getField() != null)
			throw new IllegalArgumentException("invalid context for object events: " + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + OBJECTID_ATTRIBUTE + " is missing");
		
		if(address.getField() == null)
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + FIELDID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XOBJECTEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XOBJECTEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		long objectRev = getRevision(xml, XOBJECTEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, true);
		long modelRev = getRevision(xml, XOBJECTEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, false);
		
		XID actor = getXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		checkHasNoChildren(xml, XOBJECTEVENT_ELEMENT);
		
		XAddress target = address.getParent();
		XID fieldId = address.getField();
		
		if(type == ChangeType.ADD)
			return MemoryObjectEvent.createAddEvent(actor, target, fieldId, modelRev, objectRev,
			        inTransaction);
		else if(type == ChangeType.REMOVE)
			return MemoryObjectEvent.createRemoveEvent(actor, target, fieldId, modelRev, objectRev,
			        fieldRev, inTransaction);
		else
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for object events, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository and model to fill in if not
	 *            specified in the XML.
	 * @return The {@link XModelEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XModelEvent toModelEvent(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XMODELEVENT_ELEMENT);
		
		if(context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for model events: " + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@" + MODELID_ATTRIBUTE
			        + " is missing");
		
		if(address.getObject() == null)
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@"
			        + OBJECTID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XMODELEVENT_ELEMENT);
		
		long objectRev = getRevision(xml, XMODELEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		long modelRev = getRevision(xml, XMODELEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, true);
		
		XID actor = getXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		checkHasNoChildren(xml, XMODELEVENT_ELEMENT);
		
		XAddress target = address.getParent();
		XID objectId = address.getObject();
		
		if(type == ChangeType.ADD)
			return MemoryModelEvent
			        .createAddEvent(actor, target, objectId, modelRev, inTransaction);
		else if(type == ChangeType.REMOVE)
			return MemoryModelEvent.createRemoveEvent(actor, target, objectId, modelRev, objectRev,
			        inTransaction);
		else
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type for model events, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XID of the repository to fill in if not specified in
	 *            the XML.
	 * @return The {@link XRepositoryEvent} represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XRepositoryEvent toRepositoryEvent(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XREPOSITORYEVENT_ELEMENT);
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for model events: " + context);
		
		XAddress address = getTarget(xml, context);
		
		if(address.getRepository() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + REPOSITORYID_ATTRIBUTE + " is missing");
		
		if(address.getModel() == null)
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + MODELID_ATTRIBUTE + " is missing");
		
		ChangeType type = getTypeAttribute(xml, XREPOSITORYEVENT_ELEMENT);
		
		long modelRev = getRevision(xml, XREPOSITORYEVENT_ELEMENT, MODELREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		
		XID actor = getXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext())
			throw new IllegalArgumentException("Invalid child of <" + XREPOSITORYEVENT_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		
		XAddress target = address.getParent();
		XID modelId = address.getModel();
		
		if(type == ChangeType.ADD)
			return MemoryRepositoryEvent.createAddEvent(actor, target, modelId);
		else if(type == ChangeType.REMOVE)
			return MemoryRepositoryEvent.createRemoveEvent(actor, target, modelId, modelRev);
		else
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository events, but '" + type + "'");
	}
	
	/**
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML. The context for the
	 *            events contained in the transaction will be given by the
	 *            transaction.
	 * @return The {@link List} of {@link XEvent}s represented by the given XML
	 *         element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event list.
	 * 
	 */
	public static List<XEvent> toEventList(MiniElement xml, XAddress context) {
		
		checkElementName(xml, XEVENTLIST_ELEMENT);
		
		List<XEvent> events = new ArrayList<XEvent>();
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toEvent(event, context));
		}
		
		return events;
	}
	
	public static void toXml(XEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		if(event == null) {
			out.open(XNULL_ELEMENT);
			out.close(XNULL_ELEMENT);
		} else if(event instanceof XTransactionEvent) {
			toXml((XTransactionEvent)event, out, context);
		} else if(event instanceof XFieldEvent) {
			toXml((XFieldEvent)event, out, context);
		} else if(event instanceof XObjectEvent) {
			toXml((XObjectEvent)event, out, context);
		} else if(event instanceof XModelEvent) {
			toXml((XModelEvent)event, out, context);
		} else if(event instanceof XRepositoryEvent) {
			toXml((XRepositoryEvent)event, out, context);
		} else {
			throw new RuntimeException("event " + event + " is of unexpected type: "
			        + event.getClass());
		}
	}
	
	public static void toXml(XTransactionEvent trans, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTIONEVENT_ELEMENT);
		
		setBasicAttributes(trans, out, context);
		
		XAddress newContext = trans.getTarget();
		
		for(XAtomicEvent event : trans) {
			toXml(event, out, newContext);
		}
		
		out.close(XTRANSACTIONEVENT_ELEMENT);
		
	}
	
	public static void toXml(XFieldEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XFIELDEVENT_ELEMENT);
		
		setEventAttributes(event, out, context);
		
		if(event.getChangeType() != ChangeType.ADD)
			XmlValue.toXml(event.getOldValue(), out);
		if(event.getChangeType() != ChangeType.REMOVE)
			XmlValue.toXml(event.getNewValue(), out);
		
		out.close(XFIELDEVENT_ELEMENT);
		
	}
	
	public static void toXml(XObjectEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getField() != null)
			throw new IllegalArgumentException("invalid context for object events: " + context);
		
		out.open(XOBJECTEVENT_ELEMENT);
		
		setEventAttributes(event, out, context);
		
		out.attribute(FIELDID_ATTRIBUTE, event.getFieldID().toString());
		
		out.close(XOBJECTEVENT_ELEMENT);
		
	}
	
	public static void toXml(XModelEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for model events: " + context);
		
		out.open(XMODELEVENT_ELEMENT);
		
		setEventAttributes(event, out, context);
		
		out.attribute(OBJECTID_ATTRIBUTE, event.getObjectID().toString());
		
		out.close(XMODELEVENT_ELEMENT);
		
	}
	
	public static void toXml(XRepositoryEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null)
			throw new IllegalArgumentException("invalid context for repository events: " + context);
		
		out.open(XREPOSITORYEVENT_ELEMENT);
		
		setEventAttributes(event, out, context);
		
		out.attribute(MODELID_ATTRIBUTE, event.getModelID().toString());
		
		out.close(XREPOSITORYEVENT_ELEMENT);
		
	}
	
	public static void toXml(Iterator<XEvent> events, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XEVENTLIST_ELEMENT);
		
		while(events.hasNext()) {
			toXml(events.next(), out, context);
		}
		
		out.close(XEVENTLIST_ELEMENT);
		
	}
	
	private static void setBasicAttributes(XEvent event, XmlOut out, XAddress context) {
		
		XID repoId = event.getTarget().getRepository();
		if(repoId != null && (context == null || !XI.equals(context.getRepository(), repoId)))
			out.attribute(REPOSITORYID_ATTRIBUTE, repoId.toString());
		
		XID modelId = event.getTarget().getModel();
		if(modelId != null && (context == null || !XI.equals(context.getModel(), modelId)))
			out.attribute(MODELID_ATTRIBUTE, modelId.toString());
		
		XID objectId = event.getTarget().getObject();
		if(objectId != null && (context == null || !XI.equals(context.getObject(), objectId)))
			out.attribute(OBJECTID_ATTRIBUTE, objectId.toString());
		
		XID fieldId = event.getTarget().getField();
		if(fieldId != null && (context == null || !XI.equals(context.getField(), fieldId)))
			out.attribute(FIELDID_ATTRIBUTE, fieldId.toString());
		
		if(event.getActor() != null)
			out.attribute(ACTOR_ATTRIBUTE, event.getActor().toString());
		
		if(event.getModelRevisionNumber() != XEvent.RevisionOfEntityNotSet)
			out.attribute(MODELREVISION_ATTRIBUTE, Long.toString(event.getModelRevisionNumber()));
		
		if(event.getObjectRevisionNumber() != XEvent.RevisionOfEntityNotSet)
			out.attribute(OBJECTREVISION_ATTRIBUTE, Long.toString(event.getObjectRevisionNumber()));
		
		if(event.getFieldRevisionNumber() != XEvent.RevisionOfEntityNotSet)
			out.attribute(FIELDREVISION_ATTRIBUTE, Long.toString(event.getFieldRevisionNumber()));
		
	}
	
	private static void setEventAttributes(XAtomicEvent event, XmlOut out, XAddress context) {
		
		out.attribute(TYPE_ATTRIBUTE, event.getChangeType().toString());
		
		setBasicAttributes(event, out, context);
		
		if(event.inTransaction())
			out.attribute(INTRANSACTION_ATTRIBUTE, Boolean.toString(true));
		
	}
	
}
