package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: If this annotation is present, the field is indexed. String
 * denotes where. E.g. "server".
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Indexed {
	String[] value();
}
