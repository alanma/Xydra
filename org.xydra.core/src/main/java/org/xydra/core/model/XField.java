package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.value.XValue;


/**
 * An {@link XField} is the last part of the Xydra hierarchy. It may store some
 * kind of {@link XValue} and be the child of an {@link XObject}.
 * 
 * It is mostly used to model the actual attributes of something, for example
 * each person in a telephone example may be modelled as an {@link XObject} and
 * her name, address, age etc. could be stored in the {@link XField XFields} of
 * this {@link XObject}.
 * 
 * Implementations of XField should use an {@link XFieldState} for storing and
 * representing the inner state of the XField to allow maximum persistence
 * management flexibility.
 * 
 * @author voelkel
 * @author kaidel
 * 
 */
public interface XField extends XLoggedField, Serializable {
	
	/**
	 * Sets the {@link XValue} of this field to the given value.
	 * 
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 * 
	 * @param actor The {@link XID} of the actor of this operation
	 * @param value The new {@link XValue}
	 * @return true, if this operation actually changed the current
	 *         {@link XValue} of this field, false otherwise
	 */
	@ModificationOperation
	boolean setValue(XID actor, XValue value);
	
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
	 */
	@ModificationOperation
	long executeFieldCommand(XID actor, XFieldCommand command);
	
}
