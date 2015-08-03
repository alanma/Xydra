package org.xydra.oo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotates a class/interface member (method or type) with the Xydra field ID
 * it should be persisted into
 *
 * @author xamde
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

	String value();

}
