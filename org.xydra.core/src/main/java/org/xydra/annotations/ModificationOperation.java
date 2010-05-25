package org.xydra.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation exists only for clarity of documentation.
 * 
 * Methods tagged with this annotation change the state. In REST, they are
 * mapped to PUT, POST or DELETE.
 * 
 * @author voelkel
 * 
 */
@Target( { METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ModificationOperation {
	// annotation
}
