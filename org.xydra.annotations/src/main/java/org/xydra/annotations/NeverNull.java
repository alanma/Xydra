package org.xydra.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * The annotated type can never be null, its author believes.
 * 
 * Annotations to declare that a method parameter can be null or can never be
 * null, be it at runtime or compile time, is a big mess in the Java world.
 * Until Java 8, it seems likely no standard will emerge. See
 * http://stackoverflow
 * .com/questions/4963300/which-notnull-java-annotation-should-i-use
 * 
 * @author xamde
 * 
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface NeverNull {
	
}
