package org.xydra.core.change;

import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


public interface XFieldCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if the added field existed before while a
	 * safe add will only succeed if the field didn't exist before.
	 * 
	 * A forced remove will succeed whether the object had the specified field
	 * (ignoring it's revision number) or not while a safe remove will only
	 * succeed if the field exists and has the specified revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the field was already removed.
	 * 
	 * @return true if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the value set by this command. Always null for commands of type
	 *         REMOVE.
	 */
	XValue getValue();
	
	/**
	 * @return the XID of the repository this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return the XID of the model containing the field this command refers to
	 *         (may be null)
	 */
	XID getModelID();
	
	/**
	 * @return the XID of the object containing the field this command refers to
	 *         (may be null)
	 */
	XID getObjectID();
	
	/**
	 * @return the XID of the field this command will add/remove
	 */
	XID getFieldID();
	
	/**
	 * @return the current revision number of the field this XCommand refers to
	 */
	long getRevisionNumber();
	
}
