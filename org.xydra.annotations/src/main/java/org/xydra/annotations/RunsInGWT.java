package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Code with this tag runs in GWT compiled JavaScript (if the boolean is set to
 * true).
 *
 * IMPROVE gwt module file's exclude section: consider generating it from all
 * files in /src/main/java without this annotation.
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RunsInGWT {
	boolean value();
}
