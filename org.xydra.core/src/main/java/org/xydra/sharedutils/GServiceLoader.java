package org.xydra.sharedutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
// super-source
public class GServiceLoader {
    
    public static <T> Collection<T> getAllImplementations(Class<T> interfaze) {
        Set<T> set = new HashSet<T>();
        ServiceLoader<T> serviceLoader = ServiceLoader.load(interfaze);
        for(T implementation : serviceLoader) {
            set.add(implementation);
        }
        return set;
    }
    
    public static <T> T getSingleInstance(Class<T> interfaceToBeLoaded) {
        return ServiceLoaderUtils.getSingleInstance(interfaceToBeLoaded);
    }
    
}
