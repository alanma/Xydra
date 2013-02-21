package org.xydra.oo.runtime.shared;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.runtime.java.TypeTool;


public class OOTypeBridge {
    
    private static final Logger log = LoggerFactory.getLogger(OOTypeBridge.class);
    
    /** Can not be used in collections */
    private static final Set<Class<?>> xydraTypes = new HashSet<Class<?>>(Arrays.asList(
    
    XAddress.class, XAddressListValue.class, XAddressSetValue.class, XAddressSortedSetValue.class,
            XBooleanListValue.class, XBooleanValue.class, XBinaryValue.class,
            XDoubleListValue.class, XDoubleValue.class, XID.class, XIDListValue.class,
            XIDSetValue.class, XIDSortedSetValue.class, XIntegerListValue.class,
            XIntegerValue.class, XLongListValue.class, XLongValue.class, XStringListValue.class,
            XStringSetValue.class, XStringValue.class
    
    ));
    
    /** Can not be used in collections */
    private static final Set<Class<?>> javaPrimitiveTypes = new HashSet<Class<?>>(Arrays.asList(
    
    boolean.class, byte.class, double.class, int.class, long.class
    
    ));
    
    private static final Set<Class<?>> javaDirectObjectTypes = new HashSet<Class<?>>(Arrays.asList(
    
    Boolean.class, Byte.class, Double.class, Integer.class, Long.class,
    
    String.class, XID.class, XAddress.class
    
    ));
    
    /** FIXME Plus the mapped objects */
    private static final Set<Class<?>> javaIndirectObjectTypes = new HashSet<Class<?>>();
    
    /** Not all collections can be used with all types */
    private static final Set<Class<?>> javaCollectionTypes = new HashSet<Class<?>>(Arrays.asList(
    
    Set.class, List.class, SortedSet.class
    
    ));
    
    /**
     * @param type
     * @return true iff we can generate getters and setters that map the given
     *         type to a defined Xydra type
     */
    public static boolean isTranslatableSingleType(Class<?> type) {
        assert type != null;
        
        return
        // trivially translatable 1:!
        xydraTypes.contains(type)
        
        || javaPrimitiveTypes.contains(type)
        
        || javaDirectObjectTypes.contains(type)
        
        || javaIndirectObjectTypes.contains(type)
        
        || isMappedType(type);
        
    }
    
    /**
     * @param type
     * @return true iff type is a Java type mapped via XID to a Xydra object
     */
    public static boolean isMappedType(@CanBeNull Class<?> type) {
        if(type == null)
            return false;
        try {
            Method method = type.getMethod("getId");
            return method != null && method.getReturnType().equals(XID.class);
        } catch(NoSuchMethodException e) {
            return false;
        } catch(SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    static boolean isToBeGeneratedCollectionType(Class<?> type, Class<?> componentType,
            Set<Class<?>> toBeGeneratedTypes) {
        if(isCollectionType(type)) {
            boolean componentOk = toBeGeneratedTypes.contains(componentType);
            if(!componentOk)
                log.warn("Translatable collection type (" + type
                        + ") with untranslatable component type: " + componentType);
            return componentOk;
        } else {
            return false;
        }
    }
    
    public static String getAnnotatedFieldId(Class<?> c) {
        org.xydra.oo.Field annot = c.getAnnotation(org.xydra.oo.Field.class);
        if(annot == null)
            return null;
        return annot.value();
    }
    
    public static String getAnnotatedFieldId(Method method) {
        org.xydra.oo.Field annot = method.getAnnotation(org.xydra.oo.Field.class);
        if(annot == null)
            return null;
        return annot.value();
    }
    
    public static boolean isCollectionType(Class<?> type) {
        return javaCollectionTypes.contains(type) || type.isArray();
    }
    
    public static boolean isTranslatableCollectionType(Class<?> type, Class<?> compType) {
        return isCollectionType(type) && isTranslatableSingleType(compType);
    }
    
    public static void main(String[] args) {
        System.out.println("All types");
        for(ValueType v : ValueType.values()) {
            System.out.println(v.name());
        }
    }
    
    public static boolean isToBeGeneratedCollectionType(Field field,
            Set<Class<?>> toBeGeneratedTypes) {
        Class<?> type = field.getType();
        Class<?> compType = TypeTool.getComponentType(field);
        return isToBeGeneratedCollectionType(type, compType, toBeGeneratedTypes);
    }
    
}
