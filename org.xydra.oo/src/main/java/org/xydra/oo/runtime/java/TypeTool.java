package org.xydra.oo.runtime.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.xydra.annotations.RunsInGWT;


/**
 * @author xamde
 * 
 */
@RunsInGWT(false)
public class TypeTool {
    
    /**
     * @param method
     * @return the component type of a generic type or null
     */
    public static Class<?> getComponentType(Method method) {
        return getComponentType(method.getGenericReturnType());
    }
    
    public static Class<?> getRawType(Type genericType) {
        if(genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            return parameterizedType.getRawType().getClass();
        }
        assert genericType instanceof Class<?>;
        return (Class<?>)genericType;
    }
    
    public static Class<?> getComponentType(Type genericType) {
        if(genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
            if(fieldArgTypes.length == 0)
                return null;
            if(fieldArgTypes.length > 1)
                throw new IllegalArgumentException(
                        "Multiple generic types found - which is the component type?");
            return (Class<?>)fieldArgTypes[0];
        } else if(genericType instanceof Class) {
            Class<?> type = (Class<?>)genericType;
            if(type.isArray()) {
                return type.getComponentType();
            } else {
                return null;
            }
        } else
            return null;
    }
    
    public static Class<?> getComponentType(Field field) {
        return getComponentType(field.getGenericType());
    }
    
}
