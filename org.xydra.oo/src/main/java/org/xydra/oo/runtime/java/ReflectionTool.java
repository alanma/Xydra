package org.xydra.oo.runtime.java;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.xydra.base.IHasXID;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.generator.codespec.NameUtils;
import org.xydra.oo.runtime.shared.OOTypeBridge;
import org.xydra.oo.runtime.shared.OOTypeMapping;


public class ReflectionTool {
    
    private static final Logger log = LoggerFactory.getLogger(ReflectionTool.class);
    
    public static enum KindOfMethod {
        Get("get"), Set("set"), Is("is"), GetCollection(null);
        
        String prefix;
        
        private KindOfMethod(String prefix) {
            this.prefix = prefix;
        }
    }
    
    public static String toDebug(Method method) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "(..)";
    }
    
    public static KindOfMethod extractKindOfMethod(Method method) {
        String name = method.getName();
        assert name != null;
        for(KindOfMethod m : KindOfMethod.values()) {
            if(m.prefix != null && name.startsWith(m.prefix)) {
                return m;
            }
        }
        
        if(OOTypeBridge.isCollectionType(method.getReturnType())) {
            return KindOfMethod.GetCollection;
        } else {
            return null;
        }
    }
    
    public static String extractFieldIdFromMethod(Method method) {
        String fieldId = OOTypeBridge.getAnnotatedFieldId(method);
        if(fieldId == null) {
            String name = method.getName();
            for(KindOfMethod m : KindOfMethod.values()) {
                if(m.prefix != null && name.startsWith(m.prefix)) {
                    fieldId = name.substring(m.prefix.length());
                    fieldId = NameUtils.firstLetterLowercased(fieldId);
                    break;
                }
            }
            if(fieldId == null) {
                if(OOTypeBridge.isCollectionType(method.getReturnType())) {
                    fieldId = method.getName();
                } else {
                    log.warn("No fieldId extractable from "
                            + method.getDeclaringClass().getCanonicalName() + "."
                            + method.getName() + "()");
                }
            }
            log.warn("Using fieldId '" + fieldId + "' extracted from method name '" + name
                    + "' - Please @Field annotate");
        }
        return fieldId;
    }
    
    public static boolean isKnownTranslatableCollectionType(Class<?> type, Class<?> componentType) {
        // check collection type
        if(OOTypeBridge.isCollectionType(type)) {
            // check component type of collection
            if(OOTypeBridge.isTranslatableSingleType(componentType)) {
                return true;
            } else {
                throw new RuntimeException("Dont know how to translate collection of type "
                        + type.getCanonicalName() + " with element type "
                        + componentType.getCanonicalName());
            }
        }
        return false;
    }
    
    public static boolean isMappedToXydra(Class<?> type) {
        Method m;
        try {
            m = type.getMethod("getId");
            return m != null && m.getReturnType().equals(XID.class);
        } catch(NoSuchMethodException e) {
            return false;
        } catch(SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T> T toJavaInstance(Class<T> interfaze, XWritableModel model, XID id) {
        T instance = (T)Proxy.newProxyInstance(interfaze.getClassLoader(),
                new Class<?>[] { interfaze }, new OOJavaOnlyProxy(model, id));
        return instance;
    }
    
    public static XValue convertToXydra(Object paramValue, Class<?> paramType,
            Class<?> paramComponentType) {
        // assert param.getClass().equals(paramType) : "param.class=" +
        // param.getClass()
        // + " paramType=" + paramType;
        if(paramValue instanceof IHasXID) {
            IHasXID hasXid = (IHasXID)paramValue;
            return hasXid.getId();
        }
        
        OOTypeMapping mapping = getMapping(paramType, paramComponentType);
        if(mapping == null) {
            // try via interfaces
            for(Class<?> interfaze : paramType.getInterfaces()) {
                mapping = getMapping(interfaze, paramComponentType);
                if(mapping != null)
                    break;
            }
        }
        if(mapping == null) {
            throw new RuntimeException("Not yet handling type="
                    + paramType.getCanonicalName()
                    + " "
                    + (paramComponentType == null ? "NONE" : "compType="
                            + paramComponentType.getCanonicalName()));
        }
        // assert mapping.getJavaType().equals(paramType);
        XValue v = mapping.toXydra(paramValue);
        return v;
    }
    
    private static OOTypeMapping getMapping(Class<?> type, Class<?> componentType) {
        assert type != null;
        if(OOTypeBridge.isMappedType(componentType)) {
            // mapping is always a kind of XID
            return OOTypeMapping.getMapping(type, XID.class);
        } else {
            // try as simple type
            return OOTypeMapping.getMapping(type, componentType);
        }
    }
    
    /**
     * @param collectionType
     * @param collectionComponentType
     * @return ...
     */
    public static XCollectionValue<XValue> createCollection(Class<?> collectionType,
            Class<?> collectionComponentType) {
        if(OOTypeBridge.isCollectionType(collectionType)) {
            OOTypeMapping mapping = getMapping(collectionType, collectionComponentType);
            if(mapping == null) {
                throw new RuntimeException("Not yet handling type="
                        + collectionType.getCanonicalName() + " compType="
                        + collectionComponentType);
            }
            return mapping.createEmptyXydraCollection();
        } else {
            throw new RuntimeException("Not yet handling type=" + collectionType.getCanonicalName()
                    + " compType=" + collectionComponentType);
        }
    }
    
}
