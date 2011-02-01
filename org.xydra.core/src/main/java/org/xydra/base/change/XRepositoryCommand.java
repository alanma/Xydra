package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XModel XModels} to/from the
 * specified {@link XRepository}
 * 
 */

public interface XRepositoryCommand extends XAtomicCommand {
	
	/**
	 * @return the {@link XID} of the {@link XModel} this command will
	 *         add/remove
	 */
	XID getModelId();
	
	/**
	 * @return the {@link XID} of the {@link XRepository} this command refers to
	 */
	XID getRepositoryId();
	
	/**
	 * @return the current revision number of the {@link XModel} which will be
	 *         added/removed
	 */
	long getRevisionNumber();
	
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
	 * TODO can this return {@link ChangeType#CHANGE} ?
	 * 
	 * @see org.xydra.base.change.XCommand#getChangeType()
	 */
	@Override
	ChangeType getChangeType();
	
}
