package org.xydra.core;

import java.io.File;
import java.io.IOException;

import org.xydra.devtools.PackageChaos;
import org.xydra.devtools.architecture.Architecture;
import org.xydra.devtools.java.Project;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;


public class XydraCompleteArchitecture {
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        try {
            Log4jUtils.listConfigFromClasspath();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        String scope = "org.xydra";
        
        Architecture a = new Architecture(scope);
        
        // // step 1: check for violation of GWT layers
        // Architecture a = new Architecture(scope);
        // Layer shared = a.defineLayer(scope + ".shared");
        // Layer client = a.defineLayer(scope + ".client");
        // Layer server = a.defineLayer(scope + ".server");
        // client.mayAccess(shared);
        // server.mayAccess(shared);
        
        File dot = new File("./target/res.dot");
        PackageChaos pc = new PackageChaos(a, dot);
        pc.setShowCauses(true);
        
        Project p = pc.getProject();
        p.scanDir("/Users/xamde/_data_/_p_/2013/org.xydra.oo");
        
        pc.renderDotGraph();
    }
    
}
