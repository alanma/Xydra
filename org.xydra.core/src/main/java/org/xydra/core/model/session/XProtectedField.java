package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLoggedField;
import org.xydra.core.value.XValue;



/**
 * A wrapper for an {@link XField} for a specific actor.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedField extends XLoggedField {
	
	/**
	 * Set this field to the new value.
	 * 
	 * @param value
	 * @return true if the value changed
	 */
	@ModificationOperation
	boolean setValue(XValue value);
	
	/**
	 * Executes the given command if possible.
	 * 
	 * This method will fail if,
	 * <ul>
	 * <li>the given command cannot be executed
	 * <li>the field-XID in the command does not concur with the XID of this
	 * field
	 * </ul>
	 * 
	 * @param command The command to be executed
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeFieldCommand(XFieldCommand command);
	
}
