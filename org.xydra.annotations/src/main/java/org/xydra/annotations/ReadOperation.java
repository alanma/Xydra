package org.xydra.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation exists only for clarity of documentation.
 * 
 * Methods tagged with this annotation are side-effect free read operations. In
 * REST, methods like this are mapped to GET.
 * 
 * @author voelkel
 * 
 */
@Target({ METHOD })
@Retention(RetentionPolicy.SOURCE)
public @interface ReadOperation {
	// annotation
}
