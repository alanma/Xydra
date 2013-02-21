package org.xydra.oo.generator.codespec;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.oo.runtime.java.TypeTool;


public class FieldSpec implements IMember {
    
    @NeverNull
    private String name;
    
    public TypeSpec t;
    
    public String comment;
    
    private String generatedFrom;
    
    public FieldSpec(Field field, String generatedFrom) {
        this.t = new TypeSpec(field.getType(), generatedFrom);
        this.t.componentType = TypeTool.getComponentType(field);
        this.name = field.getName();
        this.generatedFrom = generatedFrom;
    }
    
    public FieldSpec(String name, Type t, String generatedFrom) {
        this.name = name;
        this.t = new TypeSpec(TypeTool.getRawType(t), TypeTool.getComponentType(t), generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public FieldSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
        this.name = name;
        this.t = new TypeSpec(type, componentType, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public FieldSpec(String name, Class<?> type, String generatedFrom) {
        this.name = name;
        this.t = new TypeSpec(type, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public FieldSpec(String name, Class<?> type, String componentTypeName, String generatedFrom) {
        this.name = name;
        this.t = new TypeSpec(type, componentTypeName, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public FieldSpec(String name, String typeName, String generatedFrom) {
        this.name = name;
        this.t = new TypeSpec(typeName, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public boolean equals(Object other) {
        return other instanceof FieldSpec && ((FieldSpec)other).id().equals(this.id());
    }
    
    public String id() {
        return this.getName() + this.t.id();
    }
    
    public int hashCode() {
        return id().hashCode();
    }
    
    @Override
    public int compareTo(IMember o) {
        if(o instanceof FieldSpec) {
            return this.id().compareTo(((FieldSpec)o).id());
        } else {
            return getName().compareTo(o.getName());
        }
        
    }
    
    public String toString() {
        return "FIELD\n" + "  " + this.name + " " + this.t.toString();
    }
    
    public void dump() {
        System.out.println(this.toString());
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getComment() {
        return this.comment;
    }
    
    @Override
    public Set<String> getRequiredImports() {
        return this.t.getRequiredImports();
    }
    
    public String getTypeName() {
        return this.t.getTypeName();
    }
    
    @Override
    public String getGeneratedFrom() {
        return this.generatedFrom;
    }
    
    public boolean isCollectionType() {
        return this.t.isCollectionType();
    }
}
