package org.xydra.core.change;

import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XModel XModels} to/from the
 * specified {@link XRepository}
 * 
 */

public interface XRepositoryCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if an {@link XModel} with the specified
	 * {@link XID} already exists, while a safe add will only succeed if no such
	 * {@link XModel} exists.
	 * 
	 * A forced remove will succeed whether an {@link XModel} with the specified
	 * {@link XID} exists or not, while a safe remove will only succeed if such
	 * an {@link XModel} exists.
	 * 
	 * Furthermore forced commands will ignore the current revision number of
	 * the specified {@link XModel} while safe commands can only be executed if
	 * their revision number fits to the current revision number.
	 * 
	 * @return true, if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the {@link XID} of the {@link XRepository} this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return the {@link XID} of the {@link XModel} this command will
	 *         add/remove
	 */
	XID getModelID();
	
	/**
	 * @return the current revision number of the {@link XModel} which will be
	 *         added/removed
	 */
	long getRevisionNumber();
	
}
