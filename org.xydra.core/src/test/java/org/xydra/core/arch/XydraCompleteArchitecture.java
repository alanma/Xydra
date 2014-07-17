package org.xydra.core.arch;

import java.io.File;
import java.io.IOException;

import org.xydra.devtools.javapackages.PackageChaos;
import org.xydra.devtools.javapackages.Project;
import org.xydra.devtools.javapackages.architecture.Architecture;
import org.xydra.devtools.javapackages.architecture.Layer;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;


public class XydraCompleteArchitecture {
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
        try {
            Log4jUtils.listConfigFromClasspath();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        String scope = "org.xydra";
        
        Architecture a = new Architecture(scope);
        a.allowAcessFromEveryPackage("org.xydra.annotation");
        a.allowAcessFromEveryPackage("org.xydra.log");
        a.allowAcessFromEveryPackage("org.xydra.index");
        // IMPROVE architect cleaner
        a.ignoreForNow("org.xydra.perf");
        
        Layer sharedutils = a.defineLayer("org.xydra.sharedutils");
        Layer vi = a.defineLayer("org.xydra.valueindex");
        
        Layer base = a.defineLayer("org.xydra.base");
        Layer core = a.defineLayer("org.xydra.core");
        Layer persistence = a.defineLayer("org.xydra.persistence");
        Layer store = a.defineLayer("org.xydra.store");
        
        vi.mayAccess(base, core, sharedutils);
        
        base.mayAccess(vi, sharedutils);
        core.mayAccess(base, vi, sharedutils);
        persistence.mayAccess(core, base, vi, sharedutils);
        store.mayAccess(persistence, core, base, vi, sharedutils);
        
        File dot = new File("./target/corebaseetc.dot");
        PackageChaos pc = new PackageChaos(a, dot);
        pc.setShowCauses(true);
        
        Project p = pc.getProject();
        p.scanDir("src/main/java");
        
        pc.renderDotGraph();
    }
    
}