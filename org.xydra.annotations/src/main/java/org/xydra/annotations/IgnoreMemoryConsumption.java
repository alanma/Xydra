package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * For some profilers: Fields marked with this annotation should not be
 * considered to consume memory. This makes sense in some memory-optimisation
 * contexts.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface IgnoreMemoryConsumption {
    
}
