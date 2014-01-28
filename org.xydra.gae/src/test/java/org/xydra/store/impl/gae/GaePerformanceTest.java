package org.xydra.store.impl.gae;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.xydra.base.XId;
import org.xydra.core.AbstractPersistencePerformanceTest;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraRuntime;


public class GaePerformanceTest extends AbstractPersistencePerformanceTest {
    
    private static final Logger log = LoggerFactory.getLogger(GaePerformanceTest.class);
    
    @Override
    public XydraPersistence createPersistence(XId repositoryId) {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
        configureLog4j();
        InstanceContext.clear();
        XydraRuntime.init();
        XydraPersistence p = new GaePersistence(repositoryId);
        assert p.getManagedModelIds().isEmpty();
        return p;
    }
    
    public static void configureLog4j() {
        File file = new File("./src/test/resources/log4j.properties");
        if(!file.exists()) {
            log.warn("Could not update log conf at runtime from file '" + file.getAbsolutePath()
                    + "' -- not found");
        }
        Properties props = new Properties();
        Reader r;
        try {
            r = new FileReader(file);
            props.load(r);
            r.close();
            // TODO Do we really want that?
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(props);
            log.info("Updated local log config from " + file.getAbsolutePath());
        } catch(FileNotFoundException e) {
        } catch(IOException e) {
        }
        
    }
    
}
