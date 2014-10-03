package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documentation: Which security considerations are necessary for this element?
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE })
public @interface Security {
	String value();
}
