package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;


/**
 * An {@link XField} is the last part of the Xydra hierarchy. It may store some
 * kind of {@link XValue} and be the child of an {@link XObject}.
 * 
 * It is mostly used to model the actual attributes of something, for example
 * each person in a telephone example may be modelled as an {@link XObject} and
 * her name, address, age etc. could be stored in the {@link XField XFields} of
 * this {@link XObject}.
 * 
 * Implementations of XField should use an {@link XRevWritableField} for storing
 * and representing the inner state of the XField to allow maximum persistence
 * management flexibility.
 * 
 * @author voelkel
 * @author kaidel
 * 
 */
public interface XField extends XLoggedField, XWritableField, Serializable {
	
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
	 * 
	 * @return {@link XCommand#FAILED} if the {@link XCommand} failed,
	 *         {@link XCommand#NOCHANGE} if the {@link XCommand} didn't change
	 *         anything or the revision number of the {@link XEvent} caused by
	 *         the {@link XCommand}.
	 */
	@ModificationOperation
	long executeFieldCommand(XFieldCommand command);
	
	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XId getSessionActor();
	
	/**
	 * Set a new actor to be used when building commands for changes to this
	 * field. This method cannot be used if this field has a parent.
	 * 
	 * @param actor for this field.
	 */
	void setSessionActor(XId actor);
	
	/**
	 * Sets the {@link XValue} of this field to the given value.
	 * 
	 * Passing "null" as the 'value' arguments implies an remove operation (will
	 * remove the current {@link XValue})
	 * 
	 * @param value The new {@link XValue}
	 * 
	 * @return true, if this operation actually changed the current
	 *         {@link XValue} of this field, false otherwise
	 */
	@Override
    @ModificationOperation
	boolean setValue(XValue value);
	
	/**
	 * Create a consistent snapshot of this field.
	 * 
	 * @return null if this field has been removed, a consistent snapshot
	 *         otherwise.
	 */
	XRevWritableField createSnapshot();
	
}
