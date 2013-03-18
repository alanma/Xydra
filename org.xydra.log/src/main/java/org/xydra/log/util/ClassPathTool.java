package org.xydra.log.util;

import java.util.Arrays;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(false)
public class ClassPathTool {
    
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
    
    public static void main(String[] args) {
        dumpCurrentClasspath();
    }
    
}
