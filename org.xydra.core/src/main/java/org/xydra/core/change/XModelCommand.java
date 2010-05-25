package org.xydra.core.change;

import org.xydra.core.model.XID;


// FIXME max: why not use a class here
public interface XModelCommand extends XAtomicCommand {
	
	/**
	 * A forced add will succeed even if the added object existed before while a
	 * safe add will only succeed if the object didn't exist before.
	 * 
	 * A forced remove will succeed whether the object had the specified object
	 * (ignoring it's revision number) or not while a safe remove will only
	 * succeed if the object exists and has the specified revision number.
	 * 
	 * TODO there is no way to specify a remove that will succeed if the
	 * revision number was different but fail if the object was already removed.
	 * 
	 * @return true if this event is forced.
	 */
	boolean isForced();
	
	/**
	 * @return the XID of the repository this command refers to
	 */
	XID getRepositoryID();
	
	/**
	 * @return The XID of the model this command should be executed in
	 */
	XID getModelID();
	
	/**
	 * @return The XID of the object this command will delete/add
	 */
	XID getObjectID();
	
	/**
	 * @return the current revision number of the object this XCommand refers to
	 */
	long getRevisionNumber();
	
}
