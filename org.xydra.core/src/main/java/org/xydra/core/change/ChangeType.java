package org.xydra.core.change;

/**
 * An enumeration for discerning different types of {@link XEvent XEvents}.
 * 
 */

public enum ChangeType {
	/** A "child" object is added */
	ADD,
	/** A "child" object is removed */
	REMOVE,
	/** A "child" object is changed */
	CHANGE,
	/** The change type for transactions */
	TRANSACTION
}
