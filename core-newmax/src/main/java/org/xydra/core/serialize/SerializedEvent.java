package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryReversibleFieldEvent;
import org.xydra.base.change.impl.memory.MemoryTransactionEvent;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;


/**
 * Collection of methods to (de-)serialize variants of {@link XEvent} to and
 * from their XML/JSON representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedEvent {
	
	private static final String NAME_OLD_VALUE = "oldValue";
	private static final String NAME_VALUE = "value";
	private static final String NAME_EVENTS = "events";
	private static final String ACTOR_ATTRIBUTE = "actor";
	private static final String FIELDREVISION_ATTRIBUTE = "fieldRevision";
	private static final String IMPLIED_ATTRIBUTE = "implied";
	private static final String INTRANSACTION_ATTRIBUTE = "inTransaction";
	private static final String MODELREVISION_ATTRIBUTE = "modelRevision";
	private static final String OBJECTREVISION_ATTRIBUTE = "objectRevision";
	
	public static final String XEVENTLIST_ELEMENT = "xevents";
	public static final String XFIELDEVENT_ELEMENT = "xfieldEvent";
	public static final String XREVERSIBLEFIELDEVENT_ELEMENT = "xreversibleFieldEvent";
	public static final String XMODELEVENT_ELEMENT = "xmodelEvent";
	public static final String XOBJECTEVENT_ELEMENT = "xobjectEvent";
	public static final String XREPOSITORYEVENT_ELEMENT = "xrepositoryEvent";
	public static final String XTRANSACTIONEVENT_ELEMENT = "xtransactionEvent";
	
	private static boolean getImpliedAttribute(XydraElement element) {
		Object booleanString = element.getAttribute(IMPLIED_ATTRIBUTE);
		return booleanString == null ? false : SerializingUtils.toBoolean(booleanString);
	}
	
	private static boolean getInTransactionAttribute(XydraElement element) {
		Object booleanString = element.getAttribute(INTRANSACTION_ATTRIBUTE);
		return booleanString == null ? false : SerializingUtils.toBoolean(booleanString);
	}
	
	private static long getRevision(XydraElement element, String attribute, boolean required) {
		
		Object revisionString = element.getAttribute(attribute);
		
		if(revisionString == null) {
			if(required) {
				throw new ParsingError(element, "Missing attribute '" + attribute + "'");
			}
			return XEvent.REVISION_OF_ENTITY_NOT_SET;
		}
		
		return SerializingUtils.toLong(revisionString);
	}
	
	private static void setAtomicEventAttributes(XAtomicEvent event, XydraOut out,
	        XAddress context, boolean inTrans) {
		
		out.attribute(SerializingUtils.TYPE_ATTRIBUTE, event.getChangeType());
		
		setCommonAttributes(event, out, context, inTrans);
		
		if(!inTrans && event.inTransaction()) {
			out.attribute(INTRANSACTION_ATTRIBUTE, true);
		}
		
		if(event.isImplied()) {
			out.attribute(IMPLIED_ATTRIBUTE, true);
		}
		
	}
	
	/**
	 * Encode attributes common to all event types.
	 */
	private static void setCommonAttributes(XEvent event, XydraOut out, XAddress context,
	        boolean inTrans) {
		
		SerializingUtils.setAddress(event.getChangedEntity(), out, context);
		
		if(!inTrans) {
			
			if(event.getActor() != null) {
				out.attribute(ACTOR_ATTRIBUTE, event.getActor());
			}
			
			if(event.getOldModelRevision() != XEvent.REVISION_OF_ENTITY_NOT_SET) {
				// FIXME here is the problem
				out.attribute(MODELREVISION_ATTRIBUTE, event.getOldModelRevision());
			}
			
		}
		
		if(event.getOldObjectRevision() != XEvent.REVISION_OF_ENTITY_NOT_SET) {
			out.attribute(OBJECTREVISION_ATTRIBUTE, event.getOldObjectRevision());
		}
		
		if(event.getOldFieldRevision() != XEvent.REVISION_OF_ENTITY_NOT_SET) {
			out.attribute(FIELDREVISION_ATTRIBUTE, event.getOldFieldRevision());
		}
		
	}
	
	private static XAtomicEvent toAtomicEvent(XydraElement element, XAddress context,
	        TempTrans trans) throws ParsingError {
		String name = element.getType();
		if(name.equals(XFIELDEVENT_ELEMENT)) {
			return toFieldEvent(element, context, trans);
		} else if(name.equals(XREVERSIBLEFIELDEVENT_ELEMENT)) {
			return toReversibleFieldEvent(element, context, trans);
		} else if(name.equals(XOBJECTEVENT_ELEMENT)) {
			return toObjectEvent(element, context, trans);
		} else if(name.equals(XMODELEVENT_ELEMENT)) {
			return toModelEvent(element, context, trans);
		} else if(name.equals(XREPOSITORYEVENT_ELEMENT)) {
			return toRepositoryEvent(element, context, trans);
		} else {
			throw new ParsingError(element, "Unexpected event element.");
		}
	}
	
	/**
	 * Get the {@link XEvent} represented by the given XML/JSON element.
	 * 
	 * @param element
	 * 
	 * @param context The {@link XId XIds} of the repository, model, object and
	 *            field to fill in if not specified in the XML/JSON. If the
	 *            given element represents a transaction, the context for the
	 *            contained events will be given by the transaction.
	 * @return The {@link XEvent} represented by the given XML/JSON element.
	 * @throws ParsingError if the XML/JSON element does not represent a valid
	 *             event.
	 * 
	 */
	public static XEvent toEvent(XydraElement element, XAddress context) throws ParsingError {
		if(element == null) {
			return null;
		} else if(element.getType().equals(XTRANSACTIONEVENT_ELEMENT)) {
			return toTransactionEvent(element, context);
		} else {
			return toAtomicEvent(element, context, null);
		}
	}
	
	/**
	 * Get the {@link XEvent} list represented by the given XML/JSON element.
	 * 
	 * @param element
	 * 
	 * @param context The {@link XId XIds} of the repository, model, object and
	 *            field to fill in if not specified in the XML/JSON. The context
	 *            for the events contained in the transaction will be given by
	 *            the transaction.
	 * @return The {@link List} of {@link XEvent}s represented by the given
	 *         XML/JSON element.
	 * @throws ParsingError if the XML element does not represent a valid event
	 *             list.
	 * 
	 */
	public static List<XEvent> toEventList(XydraElement element, XAddress context) {
		
		SerializingUtils.checkElementType(element, XEVENTLIST_ELEMENT);
		
		List<XEvent> events = new ArrayList<XEvent>();
		Iterator<XydraElement> it = element.getChildrenByName(NAME_EVENTS);
		while(it.hasNext()) {
			XydraElement event = it.next();
			events.add(toEvent(event, context));
		}
		
		return events;
	}
	
	private static XFieldEvent toFieldEvent(XydraElement element, XAddress context, TempTrans trans) {
		
		XAddress target = SerializingUtils.getAddress(element, context);
		
		ChangeType type = SerializingUtils.getChangeType(element);
		
		long fieldRev = getRevision(element, FIELDREVISION_ATTRIBUTE, true);
		long objectRev = getRevision(element, OBJECTREVISION_ATTRIBUTE, false);
		long modelRev = trans != null ? trans.modelRev : getRevision(element,
		        MODELREVISION_ATTRIBUTE, false);
		
		XId actor = trans != null ? trans.actor : SerializingUtils.getOptionalXidAttribute(element,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(element);
		
		// XValue oldValue = null;
		XValue newValue = null;
		if(type != ChangeType.REMOVE) {
			newValue = SerializedValue.toValue(element.getElement(NAME_VALUE, 0));
			XyAssert.xyAssert(newValue != null);
			assert newValue != null;
		}
		
		if(type == ChangeType.ADD) {
			return MemoryFieldEvent.createAddEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else if(type == ChangeType.CHANGE) {
			return MemoryFieldEvent.createChangeEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(element);
			return MemoryFieldEvent.createRemoveEvent(actor, target, modelRev, objectRev, fieldRev,
			        inTransaction, implied);
		} else {
			throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
		}
	}
	
	private static XReversibleFieldEvent toReversibleFieldEvent(XydraElement element,
	        XAddress context, TempTrans trans) {
		
		XAddress target = SerializingUtils.getAddress(element, context);
		
		ChangeType type = SerializingUtils.getChangeType(element);
		
		long fieldRev = getRevision(element, FIELDREVISION_ATTRIBUTE, true);
		long objectRev = getRevision(element, OBJECTREVISION_ATTRIBUTE, false);
		long modelRev = trans != null ? trans.modelRev : getRevision(element,
		        MODELREVISION_ATTRIBUTE, false);
		
		XId actor = trans != null ? trans.actor : SerializingUtils.getOptionalXidAttribute(element,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(element);
		
		XValue oldValue = null;
		XValue newValue = null;
		int idx = 0;
		if(type != ChangeType.ADD) {
			oldValue = SerializedValue.toValue(element.getElement(NAME_OLD_VALUE, idx));
			idx++;
			XyAssert.xyAssert(oldValue != null);
			assert oldValue != null;
		}
		if(type != ChangeType.REMOVE) {
			newValue = SerializedValue.toValue(element.getElement(NAME_VALUE, idx));
			XyAssert.xyAssert(newValue != null);
			assert newValue != null;
		}
		
		if(type == ChangeType.ADD) {
			return MemoryReversibleFieldEvent.createAddEvent(actor, target, newValue, modelRev,
			        objectRev, fieldRev, inTransaction);
		} else if(type == ChangeType.CHANGE) {
			return MemoryReversibleFieldEvent.createChangeEvent(actor, target, oldValue, newValue,
			        modelRev, objectRev, fieldRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(element);
			return MemoryReversibleFieldEvent.createRemoveEvent(actor, target, oldValue, modelRev,
			        objectRev, fieldRev, inTransaction, implied);
		} else {
			throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
		}
	}
	
	private static XModelEvent toModelEvent(XydraElement element, XAddress context, TempTrans trans) {
		
		if(context != null && (context.getObject() != null || context.getField() != null)) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		XAddress address = SerializingUtils.getAddress(element, context);
		
		if(address.getModel() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.MODELID_ATTRIBUTE);
		}
		
		if(address.getObject() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.OBJECTID_ATTRIBUTE);
		}
		
		ChangeType type = SerializingUtils.getChangeType(element);
		
		long objectRev = getRevision(element, OBJECTREVISION_ATTRIBUTE, type == ChangeType.REMOVE);
		long modelRev = trans != null ? trans.modelRev : getRevision(element,
		        MODELREVISION_ATTRIBUTE, true);
		
		XId actor = trans != null ? trans.actor : SerializingUtils.getOptionalXidAttribute(element,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(element);
		
		XAddress target = address.getParent();
		XId objectId = address.getObject();
		
		if(type == ChangeType.ADD) {
			return MemoryModelEvent
			        .createAddEvent(actor, target, objectId, modelRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(element);
			return MemoryModelEvent.createRemoveEvent(actor, target, objectId, modelRev, objectRev,
			        inTransaction, implied);
		} else {
			throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for model events, but '" + type + "'");
		}
	}
	
	private static XObjectEvent toObjectEvent(XydraElement element, XAddress context,
	        TempTrans trans) {
		
		if(context != null && context.getField() != null) {
			throw new IllegalArgumentException("invalid context for object events: " + context);
		}
		
		XAddress address = SerializingUtils.getAddress(element, context);
		
		if(address.getObject() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.OBJECTID_ATTRIBUTE);
		}
		
		if(address.getField() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.FIELDID_ATTRIBUTE);
		}
		
		ChangeType type = SerializingUtils.getChangeType(element);
		
		long fieldRev = getRevision(element, FIELDREVISION_ATTRIBUTE, type == ChangeType.REMOVE);
		long objectRev = getRevision(element, OBJECTREVISION_ATTRIBUTE, true);
		long modelRev = trans != null ? trans.modelRev : getRevision(element,
		        MODELREVISION_ATTRIBUTE, false);
		
		XId actor = trans != null ? trans.actor : SerializingUtils.getOptionalXidAttribute(element,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(element);
		
		XAddress target = address.getParent();
		XId fieldId = address.getField();
		
		if(type == ChangeType.ADD) {
			return MemoryObjectEvent.createAddEvent(actor, target, fieldId, modelRev, objectRev,
			        inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(element);
			return MemoryObjectEvent.createRemoveEvent(actor, target, fieldId, modelRev, objectRev,
			        fieldRev, inTransaction, implied);
		} else {
			throw new ParsingError(element, "Attribute " + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for object events, but '" + type + "'");
		}
	}
	
	private static XRepositoryEvent toRepositoryEvent(XydraElement element, XAddress context,
	        TempTrans trans) {
		
		if(context != null && (context.getObject() != null || context.getField() != null)) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		XAddress address = SerializingUtils.getAddress(element, context);
		
		if(address.getRepository() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.REPOSITORYID_ATTRIBUTE + " is missing");
		}
		
		if(address.getModel() == null) {
			throw new ParsingError(element, "Missing attribute "
			        + SerializingUtils.MODELID_ATTRIBUTE + " is missing");
		}
		
		ChangeType type = SerializingUtils.getChangeType(element);
		
		long modelRev = trans != null ? trans.modelRev : getRevision(element,
		        MODELREVISION_ATTRIBUTE, type == ChangeType.REMOVE);
		
		XId actor = trans != null ? trans.actor : SerializingUtils.getOptionalXidAttribute(element,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(element);
		
		XAddress target = address.getParent();
		XId modelId = address.getModel();
		
		if(type == ChangeType.ADD) {
			return MemoryRepositoryEvent.createAddEvent(actor, target, modelId, modelRev,
			        inTransaction);
		} else if(type == ChangeType.REMOVE) {
			return MemoryRepositoryEvent.createRemoveEvent(actor, target, modelId, modelRev,
			        inTransaction);
		} else {
			throw new ParsingError(element, "Atribute " + SerializingUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository events, but '" + type + "'");
		}
	}
	
	private static class TempTrans {
		
		final XId actor;
		final long modelRev;
		
		public TempTrans(XId actor, long modelRev) {
			this.actor = actor;
			this.modelRev = modelRev;
		}
		
	}
	
	private static XTransactionEvent toTransactionEvent(XydraElement element, XAddress context) {
		
		XAddress target = SerializingUtils.getAddress(element, context);
		
		if(target.getField() != null || (target.getModel() == null && target.getObject() == null)) {
			throw new ParsingError(element, "Missing model or object target.");
		}
		
		long objectRev = getRevision(element, OBJECTREVISION_ATTRIBUTE, target.getObject() != null);
		long modelRev = getRevision(element, MODELREVISION_ATTRIBUTE, target.getObject() == null);
		
		XId actor = SerializingUtils.getOptionalXidAttribute(element, ACTOR_ATTRIBUTE, null);
		
		TempTrans tt = new TempTrans(actor, modelRev);
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		Iterator<XydraElement> it = element.getChildrenByName(NAME_EVENTS);
		while(it.hasNext()) {
			XydraElement event = it.next();
			events.add(toAtomicEvent(event, target, tt));
		}
		
		return MemoryTransactionEvent.createTransactionEvent(actor, target, events, modelRev,
		        objectRev);
	}
	
	/**
	 * Encode the given {@link XEvent} list as an XML/JSON element.
	 * 
	 * @param events The events to encode.
	 * @param out The XML/JSON encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 * @throws IllegalArgumentException
	 */
	public static void serialize(Iterator<XEvent> events, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XEVENTLIST_ELEMENT);
		
		out.child(NAME_EVENTS);
		out.beginArray();
		while(events.hasNext()) {
			serialize(events.next(), out, context);
		}
		out.endArray();
		
		out.close(XEVENTLIST_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XEvent} as an XML/JSON element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML/JSON encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 * @throws IllegalArgumentException
	 */
	public static void serialize(XEvent event, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		if(event == null) {
			out.nullElement();
		} else if(event instanceof XTransactionEvent) {
			serialize((XTransactionEvent)event, out, context);
		} else {
			serialize((XAtomicEvent)event, out, context, false);
		}
	}
	
	private static void serialize(XAtomicEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		if(event instanceof XReversibleFieldEvent) {
			serialize((XReversibleFieldEvent)event, out, context, inTrans);
		} else if(event instanceof XFieldEvent) {
			serialize((XFieldEvent)event, out, context, inTrans);
		} else if(event instanceof XObjectEvent) {
			serialize((XObjectEvent)event, out, context, inTrans);
		} else if(event instanceof XModelEvent) {
			serialize((XModelEvent)event, out, context, inTrans);
		} else if(event instanceof XRepositoryEvent) {
			serialize((XRepositoryEvent)event, out, context, inTrans);
		} else {
			throw new RuntimeException("event " + event + " is of unexpected type: "
			        + event.getClass());
		}
	}
	
	private static void serialize(XFieldEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		
		out.open(XFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		if(event.getChangeType() != ChangeType.REMOVE) {
			out.child(NAME_VALUE);
			SerializedValue.serialize(event.getNewValue(), out);
		}
		
		out.close(XFIELDEVENT_ELEMENT);
		
	}
	
	private static void serialize(XReversibleFieldEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		
		out.open(XREVERSIBLEFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		if(event.getChangeType() != ChangeType.ADD) {
			out.child(NAME_OLD_VALUE);
			SerializedValue.serialize(event.getOldValue(), out);
		}
		if(event.getChangeType() != ChangeType.REMOVE) {
			out.child(NAME_VALUE);
			SerializedValue.serialize(event.getNewValue(), out);
		}
		
		out.close(XREVERSIBLEFIELDEVENT_ELEMENT);
		
	}
	
	private static void serialize(XModelEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		
		if(context != null && (context.getObject() != null || context.getField() != null)) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		out.open(XMODELEVENT_ELEMENT);
		setAtomicEventAttributes(event, out, context, inTrans);
		out.close(XMODELEVENT_ELEMENT);
		
	}
	
	private static void serialize(XObjectEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		
		if(context != null && context.getField() != null) {
			throw new IllegalArgumentException("invalid context for object events: " + context);
		}
		
		out.open(XOBJECTEVENT_ELEMENT);
		setAtomicEventAttributes(event, out, context, inTrans);
		out.close(XOBJECTEVENT_ELEMENT);
		
	}
	
	private static void serialize(XRepositoryEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		
		if(context != null && (context.getObject() != null || context.getField() != null)) {
			throw new IllegalArgumentException("invalid context for repository events: " + context);
		}
		
		out.open(XREPOSITORYEVENT_ELEMENT);
		setAtomicEventAttributes(event, out, context, inTrans);
		out.close(XREPOSITORYEVENT_ELEMENT);
		
	}
	
	private static void serialize(XTransactionEvent trans, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTIONEVENT_ELEMENT);
		
		setCommonAttributes(trans, out, context, false);
		
		XAddress newContext = trans.getTarget();
		
		out.child(NAME_EVENTS);
		out.beginArray();
		for(XAtomicEvent event : trans) {
			XyAssert.xyAssert(event != null);
			assert event != null;
			serialize(event, out, newContext, true);
		}
		out.endArray();
		
		out.close(XTRANSACTIONEVENT_ELEMENT);
		
	}
	
}
