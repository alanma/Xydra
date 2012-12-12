package org.xydra.mvnxy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Copy compiled GWT javascript to the webapp folder so that it gets deployed
 * 
 * @goal copygwt
 */
public class CopyGwt extends AbstractMojo {
    
    private static final Logger log = LoggerFactory.getLogger(CopyGwt.class);
    
    /**
     * artifactId
     * 
     * @parameter expression="${project.artifactId}"
     */
    private String artifactId;
    
    /**
     * versionId
     * 
     * @parameter expression="${project.version}"
     */
    private String versionId;
    
    private String warPath;
    
    /**
     * @parameter
     */
    private String[] gwtModuleNames;
    
    public void execute() throws MojoExecutionException {
        try {
            this.warPath = "./target/" + this.artifactId + "-" + this.versionId;
            copyGwt();
        } catch(Throwable t) {
            throw new MojoExecutionException("There was an error", t);
        }
        
    }
    
    /** Copy GWT stuff */
    public void copyGwt() {
        for(String moduleName : this.gwtModuleNames) {
            copyCompiledGwtModule(this.warPath, moduleName);
        }
    }
    
    /**
     * Copy from target dir to src, so that local Jetty works
     * 
     * @param warPath e.g. './target/myname-0.1.2-SNAPSHOT'
     * @param moduleName e.g. 'mygwtmodule'
     */
    public static void copyCompiledGwtModule(String warPath, String moduleName) {
        File targetGwt = new File(warPath + "/" + moduleName);
        if(!targetGwt.exists()) {
            log.error("GWT data not found in " + targetGwt.getAbsolutePath()
                    + ".Some AJAX will not work. \n Please run 'mvn gwt:compile' first");
            System.exit(1);
        } else {
            assert targetGwt.isDirectory();
            File sourceWebAppGwt = new File("./src/main/webapp/" + moduleName);
            assert sourceWebAppGwt.getParentFile().exists();
            log.info("Copying GWT files temporarily to " + sourceWebAppGwt.getAbsolutePath());
            FileUtils.deleteQuietly(sourceWebAppGwt);
            sourceWebAppGwt.mkdirs();
            try {
                FileUtils.copyDirectory(targetGwt, sourceWebAppGwt);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static void copyFile(String srcFile, String targetFile) {
        File src = new File(srcFile);
        File target = new File(targetFile);
        try {
            FileUtils.copyFile(src, target);
        } catch(IOException e) {
            log.error("Could not copy from '" + srcFile + "' to '" + targetFile + "'", e);
        }
    }
    
}
