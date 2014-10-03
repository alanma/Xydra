package org.xydra.oo.runtime.shared;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * Represents a Java type as a pair (baseType, componentType?). That is easier
 * to do type inference with.
 * 
 * Has a base type and optionally a component type - iff base type is a
 * collection type (including array). Both components are represented as
 * {@link BaseTypeSpec} instances.
 * 
 * Has a comment (for the user) and debug info where it was generated from.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class TypeSpec implements Comparable<TypeSpec>, IType {
    
    /** simple type or collection */
    @NeverNull
    private IBaseType baseType;
    
    private String comment;
    
    /** @CanBeNull */
    private IBaseType componentType;
    
    private String generatedFrom;
    
    /**
     * A type with no component type
     * 
     * @param typePackageName
     * @param typeName
     * @param generatedFrom
     */
    public TypeSpec(String typePackageName, String typeName, String generatedFrom) {
        this.baseType = new BaseTypeSpec(typePackageName, typeName);
        this.generatedFrom = generatedFrom;
    }
    
    /**
     * @param baseType
     * @param componentType
     * @param generatedFrom
     */
    public TypeSpec(@NeverNull IBaseType baseType, IBaseType componentType,
            String generatedFrom) {
        assert baseType != null;
        this.baseType = baseType;
        this.componentType = componentType;
        this.generatedFrom = generatedFrom;
    }
    
    /**
     * @param baseTypePackage
     * @param baseTypeSimpleName
     * @param componentTypePackage
     * @param componentTypeSimpleName
     * @param generatedFrom
     */
    public TypeSpec(String baseTypePackage, String baseTypeSimpleName, String componentTypePackage,
            String componentTypeSimpleName, String generatedFrom) {
        this.baseType = new BaseTypeSpec(baseTypePackage, baseTypeSimpleName);
        this.componentType = BaseTypeSpec.create(componentTypePackage, componentTypeSimpleName);
        this.generatedFrom = generatedFrom;
    }
    
    /**
     * Create a clone
     * 
     * @param t
     */
    public TypeSpec(IType t) {
        this(new BaseTypeSpec(t.getBaseType()), t.getComponentType() == null ? null
                : new BaseTypeSpec(t.getComponentType()), t.getGeneratedFrom());
        setComment(t.getComment());
    }
    
    @Override
    public int compareTo(TypeSpec o) {
        return this.id().compareTo(o.id());
    }
    
    public void dump() {
        System.out.println("TYPESPEC " + toString());
    }
    
    @Override
	public boolean equals(Object other) {
        return other instanceof TypeSpec && ((TypeSpec)other).id().equals(this.id());
    }
    
    @Override
	public IBaseType getBaseType() {
        return this.baseType;
    }
    
    @Override
	public String getComment() {
        return this.comment;
    }
    
    @Override
	public IBaseType getComponentType() {
        return this.componentType;
    }
    
    @Override
    public Set<String> getRequiredImports() {
        HashSet<String> req = new HashSet<String>();
        
        if(this.baseType.isArray()) {
            // require component type
            assert this.componentType != null;
            req.add(this.componentType.getRequiredImport());
        } else {
            // allow component type
            req.add(this.baseType.getRequiredImport());
            if(this.componentType != null) {
                req.add(this.componentType.getRequiredImport());
            }
        }
        
        assert req.size() >= 0;
        assert req.size() <= 2;
        
        // remove primitive types and built-in imports
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
        
        assert !req.contains("void");
        return req;
    }
    
    /**
     * @return a valid type expression in java
     */
    @Override
    public String getTypeString() {
        if(this.componentType == null) {
            // simple type
            return this.baseType.getSimpleName();
        } else {
        	assert this.componentType != null;
            // assume a generic collection type
            if(this.baseType.isArray()) {
                return this.componentType.getSimpleName() + "[]";
            } else {
                return this.baseType.getSimpleName() + "<" + this.componentType.getSimpleName()
                        + ">";
            }
        }
    }
    
    @Override
	public int hashCode() {
        return id().hashCode();
    }
    
    @Override
    public String id() {
        assert this.baseType != null;
        return this.baseType.getCanonicalName()
                + (this.componentType == null ? "" : ":" + this.componentType.getCanonicalName());
    }
    
    @Override
	public void setComment(String string) {
        this.comment = string;
    }
    
    @Override
	public String toString() {
        return
        
        (this.comment != null ? "// " + this.comment + "\n" : "")
        
        + "  // generated from " + this.generatedFrom + " \n"
        
        + "type=" + this.baseType + " compType=" + this.componentType;
    }
    
    @Override
	public boolean isArray() {
        return this.baseType.isArray();
    }
    
    @Override
    public String getGeneratedFrom() {
        return this.generatedFrom;
    }
    
}
