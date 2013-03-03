package org.xydra.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Allows to declaratively state where a setting is used. Can be used as
 * documentation for developers and used to run checks with annotation
 * processing tools.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UsesSetting {
    
    /**
     * @return the configuration key, often using a dot-notation
     */
    String value();
    
}
