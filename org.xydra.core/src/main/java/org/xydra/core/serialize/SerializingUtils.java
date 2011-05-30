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
	
	protected static void checkElementType(MiniElement element, String expectedName) {
		if(element == null || !element.getType().equals(expectedName)) {
			throw new ParsingError(element, "Expected <" + expectedName + "> element.");
		}
	}
	
	protected static ChangeType getChangeType(MiniElement element) {
		Object typeString = getRequiredAttribute(element, TYPE_ATTRIBUTE);
		ChangeType type = ChangeType.fromString(typeString.toString());
		if(type == null) {
			throw new ParsingError(element, "Attribute '" + TYPE_ATTRIBUTE
			        + "' does not contain a valid type, but '" + typeString + "'");
		}
		return type;
	}
	
	protected static XID getOptionalXidAttribute(MiniElement element, String attributeName, XID def) {
		Object xidString = element.getAttribute(attributeName);
		if(xidString == null) {
			return def;
		}
		return XX.toId(xidString.toString());
	}
	
	protected static Object getRequiredAttribute(MiniElement element, String attribute) {
		Object value = element.getAttribute(attribute);
		if(value == null) {
			throw new ParsingError(element, "Missing attribute '" + attribute + "'.");
		}
		return value;
	}
	
	protected static XID getRequiredXidAttribute(MiniElement element) {
		Object xidString = getRequiredAttribute(element, XID_ATTRIBUTE);
		return XX.toId(xidString.toString());
	}
	
	@SuppressWarnings("null")
	protected static XAddress getAddress(MiniElement element, XAddress context) {
		
		boolean match = (context != null);
		
		XID repoId = getOptionalXidAttribute(element, REPOSITORYID_ATTRIBUTE, match ? context
		        .getRepository() : null);
		match = match && XI.equals(repoId, context.getRepository());
		XID modelId = getOptionalXidAttribute(element, MODELID_ATTRIBUTE, match ? context
		        .getModel() : null);
		match = match && XI.equals(modelId, context.getModel());
		XID objectId = getOptionalXidAttribute(element, OBJECTID_ATTRIBUTE, match ? context
		        .getObject() : null);
		match = match && XI.equals(objectId, context.getObject());
		XID fieldId = getOptionalXidAttribute(element, FIELDID_ATTRIBUTE, match ? context
		        .getField() : null);
		
		return XX.toAddress(repoId, modelId, objectId, fieldId);
	}
	
	@SuppressWarnings("null")
	protected static void setAddress(XAddress target, XydraOut out, XAddress context) {
		
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
	
	protected static int toInteger(Object value) {
		
		if(value == null) {
			return 0;
		} else if(value instanceof Number) {
			return ((Number)value).intValue();
		}
		
		try {
			return Integer.parseInt(value.toString());
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
			return Double.parseDouble(value.toString());
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
			return Boolean.valueOf(value.toString());
		}
	}
	
	protected static XID toId(Object object) {
		return object == null ? null : XX.toId(object.toString());
	}
	
	protected static XAddress toAddress(Object object) {
		return object == null ? null : XX.toAddress(object.toString());
	}
	
	protected static String toString(Object object) {
		return object == null ? null : object.toString();
	}

	protected static long toLong(Object value) {
    	
    	if(value == null) {
    		return 0l;
    	} else if(value instanceof Number) {
    		return ((Number)value).longValue();
    	}
    	
    	try {
    		return Long.parseLong(value.toString());
    	} catch(Exception e) {
    		throw new RuntimeException("Expected a valid long, got " + value, e);
    	}
    	
    }
	
}
