package org.xydra.oo.runtime.java;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.value.ValueType;
import org.xydra.oo.runtime.shared.OOTypeBridge;
import org.xydra.oo.runtime.shared.OOTypeMapping;


/**
 * Use during development of {@link OOTypeMapping}
 * 
 * @author xamde
 * 
 */
public class OOValueTypeGen {
    
    public static void main(String[] args) {
        System.out.println("// trivial mappings for built-in Xydra types");
        for(ValueType v : ValueType.values()) {
            if(v.isSingle()) {
                dumpEnumValue(v.name(), v.getXydraInterface(), null, v.getXydraInterface());
            } else {
                if(v.isSet()) {
                    if(v.isSortedCollection()) {
                        // sortedset
                        dumpEnumValue(v.name(), SortedSet.class, ValueType.getComponentType(v),
                                v.getXydraInterface());
                    } else {
                        // set
                        dumpEnumValue(v.name(), Set.class, ValueType.getComponentType(v),
                                v.getXydraInterface());
                    }
                } else {
                    assert v.isSortedCollection();
                    // list
                    dumpEnumValue(v.name(), List.class, ValueType.getComponentType(v),
                            v.getXydraInterface());
                }
            }
            System.out.println();
        }
    }
    
    public static void dumpEnumValue(String enumName, Class<?> javaType,
            @CanBeNull Class<?> javaComponentType, Class<?> xydraType) {
        
        @SuppressWarnings("unused")
        String javaTypeVariable = javaType.getSimpleName();
        if(OOTypeBridge.isCollectionType(javaType)) {
            assert javaComponentType != null;
            // System.out.println("@SuppressWarnings(\"rawtypes\")");
            // javaTypeVariable += "<" + javaComponentType.getSimpleName() +
            // ">";
        }
        
        System.out.println("public final static OOTypeMapping"
                
                + " "
                + enumName
                + " = new OOTypeMapping"
                +
                // "<>" +
                "( "
                + javaType.getSimpleName()
                + ".class"
                + ", "
                + (javaComponentType == null ? "null" : javaComponentType.getSimpleName()
                        + ".class") + ", " + xydraType.getSimpleName() + ".class" + ", null);");
    }
    
}
