package org.xydra.base.change;

/**
 * An enumeration for discerning different types of {@link XEvent XEvents} or
 * {@link XCommand XCommands}.
 * 
 */
public enum ChangeType {
	
	/** A "child" entity was added */
	ADD,
	/** A "child" entity was changed */
	CHANGE,
	/** A "child" entity was removed */
	REMOVE,
	/** The change type for transactions */
	TRANSACTION;
	
	/**
	 * Returns the fitting ChangeType enumeration value to a given string.
	 * 
	 * @param str The string for which the fitting ChangeTpye enumeration value
	 *            is to be returned
	 * @return the ChangeType enumeration value of the specified type or null if
	 *         the given String is neither "ADD", "REMOVE", "CHANGE" or
	 *         "TRANSACTION"
	 */
	public static ChangeType fromString(String str) {
		if(ADD.toString().equals(str)) {
			return ADD;
		} else if(REMOVE.toString().equals(str)) {
			return REMOVE;
		} else if(CHANGE.toString().equals(str)) {
			return CHANGE;
		} else if(TRANSACTION.toString().equals(str)) {
			return TRANSACTION;
		}
		return null;
	}
	
}
