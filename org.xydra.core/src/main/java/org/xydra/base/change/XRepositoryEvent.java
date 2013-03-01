package org.xydra.base.change;

import org.xydra.base.XId;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XEvent} representing changes of {@link XRepository XRepositories}.
 * 
 * @author voelkel
 * 
 */

public interface XRepositoryEvent extends XAtomicEvent {
	
	/**
	 * WHAT was changed?
	 * 
	 * @return the {@link XId} of the {@link XModel} that was added/removed.
	 */
	XId getModelId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XId} of the {@link XRepository} where the change
	 *         happened
	 */
	XId getRepositoryId();
}
