package org.xydra.conf.impl;

import java.lang.reflect.Field;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;


/**
 * Does runtime checks, only available in Java, not in GWT.
 * 
 * @author xamde
 * 
 */
@Setting("foo")
@RunsInGWT(false)
public class ConfigTool {
    
    @Setting("a field")
    String foo = "bar";
    
    public static void main(String[] args) {
        // find all @Settings
        
        try {
            System.out.println("there is " + Reflections.class.getCanonicalName());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        
        Reflections reflections = new Reflections(
        
        ClasspathHelper.forPackage("com.calpano"), ClasspathHelper.forPackage("org.xydra"),
        
        new TypesScanner(), new TypeAnnotationsScanner(), new FieldAnnotationsScanner(),
                new TypeElementsScanner());
        
        System.out.println(reflections.getFieldsAnnotatedWith(Setting.class).size());
        System.out.println(reflections.getMethodsAnnotatedWith(Setting.class).size());
        System.out.println(reflections.getTypesAnnotatedWith(Setting.class).size());
        
        for(Class<?> c : reflections.getTypesAnnotatedWith(Setting.class)) {
            System.out.println("Setting in class " + c.getCanonicalName());
            Setting setting = c.getAnnotation(Setting.class);
            assert setting != null;
            System.out.println(">> " + setting.value());
        }
        for(Field f : reflections.getFieldsAnnotatedWith(Setting.class)) {
            System.out.println("Setting in field '" + f.getName() + "' in "
                    + f.getDeclaringClass().getCanonicalName());
            Setting setting = f.getAnnotation(Setting.class);
            
            System.out.println(">> " + (setting == null ? "--" : setting.value()));
        }
        
    }
}
