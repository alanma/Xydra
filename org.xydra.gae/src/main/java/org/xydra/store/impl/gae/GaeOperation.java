package org.xydra.store.impl.gae;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A summary of which GAE persistence and memcache operations are triggered by
 * this method.
 * 
 * @author xamde
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface GaeOperation {
	
	boolean datastoreRead() default false;
	
	boolean datastoreWrite() default false;
	
	boolean memcacheRead() default false;
	
	boolean memcacheWrite() default false;
	
}
