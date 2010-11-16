package org.xydra.core.model.session;

import java.util.Iterator;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.store.AccessException;


/**
 * An XProtectedModel is a wrapper (Decorator) for an {@link XModel} which links
 * the {@link XModel} with a specific actor (represented by its {@link XID}) and
 * automatically checks the access rights for this actor on the {@link XModel},
 * if a method is called and only executes the method, if the actor is allowed
 * to execute it (otherwise {@link AccessException XAccessExceptions} will be
 * thrown).
 * 
 * All change operations like adding new {@link XField XFields} executed on an
 * XProtectedModel will directly affect the wrapped {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedModel extends XLoggedModel, XProtectedSynchronizesChanges {
	
	/**
	 * Returns the {@link XObject} contained in this model with the given
	 * {@link XID} wrapped as an {@link XProtectedObject} linked with the actor
	 * of this XProtectedModel.
	 * 
	 * @param id The {@link XID} of the {@link XObject} which is to be returned
	 * @return The {@link XObject} with the given {@link XID} or null, if no
	 *         corresponding {@link XObject} exists
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	@ReadOperation
	XProtectedObject getObject(XID objectId);
	
	/**
	 * Creates a new {@link XObject} with the given {@link XID} and adds it to
	 * this XProtecedModel or returns the already existing {@link XObject} if
	 * the given {@link XID} was already taken.
	 * 
	 * @param id The {@link XID} for the {@link XObject} which is to be created
	 * @return the newly created {@link XObject} or the already existing
	 *         {@link XObject} if the given {@link XID} was already taken as an
	 *         {@link XProtectedObject} linked with the actor of this
	 *         XProtectedModel
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	XProtectedObject createObject(XID id);
	
	/**
	 * Removes the {@link XObject} with the given {@link XID} from this
	 * XProtectedModel. s
	 * 
	 * @param object The {@link XID} of the {@link XObject} which is to be
	 *            removed
	 * @return true, if an {@link XObject} with the given {@link XID} did exist
	 *         in this XProtectedModel and could be removed
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (writes access) to execute
	 *             this method
	 */
	@ModificationOperation
	boolean removeObject(XID objectID);
	
	/**
	 * Executes the given {@link XModelCommand} if possible.
	 * 
	 * This method will fail if, the given {@link XModelCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XModelCommand}: the specified {@link XObject} does
	 * not exist and therefore cannot be removed
	 * <li>Add-type {@link XModelCommand}: the given {@link XID} is already
	 * taken and therefore a new {@link XObject} with this {@link XID} cannot be
	 * created
	 * <li>the model-{@link XID} in the {@link XModelCommand} does not concur
	 * with the {@link XID} of this XModel
	 * </ul>
	 * 
	 * @param command The {@link XModelCommand} which is to be executed
	 * @return {@link XCommand#FAILED} if executing the {@link XModelCommand}
	 *         failed, {@link XCommand#NOCHANGE} if executing the
	 *         {@link XModelCommand} didn't change anything or if executing the
	 *         {@link XModelCommand} succeeded the revision number of the
	 *         {@link XModelEvent} caused by the {@link XModelCommand}.
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	long executeModelCommand(XModelCommand command);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	long getRevisionNumber();
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean hasObject(XID objectID);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean isEmpty();
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	Iterator<XID> iterator();
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForModelEvents(XModelEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForObjectEvents(XObjectEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForFieldEvents(XFieldEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read access) to execute
	 *             this method
	 */
	boolean addListenerForTransactionEvents(XTransactionEventListener changeListener);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read or write access,
	 *             depending on the {@link XCommand XCommands} in the
	 *             {@link XTransaction}) to execute this method
	 */
	long executeTransaction(XTransaction transaction);
	
	/**
	 * @throws AccessException if the actor linked with this field does not
	 *             have the necessary access rights (read or write access,
	 *             depending on the {@link XCommand}) to execute this method
	 */
	long executeCommand(XCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getActor();
	
}
