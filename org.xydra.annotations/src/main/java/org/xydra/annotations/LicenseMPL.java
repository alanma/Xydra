package org.xydra.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * License: Mozilla Public License
 * 
 * Obligations: Code files licensed under the MPL must remain under the MPL and
 * freely available in source form ... all or none of the code in a given source
 * file falls under the MPL.
 * 
 * You must include a copy of this License with every copy of the Source Code
 * 
 * You must cause all Covered Code to which You contribute to contain a file
 * documenting the changes You made to create that Covered Code and the date of
 * any change.
 * 
 * See https://www.mozilla.org/MPL/1.0 https://www.mozilla.org/MPL/1.1/
 * https://www.mozilla.org/MPL/2.0
 * 
 * Can annotate classes, methods, packages (package-info.java can contain
 * annotations).
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
@LicenseAnnotation(id = "MPL", label = "Mozilla Public License")
public @interface LicenseMPL {

	/**
	 * Known license versions are 1.0, 1.1 and 2.0
	 * 
	 * @return
	 */
	String licenseVersion();

	/**
	 * All code annotated with this annotation is considered to be modified.
	 * 
	 * @return true if code was modified (even if just the package was changed)
	 */
	boolean modified() default true;

}
