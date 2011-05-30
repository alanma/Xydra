package org.xydra.core.serialize;

import java.util.Iterator;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.store.AccessException;


/**
 * Collection of methods to (de-)serialize variants of
 * {@link XReadableRepository}, {@link XReadableModel}, {@link XReadableObject}
 * and {@link XReadableField} to and from their XML/JSON representation.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedModel {
	
	private static final String NAME_EVENTS = "events";
	private static final String NAME_VALUE = "value";
	private static final String NAME_OBJECTS = "objects";
	private static final String NAME_FIELDS = "fields";
	private static final String NAME_MODELS = "models";
	public static final long NO_REVISION = -1;
	private static final String REVISION_ATTRIBUTE = "revision";
	private static final String STARTREVISION_ATTRIBUTE = "startRevision";
	private static final String XCHANGELOG_ELEMENT = "xlog";
	private static final String XFIELD_ELEMENT = "xfield";
	
	private static final String XMODEL_ELEMENT = "xmodel";
	private static final String XOBJECT_ELEMENT = "xobject";
	
	private static final String XREPOSITORY_ELEMENT = "xrepository";
	
	private static long getRevisionAttribute(MiniElement element) {
		
		Object revisionString = element.getAttribute(REVISION_ATTRIBUTE);
		
		if(revisionString == null) {
			return NO_REVISION;
		}
		
		return SerializingUtils.toLong(revisionString);
	}
	
	public static XChangeLogState loadChangeLogState(MiniElement element, XAddress baseAddr) {
		MiniElement logElement = element.getElement(XCHANGELOG_ELEMENT);
		if(logElement != null) {
			XChangeLogState log = new MemoryChangeLogState(baseAddr);
			loadChangeLogState(logElement, log);
			return log;
		}
		return null;
	}
	
	/**
	 * Load the change log represented by the given XML/JSON element into an
	 * {@link XChangeLogState}.
	 * 
	 * @param state The change log state to load into.
	 */
	public static void loadChangeLogState(MiniElement element, XChangeLogState state) {
		
		SerializingUtils.checkElementType(element, XCHANGELOG_ELEMENT);
		
		long startRev = 0L;
		Object revisionString = element.getAttribute(STARTREVISION_ATTRIBUTE);
		if(revisionString != null) {
			startRev = SerializingUtils.toLong(revisionString);
		}
		
		state.setFirstRevisionNumber(startRev);
		
		Iterator<MiniElement> eventElementIt = element.getChildren(NAME_EVENTS);
		while(eventElementIt.hasNext()) {
			MiniElement e = eventElementIt.next();
			XEvent event = SerializedEvent.toEvent(e, state.getBaseAddress());
			state.appendEvent(event);
		}
		
	}
	
	/**
	 * Get the {@link XField} represented by the given XML/JSON element.
	 * 
	 * @return an {@link XField}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XField element.
	 */
	public static XField toField(XID actorId, MiniElement element) {
		return new MemoryField(actorId, toFieldState(element, null));
	}
	
	/**
	 * Load the field represented by the given XML/JSON element into an
	 * {@link XRevWritableField}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleField}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableField}
	 */
	public static XRevWritableField toFieldState(MiniElement element, XRevWritableObject parent) {
		
		SerializingUtils.checkElementType(element, XFIELD_ELEMENT);
		
		XID xid = SerializingUtils.getRequiredXidAttribute(element);
		
		long revision = getRevisionAttribute(element);
		
		XValue xvalue = null;
		
		Iterator<MiniElement> valueElementIt = element.getChildren(NAME_VALUE);
		if(valueElementIt.hasNext()) {
			MiniElement valueElement = valueElementIt.next();
			xvalue = SerializedValue.toValue(valueElement);
		}
		
		XRevWritableField fieldState;
		if(parent == null) {
			XAddress fieldAddr = XX.toAddress(null, null, null, xid);
			fieldState = new SimpleField(fieldAddr);
		} else {
			fieldState = parent.createField(xid);
		}
		fieldState.setRevisionNumber(revision);
		fieldState.setValue(xvalue);
		
		return fieldState;
	}
	
	/**
	 * Get the {@link XModel} represented by the given XML/JSON element.
	 * 
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel element.
	 */
	public static XModel toModel(XID actorId, String passwordHash, MiniElement element) {
		XRevWritableModel state = toModelState(element, null, null);
		XChangeLogState log = loadChangeLogState(element, state.getAddress());
		if(log != null) {
			return new MemoryModel(actorId, passwordHash, state, log);
		} else {
			return new MemoryModel(actorId, passwordHash, state);
		}
	}
	
	/**
	 * Load the model represented by the given XML/JSON element into an
	 * {@link XRevWritableModel}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleModel}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableModel}
	 */
	public static XRevWritableModel toModelState(MiniElement element, XRevWritableRepository parent) {
		return toModelState(element, parent, null);
	}
	
	public static XRevWritableModel toModelState(MiniElement element, XAddress context) {
		return toModelState(element, null, context);
	}
	
	private static XRevWritableModel toModelState(MiniElement element,
	        XRevWritableRepository parent, XAddress context) {
		
		SerializingUtils.checkElementType(element, XMODEL_ELEMENT);
		
		XID xid = SerializingUtils.getRequiredXidAttribute(element);
		
		long revision = getRevisionAttribute(element);
		
		XRevWritableModel modelState;
		if(parent == null) {
			XAddress modelAddr;
			if(context != null) {
				modelAddr = XX.toAddress(context.getRepository(), xid, null, null);
			} else {
				modelAddr = XX.toAddress(null, xid, null, null);
			}
			modelState = new SimpleModel(modelAddr);
		} else {
			modelState = parent.createModel(xid);
		}
		modelState.setRevisionNumber(revision);
		
		Iterator<MiniElement> objectElementIt = element.getChildren(NAME_OBJECTS, XOBJECT_ELEMENT);
		while(objectElementIt.hasNext()) {
			MiniElement objectElement = objectElementIt.next();
			XRevWritableObject objectState = toObjectState(objectElement, modelState);
			assert modelState.getObject(objectState.getID()) == objectState;
		}
		
		return modelState;
	}
	
	/**
	 * Get the {@link XObject} represented by the given XML/JSON element.
	 * 
	 * @return an {@link XObject}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XObject element.
	 */
	public static XObject toObject(XID actorId, String passwordHash, MiniElement element) {
		XRevWritableObject state = toObjectState(element, null, null);
		XChangeLogState log = loadChangeLogState(element, state.getAddress());
		if(log != null) {
			return new MemoryObject(actorId, passwordHash, state, log);
		} else {
			return new MemoryObject(actorId, passwordHash, state);
		}
	}
	
	/**
	 * Load the object represented by the given XML/JSON element into an
	 * {@link XRevWritableObject}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleObject}, otherwise it is loaded into a child
	 *            state of parent.
	 * @return the created {@link XRevWritableObject}
	 */
	public static XRevWritableObject toObjectState(MiniElement element, XRevWritableModel parent) {
		return toObjectState(element, parent, null);
	}
	
	public static XRevWritableObject toObjectState(MiniElement element, XAddress context) {
		return toObjectState(element, null, context);
	}
	
	private static XRevWritableObject toObjectState(MiniElement element, XRevWritableModel parent,
	        XAddress context) {
		
		SerializingUtils.checkElementType(element, XOBJECT_ELEMENT);
		
		XID xid = SerializingUtils.getRequiredXidAttribute(element);
		
		long revision = getRevisionAttribute(element);
		
		XRevWritableObject objectState;
		if(parent == null) {
			XAddress objectAddr;
			if(context != null) {
				objectAddr = XX.toAddress(context.getRepository(), context.getModel(), xid, null);
			} else {
				objectAddr = XX.toAddress(null, null, xid, null);
			}
			objectState = new SimpleObject(objectAddr);
		} else {
			objectState = parent.createObject(xid);
		}
		
		objectState.setRevisionNumber(revision);
		
		Iterator<MiniElement> fieldElementIt = element.getChildren(NAME_FIELDS, XFIELD_ELEMENT);
		while(fieldElementIt.hasNext()) {
			MiniElement fieldElement = fieldElementIt.next();
			XRevWritableField fieldState = toFieldState(fieldElement, objectState);
			assert objectState.getField(fieldState.getID()) == fieldState;
		}
		
		return objectState;
	}
	
	/**
	 * Get the {@link XRepository} represented by the given XML/JSON element.
	 * 
	 * @return an {@link XRepository}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XRepository element.
	 */
	public static XRepository toRepository(XID actorId, String passwordHash, MiniElement element) {
		return new MemoryRepository(actorId, passwordHash, toRepositoryState(element));
	}
	
	/**
	 * Load the repository represented by the given XML/JSON element into an
	 * {@link XRevWritableRepository}.
	 * 
	 * @return the created {@link XRevWritableRepository}
	 */
	public static XRevWritableRepository toRepositoryState(MiniElement element) {
		
		SerializingUtils.checkElementType(element, XREPOSITORY_ELEMENT);
		
		XID xid = SerializingUtils.getRequiredXidAttribute(element);
		
		XAddress repoAddr = XX.toAddress(xid, null, null, null);
		XRevWritableRepository repositoryState = new SimpleRepository(repoAddr);
		
		Iterator<MiniElement> modelElementIt = element.getChildren(NAME_MODELS, XMODEL_ELEMENT);
		while(modelElementIt.hasNext()) {
			MiniElement modelElement = modelElementIt.next();
			XRevWritableModel modelState = toModelState(modelElement, repositoryState);
			assert repositoryState.getModel(modelState.getID()) == modelState;
		}
		
		return repositoryState;
	}
	
	/**
	 * Encode the given {@link XChangeLog} as an XML/JSON element.
	 * 
	 * @param log an {@link XChangeLog}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 */
	public static void serialize(XChangeLog log, XydraOut out) {
		
		// get values before outputting anything to prevent incomplete
		// elements on errors
		long rev = log.getFirstRevisionNumber();
		Iterator<XEvent> events = log.getEventsBetween(0, Long.MAX_VALUE);
		
		out.open(XCHANGELOG_ELEMENT);
		if(rev != 0) {
			out.attribute(STARTREVISION_ATTRIBUTE, rev);
		}
		
		out.beginChildren(NAME_EVENTS, true);
		while(events.hasNext()) {
			SerializedEvent.serialize(events.next(), out, log.getBaseAddress());
		}
		out.endChildren();
		
		out.close(XCHANGELOG_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableField} as an XML/JSON element, including
	 * revision numbers.
	 * 
	 * @param xfield an {@link XReadableField}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableField xfield, XydraOut out) {
		serialize(xfield, out, true);
	}
	
	/**
	 * Encode the given {@link XReadableField} as an XML/JSON element.
	 * 
	 * @param xfield an {@link XReadableField}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved in the
	 *            element.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableField xfield, XydraOut out, boolean saveRevision) {
		
		// get values before outputting anything to prevent incomplete
		// elements on errors
		XValue xvalue = xfield.getValue();
		long rev = xfield.getRevisionNumber();
		
		out.open(XFIELD_ELEMENT);
		out.attribute(SerializingUtils.XID_ATTRIBUTE, xfield.getID());
		if(saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}
		
		if(xvalue != null) {
			out.beginChildren(NAME_VALUE, false);
			SerializedValue.serialize(xvalue, out);
			out.endChildren();
		}
		
		out.close(XFIELD_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableModel} as an XML/JSON element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xmodel an {@link XReadableModel}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableModel xmodel, XydraOut out) {
		serialize(xmodel, out, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableModel} as an XML/JSON element.
	 * 
	 * @param xmodel an {@link XReadableModel}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible objects and fields instead
	 *            of throwing an exception
	 * @param saveChangeLog if true, the change log is saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableModel xmodel, XydraOut out, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		if(!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}
		
		// get revision before outputting anything to prevent incomplete
		// elements on errors
		long rev = xmodel.getRevisionNumber();
		
		out.open(XMODEL_ELEMENT);
		out.attribute(SerializingUtils.XID_ATTRIBUTE, xmodel.getID());
		if(saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}
		
		out.beginChildren(NAME_OBJECTS, true, XOBJECT_ELEMENT);
		for(XID objectId : xmodel) {
			try {
				serialize(xmodel.getObject(objectId), out, saveRevision, ignoreInaccessible, false);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endChildren();
		
		if(saveChangeLog && xmodel instanceof XLoggedModel) {
			XChangeLog log = ((XLoggedModel)xmodel).getChangeLog();
			if(log != null) {
				serialize(log, out);
				assert log.getCurrentRevisionNumber() == xmodel.getRevisionNumber();
			}
		}
		
		out.close(XMODEL_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableObject} as an XML/JSON element,
	 * including revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xobject an {@link XReadableObject}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableObject xobject, XydraOut out) {
		serialize(xobject, out, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableObject} as an XML/JSON element.
	 * 
	 * @param xobject an {@link XObject}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible fields instead of throwing
	 *            an exception
	 * @param saveChangeLog if true, any object change log is saved
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableObject xobject, XydraOut out, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		if(!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}
		
		// get revision before outputting anything to prevent incomplete
		// elements on errors
		long rev = xobject.getRevisionNumber();
		
		out.open(XOBJECT_ELEMENT);
		out.attribute(SerializingUtils.XID_ATTRIBUTE, xobject.getID());
		if(saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}
		
		out.beginChildren(NAME_FIELDS, true, XFIELD_ELEMENT);
		for(XID fieldId : xobject) {
			try {
				serialize(xobject.getField(fieldId), out, saveRevision);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endChildren();
		
		if(saveChangeLog && xobject instanceof XLoggedObject) {
			XChangeLog log = ((XLoggedObject)xobject).getChangeLog();
			if(log != null && log.getBaseAddress().equals(xobject.getAddress())) {
				serialize(log, out);
				assert log.getCurrentRevisionNumber() == xobject.getRevisionNumber();
			}
		}
		
		out.close(XOBJECT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableRepository} as an XML/JSON element,
	 * including revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xrepository an {@link XReadableRepository}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableRepository xrepository, XydraOut out) {
		serialize(xrepository, out, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableRepository} as an XML/JSON element.
	 * 
	 * @param xrepository an {@link XReadableRepository}
	 * @param out the {@link XydraOut} that a partial XML document is written
	 *            to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible models, objects and fields
	 *            instead of throwing an exception
	 * @param saveChangeLog if true, any model change logs are saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(XReadableRepository xrepository, XydraOut out,
	        boolean saveRevision, boolean ignoreInaccessible, boolean saveChangeLog) {
		
		out.open(XREPOSITORY_ELEMENT);
		out.attribute(SerializingUtils.XID_ATTRIBUTE, xrepository.getID());
		
		out.beginChildren(NAME_MODELS, true, XMODEL_ELEMENT);
		for(XID modelOd : xrepository) {
			try {
				serialize(xrepository.getModel(modelOd), out, saveRevision, ignoreInaccessible,
				        saveChangeLog);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endChildren();
		
		out.close(XREPOSITORY_ELEMENT);
		
	}
	
	public static boolean isModel(MiniElement element) {
		return element == null || XMODEL_ELEMENT.equals(element.getType());
	}
	
}
