package org.xydra.oo.generator.codespec;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.oo.runtime.java.TypeTool;


/**
 * Either {@link #type} or {@link #typeName} is not null.
 * 
 * @author xamde
 */
public class TypeSpec implements Comparable<TypeSpec> {
    
    public String generatedFrom;
    
    public TypeSpec(Class<?> type, Class<?> componentType, String generatedFrom) {
        this.type = type;
        this.componentType = componentType;
        this.generatedFrom = generatedFrom;
    }
    
    /**
     * @param type can also be an array type
     * @param generatedFrom
     */
    public TypeSpec(Class<?> type, String generatedFrom) {
        this.type = type;
        if(type.isArray()) {
            this.componentType = type.getComponentType();
        }
        this.generatedFrom = generatedFrom;
    }
    
    public TypeSpec(Class<?> type, String componentTypeName, String generatedFrom) {
        this.type = type;
        this.componentTypeName = componentTypeName;
        this.generatedFrom = generatedFrom;
    }
    
    public TypeSpec(String typeName, String generatedFrom) {
        this.typeName = typeName;
    }
    
    public TypeSpec(Type t, String generatedFrom) {
        this(TypeTool.getRawType(t), TypeTool.getComponentType(t), "XXX");
        this.generatedFrom = generatedFrom;
    }
    
    public Set<String> getRequiredImports() {
        HashSet<String> req = new HashSet<>();
        if(this.type != null) {
            if(this.type.isArray()) {
                // require only component type
            } else {
                req.add(this.type.getCanonicalName());
            }
        }
        // if(this.typeName != null) {
        // req.add(this.typeName);
        // }
        if(this.componentType != null) {
            req.add(this.componentType.getCanonicalName());
        }
        // if(this.componentTypeName != null) {
        // req.add(this.componentTypeName);
        // }
        assert req.size() >= 0;
        assert req.size() <= 2;
        
        Iterator<String> it = req.iterator();
        while(it.hasNext()) {
            String r = it.next();
            if(r.startsWith("java.lang")
            
            || r.equals("boolean") || r.equals("int") || r.equals("long") || r.equals("double")
                    || r.equals("byte")
            
            ) {
                it.remove();
            }
        }
        
        return req;
    }
    
    public String getTypeName() {
        if(this.type != null) {
            if(isCollectionType()) {
                // generic collection/array
                String compType = getComponentTypeName();
                if(this.type.isArray()) {
                    return compType + "[]";
                } else {
                    return this.type.getSimpleName() + "<" + compType + ">";
                }
                
            } else {
                // single
                return this.type.getSimpleName();
            }
        } else {
            assert this.typeName != null : "type or typeName must be defined " + id();
            // single
            return this.typeName;
        }
    }
    
    private String getComponentTypeName() {
        if(this.componentType != null) {
            return this.componentType.getSimpleName();
        } else {
            assert this.componentTypeName != null;
            return this.componentTypeName;
        }
    }
    
    public boolean isCollectionType() {
        if(this.type != null) {
            return this.type.isArray() || this.type.equals(Set.class)
                    || this.type.equals(List.class) || this.type.equals(SortedSet.class);
        }
        return false;
    }
    
    @CanBeNull
    String typeName = null;
    @CanBeNull
    public Class<?> type = null;
    @CanBeNull
    public Class<?> componentType = null;
    @CanBeNull
    String componentTypeName = null;
    
    public String comment;
    
    public String id() {
        return this.typeName + this.type + this.componentTypeName + this.componentType;
    }
    
    public int hashCode() {
        return id().hashCode();
    }
    
    public boolean equals(Object other) {
        return other instanceof TypeSpec && ((TypeSpec)other).id().equals(this.id());
    }
    
    @Override
    public int compareTo(TypeSpec o) {
        return this.id().compareTo(o.id());
    }
    
    public void dump() {
        System.out.println("TYPESPEC " + toString());
    }
    
    public String toString() {
        return
        
        (this.comment != null ? "// " + this.comment + "\n" : "")
        
        + "  // generated from " + this.generatedFrom + " \n"
        
        + "  type:" + this.type + " tname:" + this.typeName + " comptype:" + this.componentType
                + " ctname:" + this.componentTypeName
        
        ;
    }
    
}
