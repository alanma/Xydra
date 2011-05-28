package org.xydra.core.serialize;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.index.XI;


/**
 * Methods used by various other Xml* classes.
 * 
 * @author dscharrer
 * 
 */
class SerializingUtils {
	
	protected static final String FIELDID_ATTRIBUTE = "fieldId";
	protected static final String MODELID_ATTRIBUTE = "modelId";
	protected static final String OBJECTID_ATTRIBUTE = "objectId";
	protected static final String REPOSITORYID_ATTRIBUTE = "repositoryId";
	protected static final String TYPE_ATTRIBUTE = "type";
	protected static final String XID_ATTRIBUTE = "xid";
	
	protected static void checkElementName(MiniElement xml, String expectedName) {
		if(!xml.getType().equals(expectedName)) {
			throw new IllegalArgumentException("Given element " + xml.getType() + " is not an <"
			        + expectedName + "> element.");
		}
	}
	
	protected static ChangeType getChangeType(MiniElement xml, String elementName) {
		Object typeString = getRequiredAttribute(xml, TYPE_ATTRIBUTE, elementName);
		ChangeType type = ChangeType.fromString(typeString.toString());
		if(type == null) {
			throw new IllegalArgumentException("<" + elementName + ">@" + TYPE_ATTRIBUTE
			        + " does not contain a valid type, but '" + typeString + "'");
		}
		return type;
	}
	
	protected static XID getOptionalXidAttribute(MiniElement xml, String attributeName, XID def) {
		Object xidString = xml.getAttribute(attributeName);
		if(xidString == null) {
			return def;
		}
		return XX.toId(xidString.toString());
	}
	
	protected static Object getRequiredAttribute(MiniElement xml, String attribute, String element) {
		Object value = xml.getAttribute(attribute);
		if(value == null) {
			throw new IllegalArgumentException("Missing attribute " + attribute + "@<" + element
			        + ">");
		}
		return value;
	}
	
	static XID getRequiredXidAttribute(MiniElement xml, String elementName) {
		Object xidString = getRequiredAttribute(xml, XID_ATTRIBUTE, elementName);
		return XX.toId(xidString.toString());
	}
	
	@SuppressWarnings("null")
	protected static XAddress getTarget(MiniElement xml, XAddress context) {
		
		boolean match = (context != null);
		
		XID repoId = getOptionalXidAttribute(xml, REPOSITORYID_ATTRIBUTE, match ? context
		        .getRepository() : null);
		match = match && XI.equals(repoId, context.getRepository());
		XID modelId = getOptionalXidAttribute(xml, MODELID_ATTRIBUTE, match ? context.getModel()
		        : null);
		match = match && XI.equals(modelId, context.getModel());
		XID objectId = getOptionalXidAttribute(xml, OBJECTID_ATTRIBUTE, match ? context.getObject()
		        : null);
		match = match && XI.equals(objectId, context.getObject());
		XID fieldId = getOptionalXidAttribute(xml, FIELDID_ATTRIBUTE, match ? context.getField()
		        : null);
		
		return XX.toAddress(repoId, modelId, objectId, fieldId);
	}
	
	@SuppressWarnings("null")
	protected static void setTarget(XAddress target, XydraOut out, XAddress context) {
		
		boolean match = (context != null);
		
		XID repoId = target.getRepository();
		match = match && XI.equals(repoId, context.getRepository());
		if(repoId != null && !match) {
			out.attribute(REPOSITORYID_ATTRIBUTE, repoId);
		}
		
		XID modelId = target.getModel();
		match = match && XI.equals(modelId, context.getModel());
		if(modelId != null && !match) {
			out.attribute(MODELID_ATTRIBUTE, modelId);
		}
		
		XID objectId = target.getObject();
		match = match && XI.equals(objectId, context.getObject());
		if(objectId != null && !match) {
			out.attribute(OBJECTID_ATTRIBUTE, objectId);
		}
		
		XID fieldId = target.getField();
		match = match && XI.equals(fieldId, context.getField());
		if(fieldId != null && !match) {
			out.attribute(FIELDID_ATTRIBUTE, fieldId);
		}
		
	}
	
}
