package org.xydra.core.change;

/**
 * An enumeration for discerning different types of {@link XEvent XEvents} or
 * {@link XCommand XCommands}.
 * 
 */
public enum ChangeType {
	
	/** A "child" entity was added */
	ADD,
	/** A "child" entity was removed */
	REMOVE,
	/** A "child" entity was changed */
	CHANGE,
	/** The change type for transactions */
	TRANSACTION;
	
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
