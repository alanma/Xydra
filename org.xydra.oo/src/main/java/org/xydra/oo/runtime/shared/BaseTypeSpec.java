package org.xydra.oo.runtime.shared;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * A single type with a package and a simple name. Is either a collection type
 * OR a component type OR a generic (typeless) array type.
 * 
 * To represent generic collection types or normal typed arrays, use a
 * {@link TypeSpec}.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class BaseTypeSpec implements Comparable<BaseTypeSpec> {
    
    public static final BaseTypeSpec ARRAY = new BaseTypeSpec("org.xydra.oo.runtime.shared",
            "Array");
    
    @CanBeNull
    private String packageName;
    
    @NeverNull
    private String simpleName;
    
    public BaseTypeSpec(@CanBeNull String packageName, @NeverNull String simpleName) {
        init(packageName, simpleName);
    }
    
    /**
     * Create a clone
     * 
     * @param baseType
     */
    public BaseTypeSpec(BaseTypeSpec baseType) {
        this(baseType.getPackageName(), baseType.getSimpleName());
    }
    
    /**
     * @param packageName can be null for primitive Java types
     * @param simpleName can contain dots to represent inner classes as
     *            outercclass.innerclass
     */
    private void init(@CanBeNull String packageName, @NeverNull String simpleName) {
        assert simpleName != null;
        assert simpleName.length() > 0;
        assert !simpleName.equals("void");
        if(simpleName.endsWith("[]"))
            throw new IllegalArgumentException("type cannot be an array type");
        this.packageName = packageName;
        this.simpleName = simpleName;
        assert packageName != null || simpleName.equals("byte") || simpleName.equals("boolean")
                || simpleName.equals("char") || simpleName.equals("double")
                || simpleName.equals("float") || simpleName.equals("int")
                || simpleName.equals("long") || simpleName.equals("short");
    }
    
    @Override
    public int compareTo(BaseTypeSpec o) {
        int i = this.packageName.compareTo(o.getPackageName());
        if(i != 0)
            return i;
        
        return this.simpleName.compareTo(o.getSimpleName());
    }
    
    public boolean equals(Object o) {
        if(o instanceof BaseTypeSpec) {
            BaseTypeSpec obts = (BaseTypeSpec)o;
            return obts.packageName.equals(this.packageName)
                    && obts.simpleName.equals(this.simpleName);
        }
        return false;
    }
    
    public String getCanonicalName() {
        return (this.packageName == null ? "" : this.packageName + ".") + this.simpleName;
    }
    
    public String getPackageName() {
        return this.packageName;
    }
    
    public String getSimpleName() {
        return this.simpleName;
    }
    
    public int hashCode() {
        return this.packageName.hashCode() + this.simpleName.hashCode();
    }
    
    public String toString() {
        return this.getCanonicalName();
    }
    
    public boolean isArray() {
        return this.equals(ARRAY);
    }
    
    public String getRequiredImport() {
        if(getSimpleName().contains(".")) {
            // require enclosing class
            String[] parts = getSimpleName().split("[.]");
            assert parts.length == 2 : "cannot handle multiple level deep nested inner classes";
            return getPackageName() + "." + parts[0];
        } else
            return getCanonicalName();
    }
    
    /**
     * @param packageName @CanBeNull
     * @param simpleName @CanBeNull
     * @return null if simpleName is null
     */
    public static BaseTypeSpec create(java.lang.String packageName, java.lang.String simpleName) {
        if(simpleName == null)
            return null;
        return new BaseTypeSpec(packageName, simpleName);
    }
    
}
