package org.xydra.oo.generator.codespec;

public class NameUtils {
    
    public static String toJavaName(String name) {
        return firstLetterUppercased(name);
    }
    
    public static String firstLetterUppercased(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    
    public static String firstLetterLowercased(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }
    
    public static String toXFieldName(String name) {
        return firstLetterLowercased(name);
    }
    
    public static String toClassName(Class<?> clazz) {
        String s = clazz.getSimpleName();
        return "I" + s;
    }
    
    public static String withoutPackages(String typeName) {
        if(typeName.equals("void"))
            return "void";
        
        assert typeName.contains(".") : "typeName must be a FQ name";
        
        String[] s = typeName.split("[.]");
        return s[s.length - 1];
    }
    
}
