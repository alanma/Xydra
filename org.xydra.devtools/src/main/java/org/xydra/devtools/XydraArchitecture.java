package org.xydra.devtools;

import java.io.File;
import java.io.IOException;

import org.xydra.devtools.architecture.Architecture;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.Log4jLoggerFactory;
import org.xydra.log.util.Log4jUtils;


public class XydraArchitecture {
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory());
        try {
            Log4jUtils.listConfigFromClasspath();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        Architecture xydraArchitecture = new Architecture("org.xydra");
        xydraArchitecture
        
        .setLowestLayer("org.xydra.index")
        
        .addLayerOnTop("org.xydra.base")
        
        .addLayerOnTop("org.xydra.store")
        
        .addLayerOnTop("org.xydra.core");
        
        xydraArchitecture
        
        // .allowAcessFromEveryPackage("org.xydra.valueindex")
                
                .allowAcessFromEveryPackage("org.xydra.sharedutils")
                
                .allowAcessFromEveryPackage("org.xydra.annotations")
                
                .allowAcessFromEveryPackage("org.xydra.log");
        
        // do not ignore forever
        xydraArchitecture
        
        .ignoreForNow("org.xydra.valueindex")
        
        .ignoreForNow("org.xydra.valueindex")
        
        .ignoreForNow("org.xydra.perf");
        
        File dot = new File("/Users/xamde/_data_/_p_/2013/org.xydra.devtools/target/res.dot");
        PackageChaos pc = new PackageChaos(xydraArchitecture, dot);
        pc.analyse("/Users/xamde/_data_/_p_/2010/org.xydra.core/src/main/java");
    }
    
    /**
     * <pre>
     *       cluster_org_xydra_core
     *       org.xydra.store.impl.memory -> org.xydra.core causes: <MemoryModelPersistence>
     *       org.xydra.base.rmof.impl.memory -> org.xydra.core causes: <SimpleModel>
     *       org.xydra.base -> org.xydra.core causes: <XIdProvider>
     *         cluster_org_xydra_core_model
     *         org.xydra.base.change.impl.memory -> org.xydra.core.model causes: <AbstractTransactionEvent>
     *         org.xydra.store -> org.xydra.core.model causes: <AccessException>
     *         org.xydra.base.change -> org.xydra.core.model causes: <XCommand>
     *         org.xydra.base.value -> org.xydra.core.model causes: <XValue>
     *         org.xydra.store.access -> org.xydra.core.model causes: <XAccessControlManager>
     *         org.xydra.store.impl.delegate -> org.xydra.core.model causes: <XydraBlockingStore>
     *         org.xydra.base -> org.xydra.core.model causes: <X>
     *           cluster_org_xydra_core_model_delta
     *           org.xydra.store.impl.memory -> org.xydra.core.model.delta causes: <MemoryModelPersistence>
     *           cluster_org_xydra_core_model_impl
     *             cluster_org_xydra_core_model_impl_memory
     *             org.xydra.store.access.impl.memory -> org.xydra.core.model.impl.memory causes: <DelegatingAccessControlManager>
     *             org.xydra.store -> org.xydra.core.model.impl.memory causes: <XydraRuntime>
     *             org.xydra.base -> org.xydra.core.model.impl.memory causes: <X>
     *         cluster_org_xydra_core_serialize
     *         org.xydra.store.impl.rest -> org.xydra.core.serialize causes: <AbstractXydraStoreRestClient>
     *           cluster_org_xydra_core_serialize_json
     *           org.xydra.base.value -> org.xydra.core.serialize.json causes: <XValueStreamHandler>
     *           cluster_org_xydra_core_serialize_xml
     *           org.xydra.base.value -> org.xydra.core.serialize.xml causes: <XValueStreamHandler>
     *         cluster_org_xydra_core_util
     *         org.xydra.store -> org.xydra.core.util causes: <XydraRuntime_GwtEmulated>
     *         org.xydra.base.rmof.impl.memory -> org.xydra.core.util causes: <SimpleField>     *
     * </pre>
     * 
     */
    
}
