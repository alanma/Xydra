package org.xydra.core.change;

import org.xydra.core.model.XID;


public interface XRepositoryCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if the added model existed before while a
	 * safe add will only succeed if the model didn't exist before.
	 * 
	 * A forced remove will succeed whether the model had the specified model
	 * (ignoring it's revision number) or not while a safe remove will only
	 * succeed if the model exists and has the specified revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the model was already removed.
	 * 
	 * @return true if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the XID of the repository this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return the XID of the model this command will add/remove
	 */
	XID getModelID();
	
	/**
	 * @return the current revision number of the model this XCommand refers to
	 */
	long getRevisionNumber();
	
}
