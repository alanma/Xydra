package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions}.
 * 
 */
public interface XProtectedExecutesTransactions {
	
	/**
	 * This method executes the given {@link XTransaction}.
	 * 
	 * An implementation has to make sure to execute it as described in the
	 * documentation of {@link XTransaction}.
	 * 
	 * @param transaction the {@link XTransaction} which is to be executed
	 * @return {@link XCommand#FAILED} if the transaction failed,
	 *         {@link XCommand#NOCHANGE} if the transaction didn't change
	 *         anything or the revision number of the {@link XEvent} caused by
	 *         the transaction.
	 */
	@ModificationOperation
	long executeTransaction(XTransaction transaction);
	
}
