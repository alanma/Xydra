package org.xydra.core.change;

import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XFields} to/from the specified
 * {@link XObject}
 * 
 */

public interface XObjectCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if an {@link XField} with the specified
	 * {@link XID} already exists, while a safe add will only succeed if no such
	 * {@link XField} exists.
	 * 
	 * A forced remove will succeed whether an {@link XField} with the specified
	 * {@link XID} exists or not, while a safe remove will only succeed if such
	 * an {@link XField} exists.
	 * 
	 * Furthermore forced commands will ignore the current revision number of
	 * the specified {@link XObject} while safe commands can only be executed if
	 * their revision number fits to the current revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the field was already removed.
	 * 
	 * @return true, if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XID getRepositoryID();
	
	/**
	 * @return the {@link XID} of the Parent-{@link XModel} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XID getModelID();
	
	/**
	 * @return the {@link XID} of the {@link XObject} this command refers to
	 */
	XID getObjectID();
	
	/**
	 * @return the {@link XID} of the {@link XField} this command will
	 *         add/remove
	 */
	XID getFieldID();
	
	/**
	 * @return the current revision number of the {@link XField} which will be
	 *         added/removed
	 */
	long getRevisionNumber();
	
}
