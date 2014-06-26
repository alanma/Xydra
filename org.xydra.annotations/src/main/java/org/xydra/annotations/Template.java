package org.xydra.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Methods annotated with this annotation are accessed from (e.g. Apache
 * Velocity) templates. Rename and change them with care. They might also be
 * used for auto-generated documentation for templates.
 * 
 * @author voelkel
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Template {
    String value();
}
