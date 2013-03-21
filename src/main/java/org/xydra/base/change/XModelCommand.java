package org.xydra.base.change;

import org.xydra.base.XId;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XObject XObjects} to/from the
 * specified {@link XModel}
 * 
 */
public interface XModelCommand extends XAtomicCommand {
	
	/**
	 * @return The {@link XId} of the {@link XModel} this command refers to
	 */
	XId getModelId();
	
	/**
	 * @return The {@link XId} of the {@link XObject} this command will
	 *         remove/add if it can be successfully executed
	 */
	XId getObjectId();
	
	/**
	 * @return the {@link XId} of the Parent-{@link XRepository} of the
	 *         {@link XModel} this command refers to (may be null)
	 */
	XId getRepositoryId();
	
	/**
	 * @return the current revision number of the {@link XObject} which will be
	 *         added/removed
	 */
	@Override
    long getRevisionNumber();
	
	/**
	 * A forced add will succeed even if an {@link XObject} with the specified
	 * {@link XId} already exists, while a safe add will only succeed if no such
	 * {@link XObject} exists.
	 * 
	 * A forced remove will succeed whether an {@link XObject} with the
	 * specified {@link XId} exists or not, while a safe remove will only
	 * succeed if such an {@link XObject} exists.
	 * 
	 * Furthermore forced commands will ignore the current revision number of
	 * the specified {@link XModel} while safe commands can only be executed if
	 * their revision number fits to the current revision number.
	 * 
	 * @return true, if this event is forced.
	 */
	@Override
    boolean isForced();
	
}
