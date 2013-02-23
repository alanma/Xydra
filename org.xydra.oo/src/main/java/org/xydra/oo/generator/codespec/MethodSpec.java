package org.xydra.oo.generator.codespec;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.xydra.oo.runtime.java.TypeTool;


public class MethodSpec extends AbstractConstructorOrMethodSpec implements IMember {
    
    public TypeSpec returnType;
    
    public MethodSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
        super(name, generatedFrom);
        this.returnType = new TypeSpec(type, componentType, generatedFrom);
    }
    
    public MethodSpec(String name, Class<?> type, String generatedFrom) {
        super(name, generatedFrom);
        this.returnType = new TypeSpec(type, generatedFrom);
    }
    
    public MethodSpec(String name, Class<?> type, String componentTypeName, String generatedFrom) {
        super(name, generatedFrom);
        this.returnType = new TypeSpec(type, componentTypeName, generatedFrom);
    }
    
    public MethodSpec(String name, String typeName, String generatedFrom) {
        super(name, generatedFrom);
        this.returnType = new TypeSpec(typeName, generatedFrom);
    }
    
    /**
     * @param method use to take name, returnType and returnType.componentType
     * @param generatedFrom
     */
    public MethodSpec(Method method, String generatedFrom) {
        super(method.getName(), generatedFrom);
        this.returnType = new TypeSpec(method.getReturnType(), generatedFrom);
        this.returnType.componentType = TypeTool.getComponentType(method);
    }
    
    public boolean isVoid() {
        return this.returnType.getTypeName().equals("void");
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
    
    public String getReturnTypeName() {
        return this.returnType.getTypeName();
    }
    
    @Override
    public Set<String> getRequiredImports() {
        Set<String> req = new HashSet<>();
        req.addAll(super.getRequiredImports());
        req.addAll(this.returnType.getRequiredImports());
        return req;
    }
    
}
