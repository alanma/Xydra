package org.xydra.oo.generator.codespec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.oo.runtime.java.TypeTool;


public class MethodSpec implements IMember {
    
    public List<FieldSpec> params = new ArrayList<>();
    
    String name;
    
    public String comment;
    
    public TypeSpec returnType;
    
    private String generatedFrom;
    
    public List<String> sourceLines = new ArrayList<>();
    
    public MethodSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
        this.name = name;
        this.returnType = new TypeSpec(type, componentType, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public MethodSpec(String name, Class<?> type, String generatedFrom) {
        this.name = name;
        this.returnType = new TypeSpec(type, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public MethodSpec(String name, Class<?> type, String componentTypeName, String generatedFrom) {
        this.name = name;
        this.returnType = new TypeSpec(type, componentTypeName, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    public MethodSpec(String name, String typeName, String generatedFrom) {
        this.name = name;
        this.returnType = new TypeSpec(typeName, generatedFrom);
        this.generatedFrom = generatedFrom;
    }
    
    /**
     * @param method use to take name, returnType and returnType.componentType
     * @param generatedFrom
     */
    public MethodSpec(Method method, String generatedFrom) {
        this.name = method.getName();
        this.returnType = new TypeSpec(method.getReturnType(), generatedFrom);
        this.returnType.componentType = TypeTool.getComponentType(method);
        this.generatedFrom = generatedFrom;
    }
    
    public boolean isVoid() {
        return this.returnType.getTypeName().equals("void");
    }
    
    public String id() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        for(FieldSpec p : this.params) {
            sb.append(p.id());
        }
        return sb.toString();
    }
    
    public boolean equals(Object other) {
        return other instanceof FieldSpec && ((FieldSpec)other).id().equals(this.id());
    }
    
    public int hashCode() {
        return this.id().hashCode();
    }
    
    @Override
    public int compareTo(IMember o) {
        if(o instanceof MethodSpec) {
            return this.id().compareTo(((MethodSpec)o).id());
        } else {
            return getName().compareTo(o.getName());
        }
    }
    
    public String toString() {
        String s = "";
        s += "METHOD\n";
        s += "  name:" + this.getName() + " returnType:[" + this.returnType.toString() + "]" + "\n";
        s += "  comment:" + this.comment + "\n";
        for(FieldSpec p : this.params) {
            s += "  PARAM " + p.toString() + "\n";
        }
        for(String l : this.sourceLines) {
            s += "  CODE " + l + "\n";
        }
        return s;
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
    
    public String getReturnTypeName() {
        return this.returnType.getTypeName();
    }
    
    @Override
    public Set<String> getRequiredImports() {
        Set<String> req = new HashSet<>();
        req.addAll(this.returnType.getRequiredImports());
        for(FieldSpec p : this.params) {
            req.addAll(p.getRequiredImports());
        }
        return req;
    }
    
    @Override
    public String getGeneratedFrom() {
        return this.generatedFrom;
    }
}
