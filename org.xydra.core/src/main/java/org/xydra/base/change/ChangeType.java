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

}
