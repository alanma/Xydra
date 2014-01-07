package org.xydra.conf.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * A hint to the configuration system where which configuration setting is
 * required
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireConf {
    
    /**
     * @return the configuration key that is required at runtime
     */
    String[] value();
    
}
