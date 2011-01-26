package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Code with this tag runs in GWT compiled JavaScript.
 * 
 * TODO IMPROVE gwt module file's exclude section: consider generating the from
 * all files in /src/main/java without this annotation.
 * 
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RunsInGWT {
	boolean value();
}
