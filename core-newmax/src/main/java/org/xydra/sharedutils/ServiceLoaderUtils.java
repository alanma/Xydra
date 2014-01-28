package org.xydra.sharedutils;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * Note: could also be done with ResourceFinder
 * 
 * In GWT, use GIN https://code.google.com/p/google-gin/wiki/GinTutorial
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class ServiceLoaderUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderUtils.class);
    
    /**
     * In server-side Java, the config is done via the java.util.ServiceLoader
     * mechanism of config files in META-INF.
     * 
     * @param interfaceToBeLoaded
     * @return the single implementation of the given interface @NeverNull
     * @throws IllegalStateException if 0 or multiple instance are found
     */
    public static <T> T getSingleInstance(Class<T> interfaceToBeLoaded) {
        try {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(interfaceToBeLoaded);
            Iterator<T> it = serviceLoader.iterator();
            if(!it.hasNext()) {
                throw new IllegalStateException(
                        "No providers found. Make sure you have a jar on the classpath"
                                + " with the file " + configFilename(interfaceToBeLoaded));
            }
            T instance = it.next();
            if(it.hasNext()) {
                log.error("Found more than one implementation of "
                        + interfaceToBeLoaded.getCanonicalName() + " where only one was expected");
                log.error("Found: " + instance.getClass().getCanonicalName());
                while(it.hasNext()) {
                    instance = it.next();
                    log.error("Found: " + instance.getClass().getCanonicalName());
                }
                throw new IllegalStateException("Found more than one implementation of "
                        + interfaceToBeLoaded.getCanonicalName());
            } else {
                return instance;
            }
        } catch(NoClassDefFoundError e) {
            throw new IllegalStateException("Class " + interfaceToBeLoaded.getCanonicalName()
                    + " not found on classpath");
        }
    }
    
    /**
     * @param interfaceToBeLoaded
     * @return the name of the config file describing which implementations are
     *         provided, as defined by java.util.ServiceLoader
     */
    public static <T> String configFilename(Class<T> interfaceToBeLoaded) {
        return "/META-INF/services/" + interfaceToBeLoaded.getCanonicalName().replace("/", ".");
    }
    
    public static void main(String[] args) {
        try {
            getSingleInstance(Path.class);
        } catch(Error e) {
            System.out.println(">>>" + e + "<<<");
        }
    }
    
}
