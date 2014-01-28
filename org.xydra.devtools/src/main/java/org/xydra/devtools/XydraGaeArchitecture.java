package org.xydra.devtools;

import java.io.File;
import java.io.IOException;

import org.xydra.devtools.javapackages.PackageChaos;
import org.xydra.devtools.javapackages.architecture.Architecture;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;


public class XydraGaeArchitecture {
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "XydraGaeArchitecture");
        try {
            Log4jUtils.listConfigFromClasspath();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        Architecture xydraArchitecture = new Architecture("");
        xydraArchitecture
        
        .setLowestLayer("com.google.appengine")
        
        .addLayerOnTop("org.xydra.xgae")
        
        .addLayerOnTop("org.xydra.store.impl.gae")
        //
        // .addLayerOnTop("org.xydra.persistence")
        //
        // .addLayerOnTop("org.xydra.store")
        
        ;
        
        xydraArchitecture
        
        // .allowAcessFromEveryPackage("org.xydra.valueindex")
                
                .allowAcessFromEveryPackage("org.xydra.sharedutils")
                
                .allowAcessFromEveryPackage("org.xydra.annotations")
                
                .allowAcessFromEveryPackage("org.xydra.log")
                
                .allowAcessFromEveryPackage("org.xydra.xgae.annotations");
        
        xydraArchitecture
        
        // TODO ??
                .ignoreForNow("org.xydra.base")
                
                .ignoreForNow("javax.servlet")
                
                .ignoreForNow("com.google.appengine.repackaged.org.apache.http")
                
                .ignoreForNow("com.google.common")
                
                .ignoreForNow("org.xydra.core")
                
                .ignoreForNow("org.xydra.valueindex")
                
                .ignoreForNow("org.xydra.restless")
                
                .ignoreForNow("org.xydra.index")
                
                .ignoreForNow("org.xydra.perf");
        
        File dot = new File("/Users/xamde/_data_/_p_/2010/org.xydra.devtools/target/result-gae.dot");
        PackageChaos pc = new PackageChaos(xydraArchitecture, dot);
        
        pc.setShowCauses(true);
        
        pc.readImportsFromDir("/Users/xamde/_data_/_p_/2010/org.xydra.gae/src/main/java");
        
        pc.getProject().addSyntheticPackageRelation("com.google.appengine.gae-api", "GAE",
                "com.google.appengine.api.backends");
        pc.getProject().addSyntheticPackageRelation("com.google.appengine.gae-api", "GAE",
                "com.google.appengine.api.capabilities");
        pc.getProject().addSyntheticPackageRelation("com.google.appengine.gae-api", "GAE",
                "com.google.appengine.api.quota");
        
        pc.renderDotGraph();
    }
    
}
