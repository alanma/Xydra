package org.xydra.conf.impl;

import org.xydra.annotations.RunsInGWT;
import org.xydra.conf.ConfBuilder;

import com.google.gwt.core.shared.GWT;


@RunsInGWT(true)
// via super-source
public class MemoryConfig_GwtEmul {
    
    /**
     * @param clazz @NeverNull
     * @return an instance
     */
    public static <T> T newInstance(Class<T> clazz) {
        throw new IllegalArgumentException("Cannot resolve the dynamic class "
                + clazz.getName() + "in GWT. Use another kind of resolver for GWT.");
        
//        if(clazz.equals(ConfBuilder.class)) {
//            return GWT.create(ConfBuilder.class);
//        } else {
//        }
    }
    
    public static Class<?> classForName(String className) {
        // no dynamic class loading in GWT
        return null;
    }
    
}
