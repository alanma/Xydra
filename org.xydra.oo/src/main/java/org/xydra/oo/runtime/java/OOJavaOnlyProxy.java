package org.xydra.oo.runtime.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.runtime.shared.CollectionProxy;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.runtime.shared.OOProxy;
import org.xydra.oo.runtime.shared.OOTypeBridge;
import org.xydra.oo.runtime.shared.OOTypeMapping;
import org.xydra.oo.runtime.shared.SetProxy;
import org.xydra.oo.runtime.shared.SortedSetProxy;


/**
 * Java dynamic proxy representing a Java Object that is mapped to a Xydra
 * Object
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class OOJavaOnlyProxy implements InvocationHandler {
    
    private static final Logger log = LoggerFactory.getLogger(OOJavaOnlyProxy.class);
    
    private OOProxy oop;
    
    public OOJavaOnlyProxy(XWritableModel model, XID objectId) {
        this.oop = new OOProxy(model, objectId);
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /* handle Object.class methods special */
        if(Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if("equals".equals(name)) {
                return proxy == args[0];
            } else if("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if("toString".equals(name)) {
                return proxy.getClass().getName() + "@"
                        + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        
        try {
            return invoke(method, args);
        } catch(Exception | Error e) {
            log.error(method.getReturnType() + " " + method.getName() + "(..)", e);
            throw e;
        }
    }
    
    private Object invoke(Method method, Object[] args) {
        assert args == null && method.getParameterTypes().length == 0
                || (args != null && method.getParameterTypes().length == args.length) : args == null ? "args null"
                : "args.len=" + args.length;
        
        /* Determine method */
        String name = method.getName();
        if(name.equals("getId") && args == null) {
            return this.oop.getId();
        }
        
        ReflectionTool.KindOfMethod kindOfMethod = ReflectionTool.extractKindOfMethod(method);
        if(kindOfMethod == null) {
            throw new RuntimeException("Cannot handle method " + ReflectionTool.toDebug(method));
        }
        
        if(!this.oop.hasXObject()) {
            throw new RuntimeException("Object '" + this.oop.getId() + "' does not exist in XModel");
        }
        
        /* Determine fieldId */
        String fieldId = ReflectionTool.extractFieldIdFromMethod(method);
        assert fieldId != null;
        
        /* ================================ GET ================================ */
        if(kindOfMethod == ReflectionTool.KindOfMethod.Get) {
            if(args != null) {
                throw new RuntimeException("getter " + ReflectionTool.toDebug(method)
                        + " does not take arguments, has " + args.length);
            }
            Class<?> returnType = method.getReturnType();
            if(OOTypeBridge.isTranslatableSingleType(returnType)) {
                return _get_(returnType, null, fieldId, false);
            }
            Class<?> componentType = TypeTool.getComponentType(method);
            if(ReflectionTool.isKnownTranslatableCollectionType(returnType, componentType)) {
                return _get_(returnType, componentType, fieldId, false);
            }
            throw new RuntimeException("Cannot handle type " + returnType.getCanonicalName()
                    + " in setter for " + fieldId);
        } else if(kindOfMethod == ReflectionTool.KindOfMethod.Is) {
            if(args != null) {
                throw new RuntimeException("isXXX() method does not take arguments");
            }
            assert method.getReturnType().equals(Boolean.class);
            // TODO deal with old style booleans (missing field = false)
            Boolean b = (Boolean)_get_(Boolean.class, null, fieldId, false);
            return b;
        } else if(kindOfMethod == ReflectionTool.KindOfMethod.GetCollection) {
            Class<?> returnType = method.getReturnType();
            assert OOTypeBridge.isCollectionType(returnType);
            Class<?> componentType = TypeTool.getComponentType(method);
            return _get_(returnType, componentType, fieldId, true);
        } else
        /* ================================ SET ================================ */
        if(kindOfMethod == ReflectionTool.KindOfMethod.Set) {
            if(args == null || args.length != 1) {
                throw new RuntimeException("setXXX() method needs *1* argument, has "
                        + (args == null ? "none" : args.length));
            }
            Class<?> paramType = method.getParameterTypes()[0];
            if(OOTypeBridge.isTranslatableSingleType(paramType)) {
                _set_(args[0], paramType, null, fieldId);
                return null;
            }
            Class<?> componentType = TypeTool
                    .getComponentType(method.getGenericParameterTypes()[0]);
            if(ReflectionTool.isKnownTranslatableCollectionType(paramType, componentType)) {
                _set_(args[0], paramType, componentType, fieldId);
                return null;
            }
            throw new RuntimeException("Setter for type '" + paramType.getCanonicalName()
                    + "' not yet impl");
        } else {
            // TODO impl
            throw new RuntimeException("Don't know how to handle method '" + name + "' with kind '"
                    + kindOfMethod + "'");
        }
    }
    
    // FIXME TODO support all these types
    
    // // single
    //
    // // enum
    // EnumFieldProperty<Enum<T>>;
    //
    // // generic
    // ExtensibleFieldProperty< p,XValue>;
    //
    // // TODO tricky to reference outside of model
    // XAddressFieldProperty;
    //
    // // generic collection
    // SetProxy<X,J>
    //
    // // collection XID
    //
    // // TODO tricky to reference outside of model
    // XAddressListFieldProperty;
    // XAddressSortedSetFieldProperty;
    
    // ================== HERE =================
    
    /**
     * @param javaType
     * @param javaComponentType @CanBeNull is only set if javaType is a
     *            collection type
     * @param fieldName
     * @return
     */
    private Object _get_(Class<?> javaType, Class<?> javaComponentType, String fieldName,
            boolean returnCollectionsAsLiveViews) {
        XValue v = this.oop.getValue(fieldName);
        if(v == null) {
            if(returnCollectionsAsLiveViews && OOTypeBridge.isCollectionType(javaType)) {
                return liveCollection(javaType, javaComponentType, fieldName, this.oop.getXModel(),
                        this.oop.getXObject());
            } else {
                // handle primitive return values
                if(javaType.equals(byte.class))
                    return 0;
                if(javaType.equals(int.class))
                    return 0;
                if(javaType.equals(double.class))
                    return 0d;
                if(javaType.equals(long.class))
                    return 0l;
                if(javaType.equals(boolean.class))
                    return false;
                
                return null;
            }
        }
        
        Object j = convertToJava(fieldName, v, javaType, javaComponentType, this.oop.getXModel(),
                this.oop.getXObject());
        return j;
    }
    
    /**
     * @param param
     * @param paramComponentType
     * @param xydraType
     * @param fieldId
     */
    private void _set_(Object param, Class<?> paramType, Class<?> paramComponentType,
            String fieldName) {
        XValue v = ReflectionTool.convertToXydra(param, param.getClass(), paramComponentType);
        
        this.oop.setValue(fieldName, v);
    }
    
    /**
     * @param fieldName
     * @param v
     * @param type
     * @param componentType
     * @param model
     * @param object
     * @return ...
     */
    public static Object convertToJava(String fieldName, XValue v, Class<?> type,
            Class<?> componentType, XWritableModel model, XWritableObject object) {
        if(ReflectionTool.isMappedToXydra(type)) {
            // mapped instance
            assert v instanceof XID;
            return ReflectionTool.toJavaInstance(type, model, (XID)v);
        }
        // TODO what if compType is mapped? i.e. List<IPerson>
        if(OOTypeBridge.isCollectionType(type) && ReflectionTool.isMappedToXydra(componentType)) {
            return liveCollection(type, componentType, fieldName, model, object);
        }
        
        // else
        OOTypeMapping mapping = OOTypeMapping.getMapping(type, componentType);
        if(mapping == null) {
            throw new RuntimeException("Found no mapping for type=" + type.getCanonicalName()
                    + " compType="
                    + (componentType == null ? "NONE" : componentType.getCanonicalName()));
        }
        assert mapping.getJavaType().equals(type);
        assert mapping.getJavaComponentType() == null;
        // Class<?> x = mapping.getXydraType();
        // assert x.isAssignableFrom(x);
        Object o = mapping.toJava((XValue)v);
        return o;
    }
    
    public static <J, C> Object liveCollection(final Class<J> type, final Class<C> componentType,
            final String fieldName, final XWritableModel model, final XWritableObject object) {
        assert OOTypeBridge.isCollectionType(type) : "type=" + type.getCanonicalName()
                + ", compTyp=" + componentType.getCanonicalName();
        
        CollectionProxy.ITransformer<XCollectionValue<XValue>,XValue,J,C> t
        
        = new CollectionProxy.ITransformer<XCollectionValue<XValue>,XValue,J,C>() {
            
            @SuppressWarnings("unchecked")
            @Override
            public C toJavaComponent(XValue xydraValue) {
                return (C)convertToJava(fieldName, xydraValue, type, componentType, model, object);
            }
            
            @Override
            public XValue toXydraComponent(C javaValue) {
                return ReflectionTool.convertToXydra(javaValue, type, componentType);
            }
            
            @Override
            public XCollectionValue<XValue> createCollection() {
                return ReflectionTool.createCollection(type, componentType);
            }
            
        };
        
        if(type.equals(List.class)) {
            return new ListProxy<XCollectionValue<XValue>,XValue,J,C>(object, XX.toId(fieldName), t);
        } else if(type.equals(Set.class)) {
            return new SetProxy<XCollectionValue<XValue>,XValue,J,C>(object, XX.toId(fieldName), t);
        } else if(type.equals(SortedSet.class)) {
            return new SortedSetProxy<XCollectionValue<XValue>,XValue,J,C>(object,
                    XX.toId(fieldName), t);
        } else
            throw new IllegalArgumentException(
                    "Cannot create a live view for a collection of type " + type.getCanonicalName());
    }
}
