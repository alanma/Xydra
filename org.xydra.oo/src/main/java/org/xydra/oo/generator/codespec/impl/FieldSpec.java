package org.xydra.oo.generator.codespec.impl;

import java.lang.reflect.Type;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.runtime.java.JavaReflectionUtils;
import org.xydra.oo.runtime.java.JavaTypeSpecUtils;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.TypeSpec;


/**
 * specifies a field
 * 
 * @author xamde
 */
public class FieldSpec extends AbstractMember implements IMember {
    
    public IType t;
    
    public FieldSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
        super(name, generatedFrom);
        this.t = JavaTypeSpecUtils.createTypeSpec(type, componentType, generatedFrom);
    }
    
    public FieldSpec(String name, Class<?> type, String componentPackageName,
            String componentTypeName, String generatedFrom) {
        super(name, generatedFrom);
        this.t = JavaReflectionUtils.createTypeSpec(type, componentPackageName, componentTypeName,
                generatedFrom);
    }
    
    public FieldSpec(String name, String typePackageName, String typeName, String generatedFrom) {
        super(name, generatedFrom);
        this.t = new TypeSpec(typePackageName, typeName, generatedFrom);
    }
    
    public FieldSpec(String name, Type t, String generatedFrom) {
        super(name, generatedFrom);
        this.t = JavaTypeSpecUtils.createTypeSpec(JavaReflectionUtils.getRawType(t),
                JavaReflectionUtils.getComponentType(t), generatedFrom);
    }
    
    FieldSpec(String name, IType typeSpec, String generatedFrom) {
        super(name, generatedFrom);
        this.t = typeSpec;
    }
    
    @Override
    public int compareTo(IMember o) {
        if(o instanceof FieldSpec) {
            return this.id().compareTo(((FieldSpec)o).id());
        } else {
            return getName().compareTo(o.getName());
        }
        
    }
    
    public void dump() {
        System.out.println(this.toString());
    }
    
    public boolean equals(Object other) {
        return other instanceof FieldSpec && ((FieldSpec)other).id().equals(this.id());
    }
    
    @Override
    public Set<String> getRequiredImports() {
        Set<String> set = this.t.getRequiredImports();
        set.addAll(super.getRequiredImports());
        return set;
    }
    
    public int hashCode() {
        return id().hashCode();
    }
    
    public String id() {
        return this.getName() + this.t.id();
    }
    
    public String toString() {
        return "FIELD\n" + "  " + this.getName() + " " + this.t.toString();
    }
    
    public String getTypeString() {
        return this.t.getTypeString();
    }
    
    public IType getType() {
        return this.t;
    }
    
}
