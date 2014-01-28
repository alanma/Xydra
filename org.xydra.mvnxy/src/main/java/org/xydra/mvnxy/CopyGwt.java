package org.xydra.mvnxy;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xydra.gwttools.GwtBuildHelper;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * A Maven Mojo.
 * 
 * Copy compiled GWT javascript to the webapp folder so that it gets deployed.
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
        if(this.gwtModuleNames == null) {
            log.warn("No modules configured in <gwtModuleNames><gwtModuleName>...</gwtModuleName>gwtModuleNames");
            showHelp();
            return;
        }
        for(String moduleName : this.gwtModuleNames) {
            GwtBuildHelper.copyCompiledGwtModule(this.warPath, moduleName);
        }
    }
    
    private static void showHelp() {
        System.out.println("...\n" + // .
                "<build><plugins>\n" + // .
                "\n" + // .
                "<plugin>\n" + // .
                "  <groupId>org.xydra</groupId>\n" + // .
                "  <artifactId>xydra-maven-plugin</artifactId>\n" + // .
                "  <version>1.0-SNAPSHOT</version><!-- or newer -->\n" + // .
                "  <configuration>\n" + // .
                "    <gwtModuleNames>\n" + // .
                "      <gwtModuleName>gwt</gwtModuleName>\n" + // .
                "      <gwtModuleName>foo</gwtModuleName>\n" + // .
                "    </gwtModuleNames>\n" + // .
                "  </configuration>\n" + // .
                "</plugin>\n"); // .
    }
    
}
