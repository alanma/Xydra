package org.xydra.core.serialize;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.index.XI;


/**
 * Methods used by various other Serializing* classes.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
class SerializingUtils {
	
	protected static final String FIELDID_ATTRIBUTE = "fieldId";
	protected static final String MODELID_ATTRIBUTE = "modelId";
	protected static final String OBJECTID_ATTRIBUTE = "objectId";
	protected static final String REPOSITORYID_ATTRIBUTE = "repositoryId";
	protected static final String TYPE_ATTRIBUTE = "type";
	protected static final String XID_ATTRIBUTE = "xid";
	
	protected static void checkElementType(XydraElement element, String expectedName) {
		if(element == null || !element.getType().equals(expectedName)) {
			throw new ParsingError(element, "Expected <" + expectedName + "> element.");
		}
	}
	
	protected static ChangeType getChangeType(XydraElement element) {
		Object typeString = getRequiredAttribute(element, TYPE_ATTRIBUTE);
		ChangeType type = ChangeType.fromString(toString(typeString));
		if(type == null) {
			throw new ParsingError(element, "Attribute '" + TYPE_ATTRIBUTE
			        + "' does not contain a valid type, but '" + typeString + "'");
		}
		return type;
	}
	
	protected static XID getOptionalXidAttribute(XydraElement element, String attributeName, XID def) {
		String xidString = toString(element.getAttribute(attributeName));
		if(xidString == null) {
			return def;
		}
		return XX.toId(xidString);
	}
	
	protected static Object getRequiredAttribute(XydraElement element, String attribute) {
		Object value = element.getAttribute(attribute);
		if(value == null) {
			throw new ParsingError(element, "Missing attribute '" + attribute + "'.");
		}
		return value;
	}
	
	protected static XID getRequiredXidAttribute(XydraElement element) {
		return XX.toId(toString(getRequiredAttribute(element, XID_ATTRIBUTE)));
	}
	
	@SuppressWarnings("null")
	protected static XAddress getAddress(XydraElement element, XAddress context) {
		
		boolean match = (context != null);
		
		/*
		 * NullPointerExceptions cannot happen here because context is only
		 * accessed, when match is true
		 */
		XID repoId = getOptionalXidAttribute(element, REPOSITORYID_ATTRIBUTE,
		        match ? context.getRepository() : null);
		match = match && XI.equals(repoId, context.getRepository());
		XID modelId = getOptionalXidAttribute(element, MODELID_ATTRIBUTE,
		        match ? context.getModel() : null);
		match = match && XI.equals(modelId, context.getModel());
		XID objectId = getOptionalXidAttribute(element, OBJECTID_ATTRIBUTE,
		        match ? context.getObject() : null);
		match = match && XI.equals(objectId, context.getObject());
		XID fieldId = getOptionalXidAttribute(element, FIELDID_ATTRIBUTE,
		        match ? context.getField() : null);
		
		return XX.toAddress(repoId, modelId, objectId, fieldId);
	}
	
	@SuppressWarnings("null")
	protected static void setAddress(XAddress target, XydraOut out, XAddress context) {
		
		boolean match = (context != null);
		
		/*
		 * NullPointerExceptions cannot happen here because "context" is only
		 * accessed, when match is true
		 */
		
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
	
	protected static int toInteger(Object value) {
		
		if(value == null) {
			return 0;
		} else if(value instanceof Number) {
			return ((Number)value).intValue();
		}
		
		try {
			return Integer.parseInt(toString(value));
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid integer, got " + value, e);
		}
		
	}
	
	protected static double toDouble(Object value) {
		
		if(value == null) {
			return 0.d;
		} else if(value instanceof Number) {
			return ((Number)value).doubleValue();
		}
		
		try {
			return Double.parseDouble(toString(value));
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid double, got " + value, e);
		}
		
	}
	
	protected static boolean toBoolean(Object value) {
		
		if(value == null) {
			return false;
		} else if(value instanceof Boolean) {
			return (Boolean)value;
		} else {
			return Boolean.valueOf(toString(value));
		}
	}
	
	protected static XID toId(Object object) {
		return object == null ? null : XX.toId(toString(object));
	}
	
	protected static XAddress toAddress(Object object) {
		return object == null ? null : XX.toAddress(toString(object));
	}
	
	protected static String toString(Object object) {
		if(object instanceof String) {
			/*
			 * FIXME workaround for GWT bug
			 * http://code.google.com/p/google-web-toolkit
			 * /issues/detail?id=4301: avoid using String#toString()
			 */
			return (String)object;
		}
		return object == null ? null : object.toString();
	}
	
	protected static long toLong(Object value) {
		
		if(value == null) {
			return 0l;
		} else if(value instanceof Number) {
			return ((Number)value).longValue();
		}
		
		try {
			return Long.parseLong(toString(value));
		} catch(Exception e) {
			throw new RuntimeException("Expected a valid long, got " + value, e);
		}
		
	}
	
}
