package org.xydra.core.model.session;

import java.util.Iterator;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XObject;
import org.xydra.store.AccessException;


/**
 * An XProtectedObject is a wrapper (Decorator) for an {@link XObject} which
 * links the {@link XObject} with a specific actor (represented by its
 * {@link XID}) and automatically checks the access rights for this actor on the
 * {@link XObject}, if a method is called and only executes the method, if the
 * actor is allowed to execute it (otherwise {@link AccessException
 * XAccessExceptions} will be thrown).
 * 
 * All change operations like adding new {@link XObject XObjects} executed on an
 * XProtectedObject will directly affect the wrapped {@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedObject extends XLoggedObject, XWritableObject, IHasChangeLog,
        XExecutesCommands {
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	boolean addListenerForFieldEvents(XFieldEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	boolean addListenerForObjectEvents(XObjectEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	boolean addListenerForTransactionEvents(XTransactionEventListener changeListener);
	
	/**
	 * Creates a new {@link XField} and adds it to this XProtectedObject or
	 * returns the already existing {@link XField} if the given {@link XID} was
	 * already taken (both as an {@link XProtectedField} linked with the actor
	 * of this XProtectedObject)
	 * 
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            created.
	 * @return the newly created {@link XField} or the already existing
	 *         {@link XField} with this {@link XID} (both as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject)
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@ModificationOperation
	XProtectedField createField(XID fieldId);
	
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
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@ModificationOperation
	long executeObjectCommand(XObjectCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getActor();
	
	/**
	 * Returns the {@link XField} with the given {@link XID} contained in this
	 * XProtectedObject as an {@link XProtectedField} linked with the actor of
	 * this XProtectedObject.
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} which is to be
	 *            returned
	 * @return The {@link XField} with the given {@link XID} as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject or null, if no corresponding {@link XField}
	 *         exists
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	@ReadOperation
	XProtectedField getField(XID fieldId);
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	long getRevisionNumber();
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	boolean hasField(XID objectId);
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	boolean isEmpty();
	
	/**
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	Iterator<XID> iterator();
	
	/**
	 * Removes the {@link XField} with the given {@link XID} from this
	 * XProtectedObject
	 * 
	 * @param fieldId The {@link XID} of the {@link XField} which is to be
	 *            removed
	 * @return true, if the specified {@link XField} did exist and could be
	 *         removed
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@ModificationOperation
	boolean removeField(XID fieldId);
	
}
