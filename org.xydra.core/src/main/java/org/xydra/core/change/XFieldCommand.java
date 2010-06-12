package org.xydra.core.change;

import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


/**
 * An {@link XCommand} for adding, removing or changing the {@link XValue} of
 * the specified {@link XField}
 * 
 */

public interface XFieldCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if the {@link XValue} of the specified
	 * {@link XField} was set, while a safe add will only succeed if the value
	 * wasn't set.
	 * 
	 * A forced remove/change will succeed whether the {@link XValue} of the
	 * specified {@link XField} was set or not while a safe remove/change will
	 * only succeed if the {@link XValue} was set.
	 * 
	 * Furthermore forced commands will ignore the current revision number of
	 * the specified {@link XField} while safe commands can only be executed if
	 * their revision number fits to the current revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the field was already removed.
	 * 
	 * @return true, if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the {@link XValue} set by this command. Always null for commands
	 *         of type REMOVE.
	 */
	XValue getValue();
	
	/**
	 * @return the {@link XID} of the {@link XRepository} containing the
	 *         {@link XField} this command refers to (may be null)
	 */
	XID getRepositoryID();
	
	/**
	 * @return the {@link XID} of the {@link XModel} containing the
	 *         {@link XField} this command refers to (may be null)
	 */
	XID getModelID();
	
	/**
	 * @return the {@link XID} of the {@link XObject} containing the
	 *         {@link XField} this command refers to (may be null)
	 */
	XID getObjectID();
	
	/**
	 * @return the {@link XID} of the {@link XField} this command will
	 *         add/remove
	 */
	XID getFieldID();
	
	/**
	 * @return the current revision number of {@link XField} the which value
	 *         will be changed by this command
	 */
	long getRevisionNumber();
	
}
