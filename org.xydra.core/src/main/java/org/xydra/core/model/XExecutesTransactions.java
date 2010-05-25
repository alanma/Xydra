package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;



/**
 * An interface that indicates that this object is able to execute
 * {@link XTransaction}s.
 * 
 * @author Kaidel
 * 
 */
public interface XExecutesTransactions {
	
	/**
	 * This method executes the given transaction and returns true, if it was
	 * successful.
	 * 
	 * It is up to the implementation when this operation fails and returns
	 * false, but it should always return false if the given actor-XID does not
	 * concur with the actor-XID of the given transaction.
	 * 
	 * @param transaction
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeTransaction(XID actor, XTransaction transaction);
	
}
