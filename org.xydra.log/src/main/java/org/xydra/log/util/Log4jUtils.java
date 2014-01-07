package org.xydra.log.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Utility class for managing log4j.properties files. If some bundled jar
 * contains a log4j.propeties (e.g. it might come in from a /src/test/resources
 * folder), then the first log4j.properties found on the classpath is used. To
 * fix that, this class allows to explicitly load a local file from
 * /src/main/resources and apply the config listed in there.
 * 
 * @author xamde
 */
public class Log4jUtils {
    
    private static final Logger log = LoggerFactory.getLogger(Log4jUtils.class);
    
    /**
     * Dump log4j.properties resource from classpath to System.out
     * 
     * @throws IOException
     */
    public static void listConfigFromClasspath() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream("log4j.properties");
        if(in == null) {
            System.out.println("Found not log4j.properties on classpath.");
            return;
        }
        Reader r = new InputStreamReader(in, "utf-8");
        String s = IOUtils.toString(r);
        System.out.println("Found config:\n" + s);
    }
    
    /**
     * Tweak log level at runtime
     * 
     * @param clazz
     * @param log4jLevel
     */
    public static void setLevel(Class<?> clazz, Level log4jLevel) {
        LogManager.getLogger(clazz).setLevel(log4jLevel);
    }
    
    /**
     * Use local FILE (not resource)
     * './src/{main,test}/resources/log4j.properties' to overwrite log4j
     * settings
     */
    public static void configureLog4j() {
        File file = new File("./src/main/resources/log4j.properties");
        if(!file.exists()) {
            file = new File("./src/test/resources/log4j.properties");
        }
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
    
    public static void main(String[] args) throws IOException {
        listConfigFromClasspath();
    }
    
}
