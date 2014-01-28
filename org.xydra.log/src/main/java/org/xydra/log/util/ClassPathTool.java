package org.xydra.log.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(false)
public class ClassPathTool {
    
    /**
     * Prints class path (obtained via system property) to System.out
     */
    public static void dumpCurrentClasspath() {
        String cp = System.getProperty("java.class.path");
        String[] parts = cp.split("[:]");
        System.out.println("=== Real Order:");
        for(String p : parts) {
            System.out.println("Classpath: " + p);
        }
        System.out.println("=== Alphabetically:");
        Arrays.sort(parts);
        for(String p : parts) {
            System.out.println("Classpath: " + p);
        }
    }
    
    /**
     * Hacky way to list all classes that a classloader (that has a member
     * variable 'classes') has in fact loaded
     * 
     * @param byClassLoader
     */
    public static void dumpLoadedClasses(ClassLoader byClassLoader) {
        Class<?> clKlass = byClassLoader.getClass();
        System.out.println("Classloader: " + clKlass.getCanonicalName());
        while(clKlass != java.lang.ClassLoader.class) {
            clKlass = clKlass.getSuperclass();
        }
        try {
            java.lang.reflect.Field fldClasses = clKlass.getDeclaredField("classes");
            fldClasses.setAccessible(true);
            Vector<?> classes = (Vector<?>)fldClasses.get(byClassLoader);
            for(Iterator<?> iter = classes.iterator(); iter.hasNext();) {
                System.out.println("   Loaded " + iter.next());
            }
        } catch(SecurityException e) {
            e.printStackTrace();
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        } catch(NoSuchFieldException e) {
            e.printStackTrace();
        } catch(IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        dumpCurrentClasspath();
    }
    
}
