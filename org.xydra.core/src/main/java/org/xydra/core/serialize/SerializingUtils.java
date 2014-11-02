package org.xydra.core.serialize;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.core.XX;
import org.xydra.core.serialize.json.ParseNumber;
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
public class SerializingUtils {
    
    public static final String FIELDID_ATTRIBUTE = "fieldId";
    public static final String MODELID_ATTRIBUTE = "modelId";
    public static final String OBJECTID_ATTRIBUTE = "objectId";
    public static final String REPOSITORYID_ATTRIBUTE = "repositoryId";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String XID_ATTRIBUTE = "xid";
    
    public static void checkElementType(@NeverNull XydraElement element, String expectedName) {
        if(element == null || !element.getType().equals(expectedName)) {
            throw new ParsingException(element, "Expected <" + expectedName + "> element.");
        }
    }
    
    public static ChangeType getChangeType(XydraElement element) {
        Object typeString = getRequiredAttribute(element, TYPE_ATTRIBUTE);
        ChangeType type = ChangeType.fromString(toString(typeString));
        if(type == null) {
            throw new ParsingException(element, "Attribute '" + TYPE_ATTRIBUTE
                    + "' does not contain a valid type, but '" + typeString + "'");
        }
        return type;
    }
    
    public static XId getOptionalXidAttribute(XydraElement element, String attributeName, XId def) {
        String xidString = toString(element.getAttribute(attributeName));
        if(xidString == null) {
            return def;
        }
        return XX.toId(xidString);
    }
    
    public static Object getRequiredAttribute(XydraElement element, String attribute) {
        Object value = element.getAttribute(attribute);
        if(value == null) {
            throw new ParsingException(element, "Missing attribute '" + attribute + "'.");
        }
        return value;
    }
    
    public static XId getRequiredXidAttribute(XydraElement element) {
        return XX.toId(toString(getRequiredAttribute(element, XID_ATTRIBUTE)));
    }
    
    @SuppressWarnings("null")
    public static XAddress getAddress(XydraElement element, XAddress context) {
        
        boolean match = (context != null);
        
        /*
         * NullPointerExceptions cannot happen here because context is only
         * accessed, when match is true
         */
        XId repoId = getOptionalXidAttribute(element, REPOSITORYID_ATTRIBUTE,
                match ? context.getRepository() : null);
        match = match && XI.equals(repoId, context.getRepository());
        XId modelId = getOptionalXidAttribute(element, MODELID_ATTRIBUTE,
                match ? context.getModel() : null);
        match = match && XI.equals(modelId, context.getModel());
        XId objectId = getOptionalXidAttribute(element, OBJECTID_ATTRIBUTE,
                match ? context.getObject() : null);
        match = match && XI.equals(objectId, context.getObject());
        XId fieldId = getOptionalXidAttribute(element, FIELDID_ATTRIBUTE,
                match ? context.getField() : null);
        
        return XX.toAddress(repoId, modelId, objectId, fieldId);
    }
    
    @SuppressWarnings("null")
    public static void setAddress(XAddress target, XydraOut out, XAddress context) {
        
        boolean match = (context != null);
        
        /*
         * NullPointerExceptions cannot happen here because "context" is only
         * accessed, when match is true
         */
        
        XId repoId = target.getRepository();
        match = match && XI.equals(repoId, context.getRepository());
        if(repoId != null && !match) {
            out.attribute(REPOSITORYID_ATTRIBUTE, repoId);
        }
        
        XId modelId = target.getModel();
        match = match && XI.equals(modelId, context.getModel());
        if(modelId != null && !match) {
            out.attribute(MODELID_ATTRIBUTE, modelId);
        }
        
        XId objectId = target.getObject();
        match = match && XI.equals(objectId, context.getObject());
        if(objectId != null && !match) {
            out.attribute(OBJECTID_ATTRIBUTE, objectId);
        }
        
        XId fieldId = target.getField();
        match = match && XI.equals(fieldId, context.getField());
        if(fieldId != null && !match) {
            out.attribute(FIELDID_ATTRIBUTE, fieldId);
        }
        
    }
    
    public static int toInteger(Object value) {
        
        if(value == null) {
            return 0;
        } else if(value instanceof Number) {
            return ((Number)value).intValue();
        }
        
        try {
            return ParseNumber.parseInt(toString(value));
        } catch(Exception e) {
            throw new RuntimeException("Expected a valid integer, got " + value, e);
        }
        
    }
    
    public static double toDouble(Object value) {
        
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
    
    public static boolean toBoolean(Object value) {
        
        if(value == null) {
            return false;
        } else if(value instanceof Boolean) {
            return (Boolean)value;
        } else {
            return Boolean.valueOf(toString(value));
        }
    }
    
    public static XId toId(Object object) {
        return object == null ? null : XX.toId(toString(object));
    }
    
    public static XAddress toAddress(Object object) {
        return object == null ? null : XX.toAddress(toString(object));
    }
    
    public static String toString(Object object) {
        if(object instanceof String) {
            /*
             * TODO workaround for GWT bug
             * http://code.google.com/p/google-web-toolkit
             * /issues/detail?id=4301: avoid using String#toString()
             * 
             * Can be removed when bug is fixed. Not yet as of 2012-03-20.
             */
            return (String)object;
        }
        return object == null ? null : object.toString();
    }
    
    public static long toLong(Object value) {
        
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
