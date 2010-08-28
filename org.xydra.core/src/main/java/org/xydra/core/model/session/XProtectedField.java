package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedField;
import org.xydra.core.value.XValue;


/**
 * An XProtectedField is a wrapper (Decorator) for an {@link XField} which links
 * the {@link XField} with a specific actor (represented by its {@link XID}) and
 * automatically checks the access rights for this actor on the {@link XField},
 * if a method is called and only executes the method, if the actor is allowed
 * to execute it (otherwise {@link XAccessException XAccessExceptions} will be
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
	 * Sets the {@link XValue} of this field to the given value.
	 * 
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 * 
	 * @param value The new {@link XValue}
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 * 
	 */
	@ModificationOperation
	boolean setValue(XValue value);
	
	/**
	 * Executes the given {@link XCommand} if possible.
	 * 
	 * This method will fail if,
	 * <ul>
	 * <li>the given {@link XCommand} cannot be executed
	 * <li>the field-{@link XID} specified in the {@link XCommand} does not
	 * concur with the {@link XID} of this field
	 * </ul>
	 * 
	 * @param command The {@link XCommand} which is to be executed
	 * @return {@link XCommand#FAILED} if the {@link XCommand} failed,
	 *         {@link XCommand#NOCHANGE} if the {@link XCommand} didn't change
	 *         anything or the revision number of the {@link XEvent} caused by
	 *         the {@link XCommand}.
	 * @throws XAccessException if the actor linked with this field does not
	 *             have the necessary access rights (write access) to execute
	 *             this method
	 */
	@ModificationOperation
	long executeFieldCommand(XFieldCommand command);
	
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
	XValue getValue();
	
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
	boolean addListenerForFieldEvents(XFieldEventListener changeListener);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getActor();
	
}
