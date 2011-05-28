package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
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


/**
 * Collection of methods to (de-)serialize variants of {@link XEvent} to and
 * from their XML representation.
 * 
 * @author dscharrer
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlEvent {
	
	private static final String NAME_VALUE = "value";
	private static final String NAME_VALUES = "values";
	private static final String NAME_EVENTS = "events";
	private static final String ACTOR_ATTRIBUTE = "actor";
	private static final String FIELDREVISION_ATTRIBUTE = "fieldRevision";
	private static final String IMPLIED_ATTRIBUTE = "implied";
	private static final String INTRANSACTION_ATTRIBUTE = "inTransaction";
	private static final String MODELREVISION_ATTRIBUTE = "modelRevision";
	private static final String OBJECTREVISION_ATTRIBUTE = "objectRevision";
	private static final String XEVENTLIST_ELEMENT = "xevents";
	
	protected static final String XFIELDEVENT_ELEMENT = "xfieldEvent";
	protected static final String XREVERSIBLEFIELDEVENT_ELEMENT = "xreversibleFieldEvent";
	private static final String XMODELEVENT_ELEMENT = "xmodelEvent";
	private static final String XOBJECTEVENT_ELEMENT = "xobjectEvent";
	private static final String XREPOSITORYEVENT_ELEMENT = "xrepositoryEvent";
	private static final String XTRANSACTIONEVENT_ELEMENT = "xtransactionEvent";
	
	private static boolean getImpliedAttribute(MiniElement xml) {
		Object booleanString = xml.getAttribute(IMPLIED_ATTRIBUTE);
		return booleanString == null ? false : XmlValue.toBoolean(booleanString);
	}
	
	private static boolean getInTransactionAttribute(MiniElement xml) {
		Object booleanString = xml.getAttribute(INTRANSACTION_ATTRIBUTE);
		return booleanString == null ? false : XmlValue.toBoolean(booleanString);
	}
	
	private static long getRevision(MiniElement xml, String elementName, String attribute,
	        boolean required) {
		
		Object revisionString = xml.getAttribute(attribute);
		
		if(revisionString == null) {
			
			if(required) {
				throw new IllegalArgumentException("<" + elementName + ">@" + attribute
				        + " attribute is missing");
			}
			
			return XEvent.RevisionOfEntityNotSet;
		}
		
		return XmlValue.toLong(revisionString);
	}
	
	private static XValue loadValue(Iterator<MiniElement> it, String type) {
		if(!it.hasNext()) {
			throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT + "> is missing it's "
			        + type + "Value child element");
		}
		MiniElement valueElement = it.next();
		return XmlValue.toValue(valueElement);
	}
	
	private static void setAtomicEventAttributes(XAtomicEvent event, XydraOut out,
	        XAddress context, boolean inTrans) {
		
		out.attribute(XmlUtils.TYPE_ATTRIBUTE, event.getChangeType());
		
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
		
		XmlUtils.setTarget(event.getTarget(), out, context);
		
		if(!inTrans) {
			
			if(event.getActor() != null) {
				out.attribute(ACTOR_ATTRIBUTE, event.getActor());
			}
			
			if(event.getOldModelRevision() != XEvent.RevisionOfEntityNotSet) {
				out.attribute(MODELREVISION_ATTRIBUTE, event.getOldModelRevision());
			}
			
		}
		
		if(event.getOldObjectRevision() != XEvent.RevisionOfEntityNotSet) {
			out.attribute(OBJECTREVISION_ATTRIBUTE, event.getOldObjectRevision());
		}
		
		if(event.getOldFieldRevision() != XEvent.RevisionOfEntityNotSet) {
			out.attribute(FIELDREVISION_ATTRIBUTE, event.getOldFieldRevision());
		}
		
	}
	
	private static XAtomicEvent toAtomicEvent(MiniElement xml, XAddress context, TempTrans trans)
	        throws IllegalArgumentException {
		String name = xml.getType();
		if(name.equals(XFIELDEVENT_ELEMENT)) {
			return toFieldEvent(xml, context, trans);
		} else if(name.equals(XREVERSIBLEFIELDEVENT_ELEMENT)) {
			return toReversibleFieldEvent(xml, context, trans);
		} else if(name.equals(XOBJECTEVENT_ELEMENT)) {
			return toObjectEvent(xml, context, trans);
		} else if(name.equals(XMODELEVENT_ELEMENT)) {
			return toModelEvent(xml, context, trans);
		} else if(name.equals(XREPOSITORYEVENT_ELEMENT)) {
			return toRepositoryEvent(xml, context, trans);
		} else {
			throw new IllegalArgumentException("Unexpected event element: <" + name + ">.");
		}
	}
	
	/**
	 * Get the {@link XEvent} represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. If the given
	 *            element represents a transaction, the context for the
	 *            contained events will be given by the transaction.
	 * @return The {@link XEvent} represented by the given XML element.
	 * @throws IllegalArgumentException if the XML element does not represent a
	 *             valid event.
	 * 
	 */
	public static XEvent toEvent(MiniElement xml, XAddress context) throws IllegalArgumentException {
		String name = xml.getType();
		if(XmlValue.isNullElement(xml)) {
			return null;
		} else if(name.equals(XTRANSACTIONEVENT_ELEMENT)) {
			return toTransactionEvent(xml, context);
		} else {
			return toAtomicEvent(xml, context, null);
		}
	}
	
	/**
	 * Get the {@link XEvent} list represented by the given XML element.
	 * 
	 * @param context The {@link XID XIDs} of the repository, model, object and
	 *            field to fill in if not specified in the XML. The context for
	 *            the events contained in the transaction will be given by the
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
		Iterator<MiniElement> it = xml.getChildren(NAME_EVENTS);
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toEvent(event, context));
		}
		
		return events;
	}
	
	private static XFieldEvent toFieldEvent(MiniElement xml, XAddress context, TempTrans trans) {
		
		XAddress target = XmlUtils.getTarget(xml, context);
		
		ChangeType type = XmlUtils.getChangeType(xml, XFIELDEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XFIELDEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE, true);
		long objectRev = getRevision(xml, XFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE, false);
		long modelRev = trans != null ? trans.modelRev : getRevision(xml, XFIELDEVENT_ELEMENT,
		        MODELREVISION_ATTRIBUTE, false);
		
		XID actor = trans != null ? trans.actor : XmlUtils.getOptionalXidAttribute(xml,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(xml);
		
		// XValue oldValue = null;
		XValue newValue = null;
		Iterator<MiniElement> it = xml.getChildren(NAME_VALUE);
		if(type != ChangeType.REMOVE) {
			newValue = loadValue(it, "new");
			assert newValue != null;
		}
		if(it.hasNext()) {
			throw new IllegalArgumentException("Invalid child of <" + XFIELDEVENT_ELEMENT + ">: <"
			        + it.next().getType() + ">");
		}
		
		if(type == ChangeType.ADD) {
			return MemoryFieldEvent.createAddEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else if(type == ChangeType.CHANGE) {
			return MemoryFieldEvent.createChangeEvent(actor, target, newValue, modelRev, objectRev,
			        fieldRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(xml);
			return MemoryFieldEvent.createRemoveEvent(actor, target, modelRev, objectRev, fieldRev,
			        inTransaction, implied);
		} else {
			throw new IllegalArgumentException("<" + XFIELDEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
		}
	}
	
	private static XReversibleFieldEvent toReversibleFieldEvent(MiniElement xml, XAddress context,
	        TempTrans trans) {
		
		XAddress target = XmlUtils.getTarget(xml, context);
		
		ChangeType type = XmlUtils.getChangeType(xml, XREVERSIBLEFIELDEVENT_ELEMENT);
		
		long fieldRev = getRevision(xml, XREVERSIBLEFIELDEVENT_ELEMENT, FIELDREVISION_ATTRIBUTE,
		        true);
		long objectRev = getRevision(xml, XREVERSIBLEFIELDEVENT_ELEMENT, OBJECTREVISION_ATTRIBUTE,
		        false);
		long modelRev = trans != null ? trans.modelRev : getRevision(xml,
		        XREVERSIBLEFIELDEVENT_ELEMENT, MODELREVISION_ATTRIBUTE, false);
		
		XID actor = trans != null ? trans.actor : XmlUtils.getOptionalXidAttribute(xml,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(xml);
		
		XValue oldValue = null;
		XValue newValue = null;
		Iterator<MiniElement> it = xml.getChildren(NAME_VALUES);
		if(type != ChangeType.ADD) {
			oldValue = loadValue(it, "old");
			assert oldValue != null;
		}
		if(type != ChangeType.REMOVE) {
			newValue = loadValue(it, "new");
			assert newValue != null;
		}
		if(it.hasNext()) {
			throw new IllegalArgumentException("Invalid child of <" + XREVERSIBLEFIELDEVENT_ELEMENT
			        + ">: <" + it.next().getType() + ">");
		}
		
		if(type == ChangeType.ADD) {
			return MemoryReversibleFieldEvent.createAddEvent(actor, target, newValue, modelRev,
			        objectRev, fieldRev, inTransaction);
		} else if(type == ChangeType.CHANGE) {
			return MemoryReversibleFieldEvent.createChangeEvent(actor, target, oldValue, newValue,
			        modelRev, objectRev, fieldRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(xml);
			return MemoryReversibleFieldEvent.createRemoveEvent(actor, target, oldValue, modelRev,
			        objectRev, fieldRev, inTransaction, implied);
		} else {
			throw new IllegalArgumentException("<" + XREVERSIBLEFIELDEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for field events, but '" + type + "'");
		}
	}
	
	private static XModelEvent toModelEvent(MiniElement xml, XAddress context, TempTrans trans) {
		
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
		long modelRev = trans != null ? trans.modelRev : getRevision(xml, XMODELEVENT_ELEMENT,
		        MODELREVISION_ATTRIBUTE, true);
		
		XID actor = trans != null ? trans.actor : XmlUtils.getOptionalXidAttribute(xml,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(xml);
		
		XAddress target = address.getParent();
		XID objectId = address.getObject();
		
		if(type == ChangeType.ADD) {
			return MemoryModelEvent
			        .createAddEvent(actor, target, objectId, modelRev, inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(xml);
			return MemoryModelEvent.createRemoveEvent(actor, target, objectId, modelRev, objectRev,
			        inTransaction, implied);
		} else {
			throw new IllegalArgumentException("<" + XMODELEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for model events, but '" + type + "'");
		}
	}
	
	private static XObjectEvent toObjectEvent(MiniElement xml, XAddress context, TempTrans trans) {
		
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
		long modelRev = trans != null ? trans.modelRev : getRevision(xml, XOBJECTEVENT_ELEMENT,
		        MODELREVISION_ATTRIBUTE, false);
		
		XID actor = trans != null ? trans.actor : XmlUtils.getOptionalXidAttribute(xml,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(xml);
		
		XAddress target = address.getParent();
		XID fieldId = address.getField();
		
		if(type == ChangeType.ADD) {
			return MemoryObjectEvent.createAddEvent(actor, target, fieldId, modelRev, objectRev,
			        inTransaction);
		} else if(type == ChangeType.REMOVE) {
			boolean implied = getImpliedAttribute(xml);
			return MemoryObjectEvent.createRemoveEvent(actor, target, fieldId, modelRev, objectRev,
			        fieldRev, inTransaction, implied);
		} else {
			throw new IllegalArgumentException("<" + XOBJECTEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for object events, but '" + type + "'");
		}
	}
	
	private static XRepositoryEvent toRepositoryEvent(MiniElement xml, XAddress context,
	        TempTrans trans) {
		
		if(context.getObject() != null || context.getField() != null) {
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
		
		long modelRev = trans != null ? trans.modelRev : getRevision(xml, XREPOSITORYEVENT_ELEMENT,
		        MODELREVISION_ATTRIBUTE, type == ChangeType.REMOVE);
		
		XID actor = trans != null ? trans.actor : XmlUtils.getOptionalXidAttribute(xml,
		        ACTOR_ATTRIBUTE, null);
		boolean inTransaction = trans != null || getInTransactionAttribute(xml);
		
		XAddress target = address.getParent();
		XID modelId = address.getModel();
		
		if(type == ChangeType.ADD) {
			return MemoryRepositoryEvent.createAddEvent(actor, target, modelId, modelRev,
			        inTransaction);
		} else if(type == ChangeType.REMOVE) {
			return MemoryRepositoryEvent.createRemoveEvent(actor, target, modelId, modelRev,
			        inTransaction);
		} else {
			throw new IllegalArgumentException("<" + XREPOSITORYEVENT_ELEMENT + ">@"
			        + XmlUtils.TYPE_ATTRIBUTE
			        + " does not contain a valid type for repository events, but '" + type + "'");
		}
	}
	
	private static class TempTrans {
		
		final XID actor;
		final long modelRev;
		
		public TempTrans(XID actor, long modelRev) {
			this.actor = actor;
			this.modelRev = modelRev;
		}
		
	}
	
	private static XTransactionEvent toTransactionEvent(MiniElement xml, XAddress context) {
		
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
		
		TempTrans tt = new TempTrans(actor, modelRev);
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		Iterator<MiniElement> it = xml.getChildren(NAME_EVENTS);
		while(it.hasNext()) {
			MiniElement event = it.next();
			events.add(toAtomicEvent(event, target, tt));
		}
		
		return MemoryTransactionEvent.createTransactionEvent(actor, target, events, modelRev,
		        objectRev);
	}
	
	/**
	 * Encode the given {@link XEvent} list as an XML element.
	 * 
	 * @param events The events to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(Iterator<XEvent> events, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XEVENTLIST_ELEMENT);
		
		out.children(NAME_EVENTS, true);
		while(events.hasNext()) {
			toXml(events.next(), out, context);
		}
		
		out.close(XEVENTLIST_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XEvent} as an XML element.
	 * 
	 * @param event The event to encode.
	 * @param out The XML encoder to write to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element.
	 */
	public static void toXml(XEvent event, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		if(event == null) {
			XmlValue.saveNullElement(out);
		} else if(event instanceof XTransactionEvent) {
			toXml((XTransactionEvent)event, out, context);
		} else {
			toXml((XAtomicEvent)event, out, context, false);
		}
	}
	
	private static void toXml(XAtomicEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		if(event instanceof XReversibleFieldEvent) {
			toXml((XReversibleFieldEvent)event, out, context, inTrans);
		} else if(event instanceof XFieldEvent) {
			toXml((XFieldEvent)event, out, context, inTrans);
		} else if(event instanceof XObjectEvent) {
			toXml((XObjectEvent)event, out, context, inTrans);
		} else if(event instanceof XModelEvent) {
			toXml((XModelEvent)event, out, context, inTrans);
		} else if(event instanceof XRepositoryEvent) {
			toXml((XRepositoryEvent)event, out, context, inTrans);
		} else {
			throw new RuntimeException("event " + event + " is of unexpected type: "
			        + event.getClass());
		}
	}
	
	private static void toXml(XFieldEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		
		out.open(XFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		out.children(NAME_VALUE, false);
		if(event.getChangeType() != ChangeType.REMOVE) {
			XmlValue.toXml(event.getNewValue(), out);
		}
		
		out.close(XFIELDEVENT_ELEMENT);
		
	}
	
	private static void toXml(XReversibleFieldEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		
		out.open(XREVERSIBLEFIELDEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		out.children(NAME_VALUES, true);
		if(event.getChangeType() != ChangeType.ADD) {
			XmlValue.toXml(event.getOldValue(), out);
		}
		if(event.getChangeType() != ChangeType.REMOVE) {
			XmlValue.toXml(event.getNewValue(), out);
		}
		
		out.close(XREVERSIBLEFIELDEVENT_ELEMENT);
		
	}
	
	private static void toXml(XModelEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		
		if(context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for model events: " + context);
		}
		
		out.open(XMODELEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		out.attribute(XmlUtils.OBJECTID_ATTRIBUTE, event.getObjectId());
		
		out.close(XMODELEVENT_ELEMENT);
		
	}
	
	private static void toXml(XObjectEvent event, XydraOut out, XAddress context, boolean inTrans)
	        throws IllegalArgumentException {
		
		if(context.getField() != null) {
			throw new IllegalArgumentException("invalid context for object events: " + context);
		}
		
		out.open(XOBJECTEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		out.attribute(XmlUtils.FIELDID_ATTRIBUTE, event.getFieldId());
		
		out.close(XOBJECTEVENT_ELEMENT);
		
	}
	
	private static void toXml(XRepositoryEvent event, XydraOut out, XAddress context,
	        boolean inTrans) throws IllegalArgumentException {
		
		if(context.getObject() != null || context.getField() != null) {
			throw new IllegalArgumentException("invalid context for repository events: " + context);
		}
		
		out.open(XREPOSITORYEVENT_ELEMENT);
		
		setAtomicEventAttributes(event, out, context, inTrans);
		
		out.attribute(XmlUtils.MODELID_ATTRIBUTE, event.getModelId());
		
		out.close(XREPOSITORYEVENT_ELEMENT);
		
	}
	
	private static void toXml(XTransactionEvent trans, XydraOut out, XAddress context)
	        throws IllegalArgumentException {
		
		out.open(XTRANSACTIONEVENT_ELEMENT);
		
		setCommonAttributes(trans, out, context, false);
		
		XAddress newContext = trans.getTarget();
		
		out.children(NAME_EVENTS, true);
		for(XAtomicEvent event : trans) {
			assert event != null;
			toXml(event, out, newContext, true);
		}
		
		out.close(XTRANSACTIONEVENT_ELEMENT);
		
	}
	
}
