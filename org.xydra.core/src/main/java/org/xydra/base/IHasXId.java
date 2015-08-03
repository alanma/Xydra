package org.xydra.base;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;

/**
 * Objects of classes that implement this interface have an {@link XId}.
 *
 * @author xamde
 */
public interface IHasXId {

	/**
	 * Returns the {@link XId} of this entity.
	 *
	 * @return The {@link XId} of this entity. @NeverNull
	 * @throws IllegalStateException if this entity has already been removed
	 */
	@ReadOperation
	@NeverNull
	XId getId();

}
