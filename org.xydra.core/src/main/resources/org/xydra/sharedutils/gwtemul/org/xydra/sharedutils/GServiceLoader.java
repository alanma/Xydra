package org.xydra.sharedutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
// super-source
public class GServiceLoader {
    
    public static <T> Collection<T> getAllImplementations(Class<T> interfaze) {
        Set<T> set = new HashSet<T>();
        // TODO load...
        return set;
    }
    
    public static <T> T getSingleInstance(Class<T> interfaceToBeLoaded) {
        // TODO load...
        return null;
    }
    
}
