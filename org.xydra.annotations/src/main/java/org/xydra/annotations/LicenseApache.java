package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can annotate classes, methods, packages (package-info.java can contain
 * annotations).
 * 
 * Package annotations are inherited on to sub-packages. So annotating
 * package-info in the root of a project annotates the whole project.
 * 
 * @author xamde
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE })
public @interface LicenseApache {

	/**
	 * @return the short project name
	 */
	String project() default "";

	/**
	 * @return one line of copyright info
	 */
	String copyright() default "";

	/**
	 * @return complete list of contributors
	 */
	String contributors() default "";

	/**
	 * Syntax convention: "NOTICE.(some unique name).txt", e.g.
	 * "NOTICE.apache-commons-lang.txt"
	 * 
	 * @return resource name of NOTICE file, if any.
	 */
	String notice() default "";

	/**
	 * All code annotated with this annotation is considered to be modified.
	 * 
	 * @return true if code was modified (even if just the package was changed)
	 */
	boolean modified() default true;

}
