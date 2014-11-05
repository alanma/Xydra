package org.xydra.base.value;

import java.io.Serializable;


/**
 * A class for storing actual values in fields. All values in Xydra are
 * immutable.
 * 
 * An XValue may be used for a lot of different things, for example for storing
 * the name of a person.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XValue extends Serializable {

	/**
	 * This method allows to have an abstract, defined way to denote the single
	 * type of a value without having to know which of the implemented
	 * interfaces are relevant. Mostly useful for writing import/export
	 * routines.
	 * 
	 * @return the type of this value
	 */
	ValueType getType();

}
