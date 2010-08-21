package org.xydra.core.xml;

import java.util.Iterator;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.session.XAccessException;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.state.impl.memory.TemporaryFieldState;
import org.xydra.core.model.state.impl.memory.TemporaryModelState;
import org.xydra.core.model.state.impl.memory.TemporaryObjectState;
import org.xydra.core.model.state.impl.memory.TemporaryRepositoryState;
import org.xydra.core.value.XValue;


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
	
	static final String XID_ATTRIBUTE = "xid";
	private static final String REVISION_ATTRIBUTE = "revision";
	
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
	
	private static XRepositoryState toRepositoryState(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, XREPOSITORY_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XREPOSITORY_ELEMENT);
		
		XAddress repoAddr = X.getIDProvider().fromComponents(xid, null, null, null);
		XRepositoryState repositoryState = new TemporaryRepositoryState(repoAddr);
		
		// TODO can we just (correctly) assume that TemporaryRepositoryState
		// doesn't need transactions?
		Object trans = repositoryState.beginTransaction();
		
		Iterator<MiniElement> modelElementIt = xml.getElementsByTagName(XMODEL_ELEMENT);
		while(modelElementIt.hasNext()) {
			MiniElement modelElement = modelElementIt.next();
			XModelState modelState = toModelState(modelElement, repoAddr, trans);
			repositoryState.addModelState(modelState);
		}
		
		repositoryState.save(trans);
		
		repositoryState.endTransaction(trans);
		
		return repositoryState;
	}
	
	/**
	 * Get the {@link XRepository} represented by the given XML element.
	 * 
	 * @param xml a partial XML document starting with &lt;xrepository&gt; and
	 *            ending with the same &lt;/xrepository&gt;
	 * @return an {@link XRepository}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XRepository element.
	 */
	public static XRepository toRepository(MiniElement xml) {
		return new MemoryRepository(toRepositoryState(xml));
	}
	
	private static XModelState toModelState(MiniElement xml, XAddress repoAddr, Object trans) {
		
		XmlUtils.checkElementName(xml, XMODEL_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XMODEL_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XMODEL_ELEMENT);
		
		XAddress modelAddr = XX.resolveModel(repoAddr, xid);
		XChangeLogState changeLogState = new MemoryChangeLogState(modelAddr, revision);
		XModelState modelState = new TemporaryModelState(modelAddr, changeLogState);
		modelState.setRevisionNumber(revision);
		
		Object t = trans == null ? modelState.beginTransaction() : trans;
		
		Iterator<MiniElement> objectElementIt = xml.getElementsByTagName(XOBJECT_ELEMENT);
		while(objectElementIt.hasNext()) {
			MiniElement objectElement = objectElementIt.next();
			XObjectState objectState = toObjectState(objectElement, modelAddr, t);
			modelState.addObjectState(objectState);
		}
		
		modelState.save(null);
		
		if(trans == null) {
			modelState.endTransaction(t);
		}
		
		return modelState;
	}
	
	/**
	 * Get the {@link XModel} represented by the given XML element.
	 * 
	 * @param xml a partial XML document starting with &lt;xmodel&gt; and ending
	 *            with the same &lt;/xmodel&gt;
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel element.
	 */
	public static XModel toModel(MiniElement xml) {
		return new MemoryModel(toModelState(xml, null, null));
	}
	
	private static XObjectState toObjectState(MiniElement xml, XAddress modelAddr, Object trans) {
		
		XmlUtils.checkElementName(xml, XOBJECT_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XOBJECT_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XOBJECT_ELEMENT);
		
		XAddress objectAddr = XX.resolveObject(modelAddr, xid);
		XChangeLogState changeLogState = modelAddr != null ? null : new MemoryChangeLogState(
		        objectAddr, revision);
		XObjectState objectState = new TemporaryObjectState(objectAddr, changeLogState);
		objectState.setRevisionNumber(revision);
		
		Object t = trans == null ? objectState.beginTransaction() : trans;
		
		Iterator<MiniElement> fieldElementIt = xml.getElementsByTagName(XFIELD_ELEMENT);
		while(fieldElementIt.hasNext()) {
			MiniElement fieldElement = fieldElementIt.next();
			XFieldState fieldState = toFieldState(fieldElement, objectAddr, t);
			objectState.addFieldState(fieldState);
		}
		
		objectState.save(t);
		
		if(trans == null) {
			objectState.endTransaction(t);
		}
		
		return objectState;
	}
	
	/**
	 * Get the {@link XObject} represented by the given XML element.
	 * 
	 * @param xml a partial XML document starting with &lt;xobject&gt; and
	 *            ending with the same &lt;/xobject&gt;
	 * @return an {@link XObject}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XObject element.
	 */
	public static XObject toObject(MiniElement xml) {
		return new MemoryObject(toObjectState(xml, null, null));
	}
	
	private static XFieldState toFieldState(MiniElement xml, XAddress objectAddr, Object trans) {
		
		XmlUtils.checkElementName(xml, XFIELD_ELEMENT);
		
		XID xid = XmlUtils.getRequiredXidAttribute(xml, XFIELD_ELEMENT);
		
		long revision = getRevisionAttribute(xml, XFIELD_ELEMENT);
		
		XValue xvalue = null;
		
		Iterator<MiniElement> valueElementIt = xml.getElements();
		if(valueElementIt.hasNext()) {
			MiniElement valueElement = valueElementIt.next();
			xvalue = XmlValue.toValue(valueElement);
		}
		
		XAddress fieldAddr = XX.resolveField(objectAddr, xid);
		XFieldState fieldState = new TemporaryFieldState(fieldAddr);
		fieldState.setRevisionNumber(revision);
		fieldState.setValue(xvalue);
		
		fieldState.save(trans);
		return fieldState;
	}
	
	/**
	 * Get the {@link XField} represented by the given XML element.
	 * 
	 * @param xml a partial XML document starting with &lt;xfield&gt; and ending
	 *            with the same &lt;/xfield&gt;
	 * @return an {@link XField}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XField element.
	 */
	public static XField toField(MiniElement xml) {
		return new MemoryField(toFieldState(xml, null, null));
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
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseRepository xrepository, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible) {
		
		xo.open(XREPOSITORY_ELEMENT);
		xo.attribute(XID_ATTRIBUTE, xrepository.getID().toURI());
		
		for(XID modelOd : xrepository) {
			try {
				toXml(xrepository.getModel(modelOd), xo, saveRevision, ignoreInaccessible);
			} catch(XAccessException ae) {
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
		toXml(xrepository, xo, true, true);
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
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseModel xmodel, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible) {
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xmodel.getRevisionNumber();
		
		xo.open(XMODEL_ELEMENT);
		xo.attribute(XID_ATTRIBUTE, xmodel.getID().toURI());
		if(saveRevision)
			xo.attribute(REVISION_ATTRIBUTE, "" + rev);
		
		for(XID objectId : xmodel) {
			try {
				toXml(xmodel.getObject(objectId), xo, saveRevision, ignoreInaccessible);
			} catch(XAccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
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
		toXml(xmodel, xo, true, true);
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
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link XmlValueWriter} for details.
	 */
	public static void toXml(XBaseObject xobject, XmlOut xo, boolean saveRevision,
	        boolean ignoreInaccessible) {
		
		// get revision before outputting anything to prevent incomplete XML
		// elements on errors
		long rev = xobject.getRevisionNumber();
		
		xo.open(XOBJECT_ELEMENT);
		xo.attribute(XID_ATTRIBUTE, xobject.getID().toURI());
		if(saveRevision)
			xo.attribute(REVISION_ATTRIBUTE, "" + rev);
		
		for(XID fieldId : xobject) {
			try {
				toXml(xobject.getField(fieldId), xo, saveRevision);
			} catch(XAccessException ae) {
				if(!ignoreInaccessible) {
					throw ae;
				}
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
		toXml(xobject, xo, true, true);
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
		xo.attribute(XID_ATTRIBUTE, xfield.getID().toURI());
		if(saveRevision)
			xo.attribute(REVISION_ATTRIBUTE, "" + rev);
		
		if(xvalue != null)
			XmlValue.toXml(xvalue, xo);
		
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
	
}
