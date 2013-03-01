package org.xydra.base.change;

import org.xydra.base.XId;
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
	 * @return the {@link XId} of the {@link XField} this command will
	 *         add/remove
	 */
	XId getFieldId();
	
	/**
	 * @return the {@link XId} of the Parent-{@link XModel} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XId getModelId();
	
	/**
	 * @return the {@link XId} of the {@link XObject} this command refers to
	 */
	XId getObjectId();
	
	/**
	 * @return the {@link XId} of the Parent-{@link XRepository} of the
	 *         {@link XObject} this command refers to (may be null)
	 */
	XId getRepositoryId();
	
	/**
	 * @return the current revision number of the {@link XField} which will be
	 *         added/removed
	 */
	@Override
    long getRevisionNumber();
	
	/**
	 * A forced add will succeed even if an {@link XField} with the specified
	 * {@link XId} already exists, while a safe add will only succeed if no such
	 * {@link XField} exists.
	 * 
	 * A forced remove will succeed whether an {@link XField} with the specified
	 * {@link XId} exists or not, while a safe remove will only succeed if such
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
