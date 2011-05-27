package org.xydra.core.xml;

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
 * and {@link XReadableField} to and from their XML representation.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XmlModel {
	
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
	
	private static long getRevisionAttribute(MiniElement xml, String elementName) {
		
		String revisionString = xml.getAttribute(REVISION_ATTRIBUTE);
		
		if(revisionString == null)
			return NO_REVISION;
		
		try {
			return Long.parseLong(revisionString);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("<" + elementName + ">@" + REVISION_ATTRIBUTE
			        + " does not contain a long, but '" + revisionString + "'");
		}
	}
	
	static private XChangeLogState loadChangeLogState(MiniElement xml, XAddress baseAddr) {
		Iterator<MiniElement> logElementIt = xml.getElementsByTagName(XCHANGELOG_ELEMENT);
		if(logElementIt.hasNext()) {
			XChangeLogState log = new MemoryChangeLogState(baseAddr);
			loadChangeLogState(logElementIt.next(), log);
			if(logElementIt.hasNext()) {
				throw new IllegalArgumentException("xml object contains multiple change logs");
			}
		}
		return null;
	}
	
	/**
	 * Load the change log represented by the given XML element into an
	 * {@link XChangeLogState}.
	 * 
	 * @param state The change log state to load into.
	 */
	public static void loadChangeLogState(MiniElement xml, XChangeLogState state) {
		
		XmlUtils.checkElementName(xml, XCHANGELOG_ELEMENT);
		
		long startRev = 0L;
		String revisionString = xml.getAttribute(STARTREVISION_ATTRIBUTE);
		if(revisionString != null) {
			try {
				startRev = Long.parseLong(revisionString);
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException("<" + XCHANGELOG_ELEMENT + ">@"
				        + STARTREVISION_ATTRIBUTE + " does not contain a long, but '"
				        + revisionString + "'");
			}
		}
		
		state.setFirstRevisionNumber(startRev);
		
		Iterator<MiniElement> eventElementIt = xml.getElements();
		while(eventElementIt.hasNext()) {
			MiniElement e = eventElementIt.next();
			XEvent event = XmlEvent.toEvent(e, state.getBaseAddress());
			state.appendEvent(event);
		}
		
	}
	
	/**
	 * Get the {@link XField} represented by the given XML element.
	 * 
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xfield&gt; and ending
	 *            with the same &lt;/xfield&gt;
	 * 
	 * @return an {@link XField}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XField element.
	 */
	public static XField toField(XID actorId, MiniElement xml) {
		return new MemoryField(actorId, toFieldState(xml, null));
	}
	
	/**
	 * Load the field represented by the given XML element into an
	 * {@link XRevWritableField}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleField}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableField}
	 */
	public static XRevWritableField toFieldState(MiniElement xml, XRevWritableObject parent) {
		
		XmlUtils.checkElementName(xml, XFIELD_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XFIELD_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XFIELD_ELEMENT);
		
		XValue xvalue = null;
		
		Iterator<MiniElement> valueElementIt = xml.getElements();
		if(valueElementIt.hasNext()) {
			MiniElement valueElement = valueElementIt.next();
			xvalue = XmlValue.toValue(valueElement);
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
	 * Get the {@link XModel} represented by the given XML element.
	 * 
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xmodel&gt; and ending
	 *            with the same &lt;/xmodel&gt;
	 * 
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel element.
	 */
	public static XModel toModel(XID actorId, String passwordHash, MiniElement xml) {
		XRevWritableModel state = toModelState(xml, null, null);
		XChangeLogState log = loadChangeLogState(xml, state.getAddress());
		if(log != null) {
			return new MemoryModel(actorId, passwordHash, state, log);
		} else {
			return new MemoryModel(actorId, passwordHash, state);
		}
	}
	
	/**
	 * Load the model represented by the given XML element into an
	 * {@link XRevWritableModel}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleModel}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableModel}
	 */
	public static XRevWritableModel toModelState(MiniElement xml, XRevWritableRepository parent) {
		return toModelState(xml, parent, null);
	}
	
	public static XRevWritableModel toModelState(MiniElement xml, XAddress context) {
		return toModelState(xml, null, context);
	}
	
	private static XRevWritableModel toModelState(MiniElement xml, XRevWritableRepository parent,
	        XAddress context) {
		
		XmlUtils.checkElementName(xml, XMODEL_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XMODEL_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XMODEL_ELEMENT);
		
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
		
		Iterator<MiniElement> objectElementIt = xml.getElementsByTagName(XOBJECT_ELEMENT);
		while(objectElementIt.hasNext()) {
			MiniElement objectElement = objectElementIt.next();
			XRevWritableObject objectState = toObjectState(objectElement, modelState);
			assert modelState.getObject(objectState.getID()) == objectState;
		}
		
		return modelState;
	}
	
	/**
	 * Get the {@link XObject} represented by the given XML element.
	 * 
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xobject&gt; and
	 *            ending with the same &lt;/xobject&gt;
	 * 
	 * @return an {@link XObject}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XObject element.
	 */
	public static XObject toObject(XID actorId, String passwordHash, MiniElement xml) {
		XRevWritableObject state = toObjectState(xml, null, null);
		XChangeLogState log = loadChangeLogState(xml, state.getAddress());
		if(log != null) {
			return new MemoryObject(actorId, passwordHash, state, log);
		} else {
			return new MemoryObject(actorId, passwordHash, state);
		}
	}
	
	/**
	 * Load the object represented by the given XML element into an
	 * {@link XRevWritableObject}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleObject}, otherwise it is loaded into a child
	 *            state of parent.
	 * @return the created {@link XRevWritableObject}
	 */
	public static XRevWritableObject toObjectState(MiniElement xml, XRevWritableModel parent) {
		return toObjectState(xml, parent, null);
	}
	
	public static XRevWritableObject toObjectState(MiniElement xml, XAddress context) {
		return toObjectState(xml, null, context);
	}
	
	private static XRevWritableObject toObjectState(MiniElement xml, XRevWritableModel parent,
	        XAddress context) {
		
		XmlUtils.checkElementName(xml, XOBJECT_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XOBJECT_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XOBJECT_ELEMENT);
		
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
		
		Iterator<MiniElement> fieldElementIt = xml.getElementsByTagName(XFIELD_ELEMENT);
		while(fieldElementIt.hasNext()) {
			MiniElement fieldElement = fieldElementIt.next();
			XRevWritableField fieldState = toFieldState(fieldElement, objectState);
			assert objectState.getField(fieldState.getID()) == fieldState;
		}
		
		return objectState;
	}
	
	/**
	 * Get the {@link XRepository} represented by the given XML element.
	 * 
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xrepository&gt; and
	 *            ending with the same &lt;/xrepository&gt;
	 * 
	 * @return an {@link XRepository}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XRepository element.
	 */
	public static XRepository toRepository(XID actorId, String passwordHash, MiniElement xml) {
		return new MemoryRepository(actorId, passwordHash, toRepositoryState(xml));
	}
	
	/**
	 * Load the repository represented by the given XML element into an
	 * {@link XRevWritableRepository}.
	 * 
	 * @return the created {@link XRevWritableRepository}
	 */
	public static XRevWritableRepository toRepositoryState(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, XREPOSITORY_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XREPOSITORY_ELEMENT);
		
		XAddress repoAddr = XX.toAddress(xid, null, null, null);
		XRevWritableRepository repositoryState = new SimpleRepository(repoAddr);
		
		Iterator<MiniElement> modelElementIt = xml.getElementsByTagName(XMODEL_ELEMENT);
		while(modelElementIt.hasNext()) {
			MiniElement modelElement = modelElementIt.next();
			XRevWritableModel modelState = toModelState(modelElement, repositoryState);
			assert repositoryState.getModel(modelState.getID()) == modelState;
		}
		
		return repositoryState;
	}
	
	/**
	 * Encode the given {@link XChangeLog} as an XML element.
	 * 
	 * @param log an {@link XChangeLog}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 */
	public static void toXml(XChangeLog log, XydraOut xo) {
		
		// get values before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = log.getFirstRevisionNumber();
		Iterator<XEvent> events = log.getEventsBetween(0, Long.MAX_VALUE);
		
		xo.open(XCHANGELOG_ELEMENT);
		if(rev != 0) {
			xo.attribute(STARTREVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		xo.children(NAME_EVENTS, true);
		while(events.hasNext()) {
			XmlEvent.toXml(events.next(), xo, log.getBaseAddress());
		}
		
		xo.close(XCHANGELOG_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableField} as an XML element, including
	 * revision numbers.
	 * 
	 * @param xfield an {@link XReadableField}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableField xfield, XydraOut xo) {
		toXml(xfield, xo, true);
	}
	
	/**
	 * Encode the given {@link XReadableField} as an XML element.
	 * 
	 * @param xfield an {@link XReadableField}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableField xfield, XydraOut xo, boolean saveRevision) {
		
		// get values before outputting anything to prevent incomplete XML
		// elements on errors
		XValue xvalue = xfield.getValue();
		long rev = xfield.getRevisionNumber();
		
		xo.open(XFIELD_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xfield.getID().toString());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		xo.children(NAME_VALUE, false);
		if(xvalue != null) {
			XmlValue.toXml(xvalue, xo);
		}
		
		xo.close(XFIELD_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableModel} as an XML element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xmodel an {@link XReadableModel}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xmodel&gt; and ending with the same &lt;/xmodel&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableModel xmodel, XydraOut xo) {
		toXml(xmodel, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableModel} as an XML element.
	 * 
	 * @param xmodel an {@link XReadableModel}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xmodel&gt; and ending with the same &lt;/xmodel&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible objects and fields instead
	 *            of throwing an exception
	 * @param saveChangeLog if true, the change log is saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableModel xmodel, XydraOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		if(!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xmodel.getRevisionNumber();
		
		xo.open(XMODEL_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xmodel.getID().toString());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		xo.children(NAME_OBJECTS, true);
		for(XID objectId : xmodel) {
			try {
				toXml(xmodel.getObject(objectId), xo, saveRevision, ignoreInaccessible, false);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		
		if(saveChangeLog && xmodel instanceof XLoggedModel) {
			XChangeLog log = ((XLoggedModel)xmodel).getChangeLog();
			if(log != null) {
				toXml(log, xo);
				assert log.getCurrentRevisionNumber() == xmodel.getRevisionNumber();
			}
		}
		
		xo.close(XMODEL_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableObject} as an XML element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xobject an {@link XReadableObject}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xobject&gt; and ending with the same &lt;/xobject&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableObject xobject, XydraOut xo) {
		toXml(xobject, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableObject} as an XML element.
	 * 
	 * @param xobject an {@link XObject}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xobject&gt; and ending with the same &lt;/xobject&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible fields instead of throwing
	 *            an exception
	 * @param saveChangeLog if true, any object change log is saved
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableObject xobject, XydraOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		if(!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xobject.getRevisionNumber();
		
		xo.open(XOBJECT_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xobject.getID().toString());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		xo.children(NAME_FIELDS, true);
		for(XID fieldId : xobject) {
			try {
				toXml(xobject.getField(fieldId), xo, saveRevision);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		
		if(saveChangeLog && xobject instanceof XLoggedObject) {
			XChangeLog log = ((XLoggedObject)xobject).getChangeLog();
			if(log != null && log.getBaseAddress().equals(xobject.getAddress())) {
				toXml(log, xo);
				assert log.getCurrentRevisionNumber() == xobject.getRevisionNumber();
			}
		}
		
		xo.close(XOBJECT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XReadableRepository} as an XML element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xrepository an {@link XReadableRepository}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xrepository&gt; and ending with the same
	 *            &lt;/xrepository&gt; is written to. White space is permitted
	 *            but not required.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableRepository xrepository, XydraOut xo) {
		toXml(xrepository, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XReadableRepository} as an XML element.
	 * 
	 * @param xrepository an {@link XReadableRepository}
	 * @param xo the {@link XydraOut} that a partial XML document starting with
	 *            &lt;xrepository&gt; and ending with the same
	 *            &lt;/xrepository&gt; is written to. White space is permitted
	 *            but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible models, objects and fields
	 *            instead of throwing an exception
	 * @param saveChangeLog if true, any model change logs are saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValue} for details.
	 */
	public static void toXml(XReadableRepository xrepository, XydraOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		xo.open(XREPOSITORY_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xrepository.getID().toString());
		
		xo.children(NAME_MODELS, true);
		for(XID modelOd : xrepository) {
			try {
				toXml(xrepository.getModel(modelOd), xo, saveRevision, ignoreInaccessible,
				        saveChangeLog);
			} catch(AccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		
		xo.close(XREPOSITORY_ELEMENT);
		
	}
	
	public static boolean isModel(MiniElement xml) {
		return XMODEL_ELEMENT.equals(xml.getName());
	}
	
}
