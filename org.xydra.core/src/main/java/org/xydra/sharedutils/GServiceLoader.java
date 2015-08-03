package org.xydra.sharedutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
// super-source
public class GServiceLoader {

    public static <T> Collection<T> getAllImplementations(final Class<T> interfaze) {
        final Set<T> set = new HashSet<T>();
        final ServiceLoader<T> serviceLoader = ServiceLoader.load(interfaze);
        for(final T implementation : serviceLoader) {
            set.add(implementation);
        }
        return set;
    }

    public static <T> T getSingleInstance(final Class<T> interfaceToBeLoaded) {
        return ServiceLoaderUtils.getSingleInstance(interfaceToBeLoaded);
    }

}
