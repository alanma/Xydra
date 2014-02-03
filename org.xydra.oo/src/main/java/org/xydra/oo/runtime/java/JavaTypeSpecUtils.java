package org.xydra.oo.runtime.java;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.base.value.XCollectionValue;
import org.xydra.oo.runtime.shared.BaseTypeSpec;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.SharedTypeMapping;
import org.xydra.oo.runtime.shared.SharedTypeSystem;
import org.xydra.oo.runtime.shared.TypeSpec;


public class JavaTypeSpecUtils {
    
    /**
     * @param type may not be an array
     * @param componentType must be null if type is an array type
     * @param generatedFrom a debug string, not used for identity
     * @return ...
     */
    public static TypeSpec createTypeSpec(Class<?> type, Class<?> componentType,
            String generatedFrom) {
        IBaseType baseType;
        IBaseType componentBaseType;
        if(type.isArray()) {
            baseType = BaseTypeSpec.ARRAY;
            assert type.getComponentType() != null;
            componentBaseType = JavaTypeSpecUtils.createBaseTypeSpec(type.getComponentType());
        } else {
            baseType = JavaTypeSpecUtils.createBaseTypeSpec(type);
            componentBaseType = JavaTypeSpecUtils.createBaseTypeSpec(componentType);
        }
        return new TypeSpec(baseType, componentBaseType, generatedFrom);
    }
    
    /**
     * @param c
     * @return null or wrapped type
     */
    public static IBaseType createBaseTypeSpec(@CanBeNull Class<?> c) {
        if(c == null)
            return null;
        // package is null for primitive types
        String packageName = null;
        if(c.getPackage() != null) {
            packageName = c.getPackage().getName();
        }
        String simpleName;
        if(c.getEnclosingClass() != null) {
            simpleName = c.getEnclosingClass().getSimpleName() + "." + c.getSimpleName();
        } else {
            simpleName = c.getSimpleName();
        }
        return new BaseTypeSpec(packageName, simpleName);
    }
    
    /**
     * @param collectionType
     * @param collectionComponentType
     * @return ...
     * 
     *         TODO This does not work in GWT
     */
    public static XCollectionValue<?> createCollectionValue(Class<?> collectionType,
            Class<?> collectionComponentType) {
        SharedTypeMapping mapping = SharedTypeSystem.getMapping(createTypeSpec(collectionType,
                collectionComponentType, "JavaTypeSpecUtils"));
        return mapping.createEmptyXydraCollection();
    }
    
    /**
     * @param javaBaseType @NeverNull
     * @param javaComponentType @CanBeNull
     * @return a mapping for the given types
     */
    public static SharedTypeMapping getMapping(@NeverNull Class<?> javaBaseType,
            Class<?> javaComponentType) {
        assert javaBaseType != null;
        return SharedTypeMapping.getMapping(createTypeSpec(javaBaseType, javaComponentType,
                "JavaTypeSpecUtils"));
    }
    
    public static TypeSpec createTypeSpec(Class<?> javaBaseType, Class<?> javaComponentType) {
        IBaseType baseType = createBaseTypeSpec(javaBaseType);
        IBaseType componentType = createBaseTypeSpec(javaComponentType);
        return new TypeSpec(baseType, componentType, "JavaTypeSpecUtils");
    }
    
}
