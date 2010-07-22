package org.xydra.core.model.session;

import java.util.Iterator;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XObject;


/**
 * An XProtectedObject is a wrapper (Decorator) for an {@link XObject} which
 * links the {@link XObject} with a specific actor (represented by its
 * {@link XID}) and automatically checks the access rights for this actor on the
 * {@link XObject}, if a method is called and only executes the method, if the
 * actor is allowed to execute it (otherwise {@link XAccessException
 * XAccessExceptions} will be thrown).
 * 
 * All change operations like adding new {@link XObject XObjects} executed on an
 * XProtectedObject will directly affect the wrapped {@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedObject extends XLoggedObject, XProtectedSynchronizesChanges {
	
	/**
	 * Returns the {@link XField} with the given {@link XID} contained in this
	 * XProtectedObject as an {@link XProtectedField} linked with the actor of
	 * this XProtectedObject.
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} which is to be
	 *            returned
	 * @return The {@link XField} with the given {@link XID} as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject or null, if no corresponding {@link XField}
	 *         exists
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	@ReadOperation
	XProtectedField getField(XID fieldId);
	
	/**
	 * Creates a new {@link XField} and adds it to this XProtectedObject or
	 * returns the already existing {@link XField} if the given {@link XID} was
	 * already taken (both as an {@link XProtectedField} linked with the actor
	 * of this XProtectedObject)
	 * 
	 * @param fieldID The {@link XID} for the {@link XField} which is to be
	 *            created.
	 * @return the newly created {@link XField} or the already existing
	 *         {@link XField} with this {@link XID} (both as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject)
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	XProtectedField createField(XID fieldID);
	
	/**
	 * Removes the {@link XField} with the given {@link XID} from this
	 * XProtectedObject
	 * 
	 * @param fieldID The {@link XID} of the {@link XField} which is to be
	 *            removed
	 * @return true, if the specified {@link XField} did exist and could be
	 *         removed
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	boolean removeField(XID fieldID);
	
	/**
	 * Executes the given {@link XObjectCommand} if possible.
	 * 
	 * This method will fail if, the given {@link XObjectCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XObjectCommand}: the specified {@link XField} does
	 * not exist and therefore cannot be removed
	 * <li>Add-type {@link XObjectCommand}: the given {@link XID} is already
	 * taken and therefore a new {@link XField} with this {@link XID} cannot be
	 * created
	 * <li>the object-{@link XID} in the {@link XObjectCommand} does not concur
	 * with the {@link XID} of this XObject
	 * </ul>
	 * 
	 * @param command The {@link XObjectCommand} which is to be executed
	 * @return {@link XCommand#FAILED} if executing the {@link XObjectCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XObjectCommand} didn't change anything or if executing the
	 *         {@link XObjectCommand} succeeded the revision number of the
	 *         {@link XObjectEvent} caused by the {@link XObjectCommand}.
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	long executeObjectCommand(XObjectCommand command);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	long getRevisionNumber();
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean hasField(XID objectID);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean isEmpty();
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	Iterator<XID> iterator();
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForObjectEvents(XObjectEventListener changeListener);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForFieldEvents(XFieldEventListener changeListener);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForTransactionEvents(XTransactionEventListener changeListener);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read or write access,
	 *             depending on the {@link XCommand XCommands} in the
	 *             {@link XTransaction}) to execute this method
	 */
	long executeTransaction(XTransaction transaction);
	
	/**
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (read or write access,
	 *             depending on the {@link XCommand}) to execute this method
	 */
	long executeCommand(XCommand command);
	
}
