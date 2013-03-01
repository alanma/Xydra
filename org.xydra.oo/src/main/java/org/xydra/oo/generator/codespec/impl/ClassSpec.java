package org.xydra.oo.generator.codespec.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.runtime.shared.TypeSpec;


public class ClassSpec extends NamedElement {
    
    /** Make sure to add also as required imports */
    public List<String> implementedInterfaces = new ArrayList<>();
    
    public String kind;
    
    public List<IMember> members = new ArrayList<>();
    
    private PackageSpec packageSpec;
    
    private Set<String> req = new HashSet<>();
    
    public ClassSpec superClass;
    
    private String comment;
    
    /**
     * @param packageSpec
     * @param kind "interface" or "class" or "abstract class"
     * @param name
     */
    ClassSpec(PackageSpec packageSpec, String kind, String name) {
        super(name);
        this.packageSpec = packageSpec;
        packageSpec.classes.add(this);
        this.kind = kind;
    }
    
    public ConstructorSpec addConstructor(String generatedFrom) {
        ConstructorSpec constructorSpec = new ConstructorSpec(this, generatedFrom);
        this.members.add(constructorSpec);
        return constructorSpec;
    }
    
    public MethodSpec addMethod(Method m, String generatedFrom) {
        MethodSpec methodSpec = new MethodSpec(m, generatedFrom);
        this.members.add(methodSpec);
        return methodSpec;
    }
    
    public MethodSpec addMethod(String name, String typePackageName, String typeName,
            String generatedFrom) {
        MethodSpec methodSpec = new MethodSpec(name, typePackageName, typeName, generatedFrom);
        this.members.add(methodSpec);
        return methodSpec;
    }
    
    public MethodSpec addMethod(String name, TypeSpec t, String generatedFrom) {
        MethodSpec methodSpec = new MethodSpec(name, t, generatedFrom);
        this.members.add(methodSpec);
        return methodSpec;
    }
    
    public void addRequiredImports(Class<?> c) {
        addRequiredImports(c.getCanonicalName());
    }
    
    public void addRequiredImports(String canonicalName) {
        assert !canonicalName.equals("void");
        this.req.add(canonicalName);
    }
    
    public MethodSpec addVoidMethod(String name, String generatedFrom) {
        MethodSpec methodSpec = MethodSpec.createVoid(name, generatedFrom);
        this.members.add(methodSpec);
        return methodSpec;
    }
    
    public void dump() {
        System.out.println("Class " + getName() + " extends " + this.superClass.getCanonicalName());
        for(IMember t : this.members) {
            t.dump();
        }
    }
    
    public boolean equals(Object other) {
        return other instanceof ClassSpec && ((ClassSpec)other).id().equals(this.id());
    }
    
    public String getCanonicalName() {
        return this.packageSpec.getFQPackageName() + "." + getName();
    }
    
    public String getPackageName() {
        return this.packageSpec.getFQPackageName();
    }
    
    public Set<String> getRequiredImports() {
        Set<String> imports = new HashSet<>();
        imports.addAll(this.req);
        if(this.superClass != null) {
            imports.add(this.superClass.getCanonicalName());
        }
        for(IMember t : this.members) {
            for(String req : t.getRequiredImports()) {
                imports.add(req);
            }
        }
        return imports;
    }
    
    public int hashCode() {
        return id().hashCode();
    }
    
    public String id() {
        return this.getName();
    }
    
    public boolean isBuiltIn() {
        return this.packageSpec.isBuiltIn();
    }
    
    public void setComment(String s) {
        this.comment = s;
    }
    
    public String getComment() {
        return this.comment;
    }
    
}
