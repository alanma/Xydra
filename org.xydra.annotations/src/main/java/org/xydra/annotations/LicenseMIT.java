package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * License: MIT License
 *
 * Broadly the same as BSD 3-clause;
 *
 * Exact license text varies, many versions;
 *
 * One popular version: http://opensource.org/licenses/MIT (= the expat license)
 *
 * <h2>How to use this annotation</h2> Can annotate classes, methods, packages
 * (package-info.java can contain annotations).
 *
 * Package annotations are inherited on to sub-packages. So annotating
 * package-info in the root of a project annotates the whole project.
 *
 * IMPROVE It would make a somehow cleaner process by using RetentionPolicy
 * SOURCE, then write an AnnotationProcessor to extract some XML file to be put
 * in /META-INF, where another process collects them.
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE,
// TODO parse license annotations also for fields
		ElementType.FIELD })
@LicenseAnnotation(id = "MIT", label = "MIT License")
public @interface LicenseMIT {

	/**
	 * @return the MIT licenses are short, so paste the full text here
	 */
	String licenseText();

	/**
	 * @return ISO date string when the dependency and its licenses text have
	 *         been downloaded
	 */
	String dateRetrieved();

}
