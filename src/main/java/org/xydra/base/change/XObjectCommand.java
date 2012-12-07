package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An {@link XCommand} for adding/removing {@link XField XFields} to/from the
 * specified {@link XObject}
 * 
 */

public interface XObjectCommand extends XAtomicCommand {
	
	/**
	 * @return the {@link XID} of the {@link XField} this command will
	 *         add/remove
	 */
	XID getFieldId();
	
	/**
	 * @return the {@link XID} of the Parent-{@link XModel} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XID getModelId();
	
	/**
	 * @return the {@link XID} of the {@link XObject} this command refers to
	 */
	XID getObjectId();
	
	/**
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XID getRepositoryId();
	
	/**
	 * @return the current revision number of the {@link XField} which will be
	 *         added/removed
	 */
	@Override
    long getRevisionNumber();
	
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
	 * @return true, if this event is forced.
	 */
	@Override
    boolean isForced();
	
}
