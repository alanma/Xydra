package org.xydra.core.change;

import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XObjects} to/from the
 * specified {@link XModel}
 * 
 */

public interface XModelCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if an {@link XObject} with the specified
	 * {@link XID} already exists, while a safe add will only succeed if no such
	 * {@link XObject} exists.
	 * 
	 * A forced remove will succeed whether an {@link XObject} with the
	 * specified {@link XID} exists, while a safe remove will only succeed if
	 * such an {@link XObject} exists.
	 * 
	 * Furthermore forced commands will ignore the current revision number of
	 * the specified {@link XModel} while safe commands can only be executed if
	 * their revision number fits to the current revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the object was already removed.
	 * 
	 * @return true, if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the {@link XID} of the {@link XRepository} holding the
	 *         {@link XModel} this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return The {@link XID} of the {@link XModel} this command refers to
	 */
	XID getModelID();
	
	/**
	 * @return The {@link XID} of the {@link XObject} this command will
	 *         remove/add if it can be successfully executed
	 */
	XID getObjectID();
	
	/**
	 * @return the current revision number of the {@link XObject} this XCommand
	 *         refers to
	 */
	long getRevisionNumber();
	
}
