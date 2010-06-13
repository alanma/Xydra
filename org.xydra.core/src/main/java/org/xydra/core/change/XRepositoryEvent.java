package org.xydra.core.change;

import org.xydra.core.model.XID;
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
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the {@link XRepository} where the change
	 *         happened
	 */
	XID getRepositoryID();
	
	/**
	 * WHAT was changed?
	 * 
	 * @return the {@link XID} of the {@link XModel} that was added/removed.
	 */
	XID getModelID();
}
