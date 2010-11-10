package org.xydra.core.value;

import java.io.Serializable;

import org.xydra.core.model.XField;


/**
 * A class for storing actual values in {@link XField XFields}. All values in
 * Xydra are immutable.
 * 
 * An XValue may be used for a lot of different things, for example for storing
 * the name of a person.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XValue extends Serializable {
	
}
