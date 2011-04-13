package org.xydra.base.change;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;


/**
 * A class that represents a transaction in the Xydra environment.
 * 
 * @author Kaidel
 * 
 *         A Transaction basically is a list of {@link XCommand}s that need to
 *         be executed in an "all or nothing" manner. If one command of the
 *         Transaction is not executable or an error/exception occurs while it
 *         is being executed, the whole Transaction will be rolled back (all
 *         commands that were already executed need to be undone). The
 *         Transaction itself is not responsible that it will or can be executed
 *         in this manner. Classes that implement the
 *         {@link XSynchronizesChanges} interfaces provide an
 *         executeTransaction() method that needs to be implemented in such a
 *         way that this behavior is guaranteed.
 * 
 *         The added commands will be executed in the order of the underlying
 *         list, the command with index 0 will be executed first, then the
 *         command with index 1 etc.
 * 
 *         All commands need to have the same actor, which is specified by the
 *         actor-{@link XID} set by the constructor. Trying to add commands to
 *         the transaction that do not refer to the same actor as the
 *         transaction itself, will not do anything, such commands will not be
 *         added.
 * 
 *         Concerning revision numbers, {@link XCommand XCommands} added to the
 *         transaction need to refer to the revision number of the entity they
 *         want to change BEFORE any command in the transaction is executed. For
 *         example, suppose you want to add 2 {@link XObject XObjects} to and
 *         {@link XModel} with the current revision number 78. Then you'll need
 *         to add two commands that refer to the revision number 78 (and not in
 *         a way, that the first command refers to 78 and the second one to 79 -
 *         this is wrong).
 * 
 *         XTransaction is a subclass of {@link XCommand}, so it is possible to
 *         add a Transaction to a Transaction. Adding a Transaction to another
 *         Transaction will not directly add the Transaction, but get all
 *         commands from it and add these in their order to the Target
 *         Transaction. If any of the commands of the given transaction could
 *         not be added, none of them are added.
 * 
 *         For example:
 * 
 *         Transaction T1 = {a,b,c} Transaction T2 = {d,e} Add T1 to T2 => T2 =
 *         {d,e,a,b,c} (and not {d,e,T1}) This still leaves the Transaction
 *         structure and behavior ("all or nothing") intact.
 * 
 *         This also means that a Transaction is an {@link XCommand} that
 *         actually wraps a set of (logically linked) {@link XCommand}s.
 * 
 *         {@link XRepositoryCommand}s cannot be added to a Transaction,
 *         Transactions only operate on {@link XModel}s, {@link XObject}s and
 *         {@link XField}s.
 * 
 *         The {@link XAddress} of a transaction is address of the model or
 *         object that contains all the objects/fields modified by the
 *         transaction.
 * 
 */
public interface XTransaction extends XCommand, Iterable<XAtomicCommand> {
	
	/**
	 * @return The {@link XCommand} at the given index in this transaction.
	 */
	XAtomicCommand getCommand(int index);
	
	/**
	 * @return the number of {@link XCommand XCommands} in this Transaction.
	 */
	int size();
	
	/**
	 * @return always {@link ChangeType#TRANSACTION}
	 */
	@Override
	ChangeType getChangeType();
	
}
