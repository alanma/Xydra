package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
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
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
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
	protected static final String XFIELDEVENT_ELEMENT = "xfieldEvent";
	private static final String XTRANSACTIONEVENT_ELEMENT = "xtransactionEvent";
	private static final String XEVENTLIST_ELEMENT = "xevents";
	private static final String XNULL_ELEMENT = "xnull";
	
	private static final String FIELDREVISION_ATTRIBUTE = "fieldRevision";
	private static final String OBJECTREVISION_ATTRIBUTE = "objectRevision";
	private static final String MODELREVISION_ATTRIBUTE = "modelRevision";
	private static final String INTRANSACTION_ATTRIBUTE = "inTransaction";
	private static final String ACTOR_ATTRIBUTE = "actor";
	
	private static long getRevision(MiniElement xml, String elementName, String attribute,
	        boolean required) {
		
		String revisionString = xml.getAttribute(attribute);
		
		if(revisionString == null) {
			
			if(required) {
				throw new IllegalArgumentException("<" + elementName + ">@" + attribute
				        + " attribute is missing");
			}
			
			return XEvent.RevisionOfEntityNotSet;
		}
		
		try {
			return Long.parseLong(revisionString);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("<" + elementName + ">@" + attribute
			        + " does not contain a long, but '" + revisionString + "'");
		}
	}
	
	private static boolean getInTransactionAttribute(MiniElement xml) {
		String booleanString = xml.getAttribute(INTRANSACTION_ATTRIBUTE);
		return Boolean.parseBoolean(booleanString);
	}
	
	/**
	 * Get the {@link XEvent} represented by the given XML element.
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
		return toEvent(xml, context, null);
	}
	
	protected static XEvent toEvent(MiniElement xml, XAddress context, XChangeLogState cl)
	        throws IllegalArgumentException {
		String name = xml.getName();
		if(name.equals(XTRANSACTIONEVENT_ELEMENT)) {
			return toTransactionEvent(xml, context, cl);
		} else {
			return toAtomicEvent(xml, context, cl);
		}
	}
	
	/**
	 * Get the {@link XAtomicEvent} represented by the given XML element.
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
		return toAtomicEvent(xml, context, null);
	}
	
	protected static XAtomicEvent toAtomicEvent(MiniElement xml, XAddress context,
	        XChangeLogState cl) throws IllegalArgumentException {
		String name = xml.getName();
		if(name.equals(XFIELDEVENT_ELEMENT)) {
			return toFieldEvent(xml, context, cl);
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
	 * Get the {@link XTransactionEvent} represented by the given XML element.
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
		return toTransactionEvent(xml, context, null);
	}
	
	protected static XTransactionEvent toTransactionEvent(MiniElement xml, XAddress context,
	        XChangeLogState cl) {
		
		XmlUtils.checkElementName(xml, XTRANSACTIONEVENT_ELEMENT);
		
		XAddress target = XmlUtils.getTarget(xml, context);
		
		if(target.getField() != null || (target.getModel() == null && target.getObject() == null)) {
			throw new IllegalArgumentException("TransactionEvent element " + xml
			        + " does not specify a model or object target.");
		}
		
		long objectRev = getRevision(xml, XFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, target
		        .getObject() != null);
		long modelRev = getRevision(xml, XFIELDEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, target
		        .getObject() == null);
		
		XID actor = XmlUtils.getOptionalXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toAtomicEvent(event, target, cl));
		}
		
		return MemoryTransactionEvent.createTransactionEvent(actor, target, events, modelRev,
		        objectRev);
	}
	
	/**
	 * Get the {@link XFieldEvent} represented by the given XML element.
	 * 
	 * @param context The XIDs of the repository, model, object and field to
	 *            fill in if not specified in the XML.
	 * @return The {@link XFieldEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XFieldEvent toFieldEvent(MiniElement xml, XAddress context) {
		return toFieldEvent(xml, context, null);
	}
	
	protected static XFieldEvent toFieldEvent(MiniElement xml, XAddress context, XChangeLogState cl) {
		
		XmlUtils.checkElementName(xml, XFIELDEVENT_ELEMENT);
		
		XAddress target = XmlUtils.getTarget(xml, context);
		
		ChangeType type = XmlUtils.getChangeType(xml, XFIELDEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XFIELDEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE, true);
		long objectRev = getRevision(xml, XFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, false);
		long modelRev = getRevision(xml, XFIELDEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, false);
		
		XID actor = XmlUtils.getOptionalXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		XValue oldValue = null;
		XValue newValue = null;
		Iterator<MiniElement> it = xml.getElements();
		boolean loadedOldValueFromChangeLog = false;
		if(type != ChangeType.ADD) {
			// try to get the old value from a previous event
			if(cl != null && fieldRev > cl.getFirstRevisionNumber()
			        && fieldRev <= cl.getCurrentRevisionNumber()) {
				XEvent oldEvent = cl.getEvent(fieldRev - 1);
				oldValue = getOldValue(oldEvent, target);
				loadedOldValueFromChangeLog = true;
			} else {
				oldValue = loadValue(it, "old");
			}
			assert oldValue != null;
		}
		if(type != ChangeType.REMOVE) {
			newValue = loadValue(it, "new");
			assert newValue != null;
			// if the oldValue was present in the serialized event, don't die
			if(loadedOldValueFromChangeLog && it.hasNext()) {
				assert XI.equals(oldValue, newValue);
				oldValue = newValue;
				newValue = loadValue(it, "new");
			}
		}
		if(it.hasNext()) {
			throw new IllegalArgumentException("Invalid child of <" + XFIELDEVENT_ELEMENT + ">: <"
			        + it.next().getName() + ">");
		}
		
		if(type == ChangeType.ADD) {
			return MemoryFieldEvent.createAddEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else if(type == ChangeType.CHANGE) {
			return MemoryFieldEvent.createChangeEvent(actor, target, oldValue, newValue, modelRev,
			        objectRev, fieldRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			return MemoryFieldEvent.createRemoveEvent(actor, target, oldValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else {
			throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
		}
	}
	
	private static XValue loadValue(Iterator<MiniElement> it, String type) {
		if(!it.hasNext()) {
			throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT + "> is missing it's "
			        + type + "Value child element");
		}
		MiniElement valueElement = it.next();
		return XmlValue.toValue(valueElement);
	}
	
	/**
	 * Get the {@link XObjectEvent} represented by the given XML element.
	 * 
	 * @param context The XIDs of the repository, model and object to fill in if
	 *            not specified in the XML.
	 * @return The {@link XObjectEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XObjectEvent toObjectEvent(MiniElement xml, XAddress context) {
		
		XmlUtils.checkElementName(xml, XOBJECTEVENT_ELEMENT);
		
		if(context.getField() != null) {
			throw new IllegalArgumentException("invalid context for object events: " + context);
		}
		
		XAddress address = XmlUtils.getTarget(xml, context);
		
		if(address.getObject() == null) {
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + XmlUtils.OBJECTID_ATTRIBUTE + " is missing");
		}
		
		if(address.getField() == null) {
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + XmlUtils.FIELDID_ATTRIBUTE + " is missing");
		}
		
		ChangeType type = XmlUtils.getChangeType(xml, XOBJECTEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XOBJECTEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		long objectRev = getRevision(xml, XOBJECTEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, true);
		long modelRev = getRevision(xml, XOBJECTEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, false);
		
		XID actor = XmlUtils.getOptionalXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		XmlUtils.checkHasNoChildren(xml, XOBJECTEVENT_ELEMENT);
		
		XAddress target = address.getParent();
		XID fieldId = address.getField();
		
		if(type == ChangeType.ADD) {
			return MemoryObjectEvent.createAddEvent(actor, target, fieldId, modelRev, objectRev,
			        inTransaction);
		} else if(type == ChangeType.REMOVE) {
			return MemoryObjectEvent.createRemoveEvent(actor, target, fieldId, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else {
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for object events, but '" + type + "'");
		}
	}
	
	/**
	 * Get the {@link XModelEvent} represented by the given XML element.
	 * 
	 * @param context The XIDs of the repository and model to fill in if not
	 *            specified in the XML.
	 * @return The {@link XModelEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XModelEvent toModelEvent(MiniElement xml, XAddress context) {
		
		XmlUtils.checkElementName(xml, XMODELEVENT_ELEMENT);
		
		if(context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		XAddress address = XmlUtils.getTarget(xml, context);
		
		if(address.getModel() == null) {
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@"
			        + XmlUtils.MODELID_ATTRIBUTE + " is missing");
		}
		
		if(address.getObject() == null) {
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@"
			        + XmlUtils.OBJECTID_ATTRIBUTE + " is missing");
		}
		
		ChangeType type = XmlUtils.getChangeType(xml, XMODELEVENT_ELEMENT);
		
		long objectRev = getRevision(xml, XMODELEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		long modelRev = getRevision(xml, XMODELEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, true);
		
		XID actor = XmlUtils.getOptionalXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		boolean inTransaction = getInTransactionAttribute(xml);
		
		XmlUtils.checkHasNoChildren(xml, XMODELEVENT_ELEMENT);
		
		XAddress target = address.getParent();
		XID objectId = address.getObject();
		
		if(type == ChangeType.ADD) {
			return MemoryModelEvent
			        .createAddEvent(actor, target, objectId, modelRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			return MemoryModelEvent.createRemoveEvent(actor, target, objectId, modelRev, objectRev,
			        inTransaction);
		} else {
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for model events, but '" + type + "'");
		}
	}
	
	/**
	 * Get the {@link XRepositoryEvent} represented by the given XML element.
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
		
		XmlUtils.checkElementName(xml, XREPOSITORYEVENT_ELEMENT);
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		XAddress address = XmlUtils.getTarget(xml, context);
		
		if(address.getRepository() == null) {
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + XmlUtils.REPOSITORYID_ATTRIBUTE + " is missing");
		}
		
		if(address.getModel() == null) {
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + XmlUtils.MODELID_ATTRIBUTE + " is missing");
		}
		
		ChangeType type = XmlUtils.getChangeType(xml, XREPOSITORYEVENT_ELEMENT);
		
		long modelRev = getRevision(xml, XREPOSITORYEVENT_ELEMENT, MODELREVISION_ATTRIBUTE,
		        type == ChangeType.REMOVE);
		
		XID actor = XmlUtils.getOptionalXidAttribute(xml, ACTOR_ATTRIBUTE, null);
		
		Iterator<MiniElement> it = xml.getElements();
		if(it.hasNext()) {
			throw new IllegalArgumentException("Invalid child of <" + XREPOSITORYEVENT_ELEMENT
			        + ">: <" + it.next().getName() + ">");
		}
		
		XAddress target = address.getParent();
		XID modelId = address.getModel();
		
		if(type == ChangeType.ADD) {
			return MemoryRepositoryEvent.createAddEvent(actor, target, modelId);
		} else if(type == ChangeType.REMOVE) {
			return MemoryRepositoryEvent.createRemoveEvent(actor, target, modelId, modelRev);
		} else {
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository events, but '" + type + "'");
		}
	}
	
	/**
	 * Get the {@link XEvent} list represented by the given XML element.
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
		
		XmlUtils.checkElementName(xml, XEVENTLIST_ELEMENT);
		
		List<XEvent> events = new ArrayList<XEvent>();
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toEvent(event, context));
		}
		
		return events;
	}
	
	/**
	 * Encode the given {@link XEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		toXml(event, out, context, null);
	}
	
	protected static void toXml(XEvent event, XmlOut out, XAddress context, XChangeLog cl)
	        throws IllegalArgumentException {
		if(event == null) {
			out.open(XNULL_ELEMENT);
			out.close(XNULL_ELEMENT);
		} else if(event instanceof XTransactionEvent) {
			toXml((XTransactionEvent)event, out, context, cl);
		} else if(event instanceof XFieldEvent) {
			toXml((XFieldEvent)event, out, context, cl);
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
	
	/**
	 * Encode the given {@link XTransactionEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XTransactionEvent trans, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		toXml(trans, out, context, null);
	}
	
	public static void toXml(XTransactionEvent trans, XmlOut out, XAddress context, XChangeLog cl)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTIONEVENT_ELEMENT);
		
		setCommonAttributes(trans, out, context);
		
		XAddress newContext = trans.getTarget();
		
		for(XAtomicEvent event : trans) {
			toXml(event, out, newContext, cl);
		}
		
		out.close(XTRANSACTIONEVENT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XFieldEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XFieldEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context);
		
		if(event.getChangeType() != ChangeType.ADD) {
			XmlValue.toXml(event.getOldValue(), out);
		}
		if(event.getChangeType() != ChangeType.REMOVE) {
			XmlValue.toXml(event.getNewValue(), out);
		}
		
		out.close(XFIELDEVENT_ELEMENT);
		
	}
	
	protected static void toXml(XFieldEvent event, XmlOut out, XAddress context, XChangeLog cl)
	        throws IllegalArgumentException {
		
		out.open(XFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context);
		
		if(event.getChangeType() != ChangeType.ADD) {
			// don't save the old value if it is already saved in an old event
			long fieldRev = event.getFieldRevisionNumber();
			if(cl != null && fieldRev > cl.getFirstRevisionNumber()
			        && fieldRev <= cl.getCurrentRevisionNumber()) {
				XEvent oldEvent = cl.getEventAt(fieldRev - 1);
				XValue oldValue = getOldValue(oldEvent, event.getTarget());
				assert XI.equals(event.getOldValue(), oldValue);
			} else {
				XmlValue.toXml(event.getOldValue(), out);
			}
		}
		if(event.getChangeType() != ChangeType.REMOVE) {
			XmlValue.toXml(event.getNewValue(), out);
		}
		
		out.close(XFIELDEVENT_ELEMENT);
		
	}
	
	private static XValue getOldValue(XEvent oldEvent, XAddress target) {
		
		if(oldEvent instanceof XFieldEvent) {
			
			ChangeType ct = oldEvent.getChangeType();
			if(!target.equals(oldEvent.getTarget())
			        || (ct != ChangeType.ADD && ct != ChangeType.CHANGE)) {
				throw new RuntimeException("error de-serializing XFieldEvent (target=" + target
				        + ") in change log: fieldRev refers to non-matching event: " + oldEvent);
			}
			
			return ((XFieldEvent)oldEvent).getNewValue();
			
		} else if(oldEvent instanceof XTransactionEvent) {
			XTransactionEvent te = (XTransactionEvent)oldEvent;
			for(int i = te.size() - 1; i > 0; i--) {
				XAtomicEvent ae = te.getEvent(i);
				if(ae instanceof XFieldEvent && target.equals(ae.getTarget())) {
					ChangeType ct = ae.getChangeType();
					if((ct != ChangeType.ADD && ct != ChangeType.CHANGE)) {
						throw new RuntimeException("error de-serializing XFieldEvent (target="
						        + target
						        + ") in change log: fieldRev refers to non-matching event: "
						        + oldEvent);
					}
					return ((XFieldEvent)ae).getNewValue();
				}
			}
			
		}
		
		throw new RuntimeException("error de-serializing XFieldEvent (target=" + target
		        + ") in change log: fieldRev refers to non-matching event: " + oldEvent);
	}
	
	/**
	 * Encode the given {@link XObjectEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XObjectEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getField() != null) {
			throw new IllegalArgumentException("invalid context for object events: " + context);
		}
		
		out.open(XOBJECTEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context);
		
		out.attribute(XmlUtils.FIELDID_ATTRIBUTE, event.getFieldID().toString());
		
		out.close(XOBJECTEVENT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XModelEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XModelEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		out.open(XMODELEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context);
		
		out.attribute(XmlUtils.OBJECTID_ATTRIBUTE, event.getObjectID().toString());
		
		out.close(XMODELEVENT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XRepositoryEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XRepositoryEvent event, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		if(context.getModel() != null || context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for repository events: " + context);
		}
		
		out.open(XREPOSITORYEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context);
		
		out.attribute(XmlUtils.MODELID_ATTRIBUTE, event.getModelID().toString());
		
		out.close(XREPOSITORYEVENT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XEvent} list as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(Iterator<XEvent> events, XmlOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XEVENTLIST_ELEMENT);
		
		while(events.hasNext()) {
			toXml(events.next(), out, context);
		}
		
		out.close(XEVENTLIST_ELEMENT);
		
	}
	
	/**
	 * Encode attributes common to all event types.
	 */
	private static void setCommonAttributes(XEvent event, XmlOut out, XAddress context) {
		
		XmlUtils.setTarget(event.getTarget(), out, context);
		
		if(event.getActor() != null) {
			out.attribute(ACTOR_ATTRIBUTE, event.getActor().toString());
		}
		
		if(event.getModelRevisionNumber() != XEvent.RevisionOfEntityNotSet) {
			out.attribute(MODELREVISION_ATTRIBUTE, Long.toString(event.getModelRevisionNumber()));
		}
		
		if(event.getObjectRevisionNumber() != XEvent.RevisionOfEntityNotSet) {
			out.attribute(OBJECTREVISION_ATTRIBUTE, Long.toString(event.getObjectRevisionNumber()));
		}
		
		if(event.getFieldRevisionNumber() != XEvent.RevisionOfEntityNotSet) {
			out.attribute(FIELDREVISION_ATTRIBUTE, Long.toString(event.getFieldRevisionNumber()));
		}
		
	}
	
	private static void setAtomicEventAttributes(XAtomicEvent event, XmlOut out, XAddress context) {
		
		out.attribute(XmlUtils.TYPE_ATTRIBUTE, event.getChangeType().toString());
		
		setCommonAttributes(event, out, context);
		
		if(event.inTransaction()) {
			out.attribute(INTRANSACTION_ATTRIBUTE, Boolean.toString(true));
		}
		
	}
	
}
