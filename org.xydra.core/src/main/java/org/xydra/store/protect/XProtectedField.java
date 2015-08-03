package org.xydra.store.protect;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.value.XValue;
import org.xydra.core.AccessException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedField;


/**
 * An XProtectedField is a wrapper (Decorator) for an {@link XField} which links
 * the {@link XField} with a specific actor (represented by its {@link XId}) and
 * automatically checks the access rights for this actor on the {@link XField},
 * if a method is called and only executes the method, if the actor is allowed
 * to execute it (otherwise {@link AccessException XAccessExceptions} will be
 * thrown).
 *
 * All change operations like manipulating the stored {@link XValue} executed on
 * an XProtectedField will directly affect the wrapped {@link XField}.
 *
 * @author dscharrer
 *
 */
public interface XProtectedField extends XLoggedField {

	/**
	 * Executes the given {@link XCommand} if possible.
	 *
	 * This method will fail if,
	 * <ul>
	 * <li>the given {@link XCommand} cannot be executed
	 * <li>the field-{@link XId} specified in the {@link XCommand} does not
	 * concur with the {@link XId} of this field
	 * </ul>
	 *
	 * @param command The {@link XCommand} which is to be executed
	 * @return {@link XCommand#FAILED} if the {@link XCommand} failed,
	 *         {@link XCommand#NOCHANGE} if the {@link XCommand} didn't change
	 *         anything or the revision number of the {@link XEvent} caused by
	 *         the {@link XCommand}.
	 * @throws AccessException if the actor linked with this field does not have
	 *             the necessary access rights (write access) to execute this
	 *             method
	 */
	@ModificationOperation
	long executeFieldCommand(XFieldCommand command);

	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XId getActor();

}
