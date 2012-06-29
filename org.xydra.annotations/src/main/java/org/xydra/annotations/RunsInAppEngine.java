package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Code with this tag runs in the Google AppEngine for Java (GAE/J).
 * 
 * AppEngine has no notion of files.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RunsInAppEngine {
	boolean value();
}
