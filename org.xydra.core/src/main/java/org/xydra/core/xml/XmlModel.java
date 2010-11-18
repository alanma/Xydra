package org.xydra.core.xml;

import java.util.Iterator;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateStore;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryFieldState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;
import org.xydra.core.model.state.impl.memory.TemporaryRepositoryState;
import org.xydra.core.value.XValue;
import org.xydra.store.AccessException;


/**
 * XModel-Implementation agnostic implementation of {@link XmlModelReader} and
 * {@link XmlModelWriter} that uses an {@link XModelFactory} to create
 * de-serialized XModel objects and their parts.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class XmlModel {
	
	private static final String XREPOSITORY_ELEMENT = "xrepository";
	private static final String XMODEL_ELEMENT = "xmodel";
	private static final String XOBJECT_ELEMENT = "xobject";
	private static final String XFIELD_ELEMENT = "xfield";
	private static final String XCHANGELOG_ELEMENT = "xlog";
	
	private static final String REVISION_ATTRIBUTE = "revision";
	private static final String STARTREVISION_ATTRIBUTE = "startRevision";
	
	public static final long NO_REVISION = -1;
	
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
	
	/**
	 * Load the repository represented by the given XML element into an
	 * {@link XRepositoryState}.
	 * 
	 * @param If this is not null the given store will be used to create the
	 *            reository state, otherwise a {@link TemporaryRepositoryState}
	 *            ist created
	 * 
	 * @return the created {@link XRepositoryState}
	 */
	public static XRepositoryState toRepositoryState(MiniElement xml, XStateStore store) {
		
		XmlUtils.checkElementName(xml, XREPOSITORY_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XREPOSITORY_ELEMENT);
		
		XAddress repoAddr = XX.toAddress(xid, null, null, null);
		XRepositoryState repositoryState = new TemporaryRepositoryState(repoAddr);
		if(store == null) {
			repositoryState = new TemporaryRepositoryState(repoAddr);
		} else {
			repositoryState = store.createRepositoryState(repoAddr);
		}
		
		XStateTransaction trans = repositoryState.beginTransaction();
		
		Iterator<MiniElement> modelElementIt = xml.getElementsByTagName(XMODEL_ELEMENT);
		while(modelElementIt.hasNext()) {
			MiniElement modelElement = modelElementIt.next();
			XModelState modelState = toModelState(modelElement, repositoryState, trans);
			repositoryState.addModelState(modelState);
		}
		
		repositoryState.save(trans);
		
		repositoryState.endTransaction(trans);
		
		return repositoryState;
	}
	
	/**
	 * Get the {@link XRepository} represented by the given XML element.
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xrepository&gt; and
	 *            ending with the same &lt;/xrepository&gt;
	 * 
	 * @return an {@link XRepository}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XRepository element.
	 */
	public static XRepository toRepository(XID actorId, MiniElement xml) {
		return new MemoryRepository(actorId, toRepositoryState(xml, null));
	}
	
	/**
	 * Load the model represented by the given XML element into an
	 * {@link XModelState}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link TemporaryModelState}, otherwise it is loaded into a
	 *            child state of parent.
	 * @param trans The state transaction to use for creating the model.
	 * @return the created {@link XModelState}
	 */
	public static XModelState toModelState(MiniElement xml, XRepositoryState parent,
	        XStateTransaction trans) {
		
		XmlUtils.checkElementName(xml, XMODEL_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XMODEL_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XMODEL_ELEMENT);
		
		XModelState modelState;
		if(parent == null) {
			XAddress modelAddr = XX.toAddress(null, xid, null, null);
			XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr);
			modelState = new TemporaryModelState(modelAddr, changeLogState);
		} else {
			modelState = parent.createModelState(xid);
		}
		modelState.setRevisionNumber(revision);
		
		XStateTransaction t = trans == null ? modelState.beginTransaction() : trans;
		
		Iterator<MiniElement> objectElementIt = xml.getElementsByTagName(XOBJECT_ELEMENT);
		while(objectElementIt.hasNext()) {
			MiniElement objectElement = objectElementIt.next();
			XObjectState objectState = toObjectState(objectElement, modelState, t);
			modelState.addObjectState(objectState);
		}
		
		Iterator<MiniElement> logElementIt = xml.getElementsByTagName(XCHANGELOG_ELEMENT);
		if(logElementIt.hasNext()) {
			loadChangeLogState(logElementIt.next(), modelState.getChangeLogState(), t);
			if(logElementIt.hasNext()) {
				throw new IllegalArgumentException("xml model " + modelState.getAddress()
				        + " contains multiple change logs");
			}
		} else {
			modelState.getChangeLogState().setFirstRevisionNumber(revision + 1);
		}
		
		modelState.save(null);
		
		if(trans == null) {
			modelState.endTransaction(t);
		}
		
		return modelState;
	}
	
	/**
	 * Load the model represented by the given XML element into an
	 * {@link XModelState}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link TemporaryModelState}, otherwise it is loaded into a
	 *            child state of parent.
	 * @param trans The state transaction to use for creating the model.
	 * @return the created {@link XModelState}
	 */
	public static void loadChangeLogState(MiniElement xml, XChangeLogState state,
	        XStateTransaction trans) {
		
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
			XEvent event = XmlEvent.toEvent(e, state.getBaseAddress(), state);
			state.appendEvent(event, trans);
		}
		
	}
	
	/**
	 * Get the {@link XModel} represented by the given XML element.
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xmodel&gt; and ending
	 *            with the same &lt;/xmodel&gt;
	 * 
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel element.
	 */
	public static XModel toModel(XID actorId, MiniElement xml) {
		return new MemoryModel(actorId, toModelState(xml, null, null));
	}
	
	/**
	 * Load the object represented by the given XML element into an
	 * {@link XObjectState}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link TemporaryObjectState}, otherwise it is loaded into a
	 *            child state of parent.
	 * @param trans The state transaction to use for creating the object.
	 * @return the created {@link XObjectState}
	 */
	public static XObjectState toObjectState(MiniElement xml, XModelState parent,
	        XStateTransaction trans) {
		
		XmlUtils.checkElementName(xml, XOBJECT_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XOBJECT_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XOBJECT_ELEMENT);
		
		XObjectState objectState;
		if(parent == null) {
			XAddress objectAddr = XX.toAddress(null, null, xid, null);
			XChangeLogState changeLogState = new MemoryChangeLogState(objectAddr);
			objectState = new TemporaryObjectState(objectAddr, changeLogState);
		} else {
			objectState = parent.createObjectState(xid);
		}
		
		objectState.setRevisionNumber(revision);
		
		XStateTransaction t = trans == null ? objectState.beginTransaction() : trans;
		
		Iterator<MiniElement> fieldElementIt = xml.getElementsByTagName(XFIELD_ELEMENT);
		while(fieldElementIt.hasNext()) {
			MiniElement fieldElement = fieldElementIt.next();
			XFieldState fieldState = toFieldState(fieldElement, objectState, t);
			objectState.addFieldState(fieldState);
		}
		
		Iterator<MiniElement> logElementIt = xml.getElementsByTagName(XCHANGELOG_ELEMENT);
		if(logElementIt.hasNext()) {
			if(parent != null) {
				throw new IllegalArgumentException("xml object " + objectState.getAddress()
				        + " has a change log, but is contained in a model");
			}
			loadChangeLogState(logElementIt.next(), objectState.getChangeLogState(), t);
			if(logElementIt.hasNext()) {
				throw new IllegalArgumentException("xml object " + objectState.getAddress()
				        + " contains multiple change logs");
			}
		} else if(parent == null) {
			objectState.getChangeLogState().setFirstRevisionNumber(revision);
		}
		
		objectState.save(t);
		
		if(trans == null) {
			objectState.endTransaction(t);
		}
		
		return objectState;
	}
	
	/**
	 * Get the {@link XObject} represented by the given XML element.
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xobject&gt; and
	 *            ending with the same &lt;/xobject&gt;
	 * 
	 * @return an {@link XObject}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XObject element.
	 */
	public static XObject toObject(XID actorId, MiniElement xml) {
		return new MemoryObject(actorId, toObjectState(xml, null, null));
	}
	
	/**
	 * Load the field represented by the given XML element into an
	 * {@link XFieldState}.
	 * 
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link TemporaryFieldState}, otherwise it is loaded into a
	 *            child state of parent.
	 * @param trans The state transaction to use for creating the field.
	 * @return the created {@link XFieldState}
	 */
	public static XFieldState toFieldState(MiniElement xml, XObjectState parent,
	        XStateTransaction trans) {
		
		XmlUtils.checkElementName(xml, XFIELD_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XFIELD_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XFIELD_ELEMENT);
		
		XValue xvalue = null;
		
		Iterator<MiniElement> valueElementIt = xml.getElements();
		if(valueElementIt.hasNext()) {
			MiniElement valueElement = valueElementIt.next();
			xvalue = XmlValue.toValue(valueElement);
		}
		
		XFieldState fieldState;
		if(parent == null) {
			XAddress fieldAddr = XX.toAddress(null, null, null, xid);
			fieldState = new TemporaryFieldState(fieldAddr);
		} else {
			fieldState = parent.createFieldState(xid);
		}
		fieldState.setRevisionNumber(revision);
		fieldState.setValue(xvalue);
		
		fieldState.save(trans);
		return fieldState;
	}
	
	/**
	 * Get the {@link XField} represented by the given XML element.
	 * @param actorId TODO
	 * @param xml a partial XML document starting with &lt;xfield&gt; and ending
	 *            with the same &lt;/xfield&gt;
	 * 
	 * @return an {@link XField}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XField element.
	 */
	public static XField toField(XID actorId, MiniElement xml) {
		return new MemoryField(actorId, toFieldState(xml, null, null));
	}
	
	/**
	 * Encode the given {@link XBaseRepository} as an XML element.
	 * 
	 * @param xmodel an {@link XBaseRepository}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xrepository&gt; and ending with the same
	 *            &lt;/xrepository&gt; is written to. White space is permitted
	 *            but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible models, objects and fields
	 *            instead of throwing an exception
	 * @param saveChangeLog if true, any model change logs are saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseRepository xrepository, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		xo.open(XREPOSITORY_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xrepository.getID().toURI());
		
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
	
	/**
	 * Encode the given {@link XBaseRepository} as an XML element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xmodel an {@link XBaseRepository}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xrepository&gt; and ending with the same
	 *            &lt;/xrepository&gt; is written to. White space is permitted
	 *            but not required.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseRepository xrepository, XmlOut xo) {
		toXml(xrepository, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XBaseModel} as an XML element.
	 * 
	 * @param xmodel an {@link XBaseModel}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xmodel&gt; and ending with the same &lt;/xmodel&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible objects and fields instead
	 *            of throwing an exception
	 * @param saveChangeLog if true, the change log is saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseModel xmodel, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xmodel.getRevisionNumber();
		
		xo.open(XMODEL_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xmodel.getID().toURI());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
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
			}
		}
		
		xo.close(XMODEL_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XBaseModel} as an XML element, including revision
	 * numbers and ignoring inaccessible entities.
	 * 
	 * @param xmodel an {@link XBaseModel}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xmodel&gt; and ending with the same &lt;/xmodel&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseModel xmodel, XmlOut xo) {
		toXml(xmodel, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XBaseObject} as an XML element.
	 * 
	 * @param xobject an {@link XObject}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xobject&gt; and ending with the same &lt;/xobject&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @param ignoreInaccessible ignore inaccessible fields instead of throwing
	 *            an exception
	 * @param saveChangeLog if true, any object change log is saved
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseObject xobject, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible, boolean saveChangeLog) {
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xobject.getRevisionNumber();
		
		xo.open(XOBJECT_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xobject.getID().toURI());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
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
			}
		}
		
		xo.close(XOBJECT_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XBaseObject} as an XML element, including
	 * revision numbers and ignoring inaccessible entities.
	 * 
	 * @param xobject an {@link XBaseObject}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xobject&gt; and ending with the same &lt;/xobject&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseObject xobject, XmlOut xo) {
		toXml(xobject, xo, true, true, true);
	}
	
	/**
	 * Encode the given {@link XBaseField} as an XML element.
	 * 
	 * @param xfield an {@link XBaseField}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 * @param saveRevision true if revision numbers should be saved to the xml
	 *            file.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseField xfield, XmlOut xo, boolean saveRevision) {
		
		// get values before outputting anything to prevent incomplete XML
		// elements on errors
		XValue xvalue = xfield.getValue();
		long rev = xfield.getRevisionNumber();
		
		xo.open(XFIELD_ELEMENT);
		xo.attribute(XmlUtils.XID_ATTRIBUTE, xfield.getID().toURI());
		if(saveRevision) {
			xo.attribute(REVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		if(xvalue != null) {
			XmlValue.toXml(xvalue, xo);
		}
		
		xo.close(XFIELD_ELEMENT);
		
	}
	
	/**
	 * Encode the given {@link XBaseField} as an XML element, including revision
	 * numbers.
	 * 
	 * @param xfield an {@link XBaseField}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseField xfield, XmlOut xo) {
		toXml(xfield, xo, true);
	}
	
	/**
	 * Encode the given {@link XChangeLog} as an XML element.
	 * 
	 * @param log an {@link XChangeLog}
	 * @param out the {@link XmlOut} that a partial XML document starting with
	 *            &lt;xfield&gt; and ending with the same &lt;/xfield&gt; is
	 *            written to. White space is permitted but not required.
	 */
	public static void toXml(XChangeLog log, XmlOut xo) {
		
		// get values before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = log.getFirstRevisionNumber();
		Iterator<XEvent> events = log.getEventsBetween(0, Long.MAX_VALUE);
		
		xo.open(XCHANGELOG_ELEMENT);
		if(rev != 0) {
			xo.attribute(STARTREVISION_ATTRIBUTE, Long.toString(rev));
		}
		
		while(events.hasNext()) {
			XmlEvent.toXml(events.next(), xo, log.getBaseAddress(), log);
		}
		
		xo.close(XCHANGELOG_ELEMENT);
		
	}
	
}
