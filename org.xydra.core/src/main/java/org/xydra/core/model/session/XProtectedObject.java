package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedObject;
import org.xydra.core.model.XObject;


/**
 * A wrapper for an {@link XObject} for a specific actor.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedObject extends XLoggedObject, XProtectedSynchronizesChanges {
	
	/**
	 * Returns the field corresponding to the given XID in this object.
	 * 
	 * @param fieldID The XID of the wanted {@link XProtectedField}
	 * @return The {@link XProtectedField} with the given XID or null, if no
	 *         corresponding {@link XProtectedField} exists
	 */
	@ReadOperation
	XProtectedField getField(XID fieldId);
	
	/**
	 * Creates a new field and adds it to this object
	 * 
	 * @param fieldID The XID of the field to be created.
	 * @return the created field or the already existing field with this XID
	 */
	@ModificationOperation
	XProtectedField createField(XID fieldID);
	
	/**
	 * Removes the field from this object
	 * 
	 * @param fieldID The XID of the field which is to be removed
	 * @return true, if the given field did exist in the object and could be
	 *         removed
	 */
	@ModificationOperation
	boolean removeField(XID fieldID);
	
	/**
	 * Executes the given command if possible.
	 * 
	 * This method will fail if,
	 * <ul>
	 * <li>the given command cannot be executed (i.e. the specified object does
	 * not exist, and therefore cannot be removed [delete], or the given XID is
	 * already taken and therefore a new model with this XID cannot be created
	 * [add])
	 * <li>the model-XID in the command does not concur with the XID of this
	 * model
	 * </ul>
	 * 
	 * @param command The command to be executed
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeObjectCommand(XObjectCommand command);
	
}
