package org.xydra.base;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;

/**
 * Objects of classes that implement this interface have a label.
 * Helps debugging and auto-generating UIs.
 *
 * @author xamde
 */
public interface IHasLabel {

	/**
	 * Returns a human-readable, short label
	 *
	 * @return A short String @NeverNull
	 */
	@ReadOperation
	@NeverNull
	String getLabel();

}
