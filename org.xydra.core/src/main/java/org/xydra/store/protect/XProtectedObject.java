package org.xydra.store.protect;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.core.AccessException;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XObject;


/**
 * An XProtectedObject is a wrapper (Decorator) for an {@link XObject} which
 * links the {@link XObject} with a specific actor (represented by its
 * {@link XId}) and automatically checks the access rights for this actor on the
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
public interface XProtectedObject extends XLoggedObject, XExecutesCommands {

	/**
	 * Creates a new {@link XField} and adds it to this XProtectedObject or
	 * returns the already existing {@link XField} if the given {@link XId} was
	 * already taken (both as an {@link XProtectedField} linked with the actor
	 * of this XProtectedObject)
	 *
	 * @param fieldId The {@link XId} for the {@link XField} which is to be
	 *            created.
	 * @return the newly created {@link XField} or the already existing
	 *         {@link XField} with this {@link XId} (both as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject)
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@Override
    @ModificationOperation
	XProtectedField createField(XId fieldId);

	/**
	 * Executes the given {@link XObjectCommand} if possible.
	 *
	 * This method will fail if, the given {@link XObjectCommand} cannot be
	 * executed which may occur in the following cases:
	 * <ul>
	 * <li>Remove-type {@link XObjectCommand}: the specified {@link XField} does
	 * not exist and therefore cannot be removed
	 * <li>Add-type {@link XObjectCommand}: the given {@link XId} is already
	 * taken and therefore a new {@link XField} with this {@link XId} cannot be
	 * created
	 * <li>the object-{@link XId} in the {@link XObjectCommand} does not concur
	 * with the {@link XId} of this XObject
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
	XId getActor();

	/**
	 * Returns the {@link XField} with the given {@link XId} contained in this
	 * XProtectedObject as an {@link XProtectedField} linked with the actor of
	 * this XProtectedObject.
	 *
	 * @param fieldId The {@link XId} of the {@link XField} which is to be
	 *            returned
	 * @return The {@link XField} with the given {@link XId} as an
	 *         {@link XProtectedField} linked with the actor of this
	 *         XProtectedObject or null, if no corresponding {@link XField}
	 *         exists
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (read access) to execute this
	 *             method
	 */
	@Override
    @ReadOperation
	XProtectedField getField(XId fieldId);

}
