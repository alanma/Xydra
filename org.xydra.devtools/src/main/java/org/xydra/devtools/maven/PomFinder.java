package org.xydra.devtools.maven;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;


public class PomFinder {
    
    public static List<File> findPomFiles(File dir) {
        List<File> list = new LinkedList<File>();
        File[] files = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().equals("pom.xml");
            }
        });
        for(File f : files) {
            if(f.isDirectory()) {
                list.addAll(findPomFiles(f));
            } else {
                list.add(f);
            }
        }
        return list;
    }
    
    public static void main(String[] args) {
        showArtifactDependencies(new File("target/poms.dot"), new File("/Users/xamde/_data_/_p_"),
                new File("/Users/xamde/.m2/repository"));
    }
    
    private static void showArtifactDependencies(File targetDotFile, File ... sourceDirs) {
        List<File> list = new LinkedList<File>();
        for(File sourceDir : sourceDirs) {
            list.addAll(findPomFiles(sourceDir));
        }
        
        for(File f : list) {
            System.out.println(f.getAbsolutePath());
        }
        
    }
    
}
