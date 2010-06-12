package org.xydra.core.change;

/**
 * An enumeration for discerning different types of {@link XEvent XEvents}.
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
	TRANSACTION
}
