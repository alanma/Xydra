package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XObjectCommand;



/**
 * An {@link XObject} is an extensible object. At runtime, {@link XField}s can
 * be added, removed and changed.
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public interface XObject extends XLoggedObject, XExecutesTransactions, XExecutesCommands {
	
	/**
	 * Returns the field corresponding to the given XID in this object.
	 * 
	 * @param fieldID The XID of the wanted {@link XField}
	 * @return The {@link XField} with the given XID or null, if no
	 *         corresponding {@link XField} exists
	 */
	@ReadOperation
	XField getField(XID fieldId);
	
	/**
	 * Creates a new XField and adds it to this XObject
	 * 
	 * @param actor The XID of the actor.
	 * @param fieldID The XID of the XField to be created.
	 * @return the created field or the already existing field with this XID
	 */
	@ModificationOperation
	XField createField(XID actor, XID fieldID);
	
	/**
	 * Removes the XField from this XObject
	 * 
	 * @param actor The XID of the actor
	 * @param fieldID The XID of the XField which is to be removed
	 * @return true, if the given field did exist in the object and could be
	 *         removed
	 */
	@ModificationOperation
	boolean removeField(XID actor, XID fieldID);
	
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
	 * @param actor The XID of the actor
	 * @param command The command to be executed
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeObjectCommand(XID actor, XObjectCommand command);
	
}
