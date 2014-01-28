package org.xydra.devtools.serviceloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;


/**
 * Help managing configuration files for {@link ServiceLoader}
 * 
 * @author xamde
 */
public class ServiceLoaderTool {
    
    public static void clearServiceLoaderDirectory(File mavenProjectRootDir) {
        File serviceDir = ensureServiceDir(mavenProjectRootDir);
        for(File f : serviceDir.listFiles()) {
            f.delete();
        }
    }
    
    /**
     * Write a configuration file for {@link ServiceLoader}
     * 
     * @param mavenProjectRootDir files are written in
     *            /src/main/resources/META-INF/services/...
     * @param clazz the implementation
     * @param interfaze the interface
     * @param comment @CanBeNullmay not contain new lines
     * @throws IOException
     */
    public static void writeServiceLoaderFile(File mavenProjectRootDir, Class<?> clazz,
            Class<?> interfaze, String comment) throws IOException {
        File serviceDir = ensureServiceDir(mavenProjectRootDir);
        
        File f = new File(serviceDir, interfaze.getCanonicalName());
        
        List<String> lines = new ArrayList<>();
        if(comment != null) {
            lines.add("# " + comment);
        }
        lines.add(clazz.getCanonicalName() + " # written by "
                + ServiceLoaderTool.class.getCanonicalName() + " on " + new Date());
        
        FileUtils.writeLines(f, lines);
        
        System.out.println("Written to " + f.getAbsolutePath());
    }
    
    private static File ensureServiceDir(File mavenProjectRootDir) {
        if(!mavenProjectRootDir.exists() || !mavenProjectRootDir.isDirectory()) {
            throw new IllegalArgumentException("Dir '" + mavenProjectRootDir.getAbsolutePath()
                    + "' is not an existing directory");
        }
        File serviceDir = new File(mavenProjectRootDir, "/src/main/resources/META-INF/services");
        serviceDir.mkdirs();
        return serviceDir;
    }
    
}
