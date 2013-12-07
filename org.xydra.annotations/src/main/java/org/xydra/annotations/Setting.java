package org.xydra.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * A setting that allows to fine-tune behaviour at runtime or compile time.
 * 
 * Annotate compile time or hard-coded runtime flags with this annotation.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Setting {
    
    String value();
    
}
