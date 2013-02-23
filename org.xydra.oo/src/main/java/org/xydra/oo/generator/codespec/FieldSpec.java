package org.xydra.oo.generator.codespec;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import org.xydra.oo.runtime.java.TypeTool;


public class FieldSpec extends AbstractMember implements IMember {
    
    public TypeSpec t;
    
    public FieldSpec(Field field, String generatedFrom) {
        super(field.getName(), generatedFrom);
        this.t = new TypeSpec(field.getType(), generatedFrom);
        this.t.componentType = TypeTool.getComponentType(field);
    }
    
    public FieldSpec(String name, Type t, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(TypeTool.getRawType(t), TypeTool.getComponentType(t), generatedFrom);
    }
    
    public FieldSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(type, componentType, generatedFrom);
    }
    
    public FieldSpec(String name, Class<?> type, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(type, generatedFrom);
    }
    
    public FieldSpec(String name, Class<?> type, String componentTypeName, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(type, componentTypeName, generatedFrom);
    }
    
    public FieldSpec(String name, String typeName, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(typeName, generatedFrom);
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
        return "FIELD\n" + "  " + this.getName() + " " + this.t.toString();
    }
    
    public void dump() {
        System.out.println(this.toString());
    }
    
    @Override
    public Set<String> getRequiredImports() {
        Set<String> set = this.t.getRequiredImports();
        set.addAll(super.getRequiredImports());
        return set;
    }
    
    public String getTypeName() {
        return this.t.getTypeName();
    }
    
    public boolean isCollectionType() {
        return this.t.isCollectionType();
    }
}
