package org.xydra.core.change;

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
