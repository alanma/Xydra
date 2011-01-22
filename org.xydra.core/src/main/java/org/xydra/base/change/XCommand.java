package org.xydra.base.change;

import org.xydra.base.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * XCommands are objects, that represent future change operations on entities,
 * for example on an {@link XModel}. They can be used to add new entities,
 * remove entities, execute transactions, change values etc.
 * 
 * See the more specific types (for example {@link XModelEvent} for further
 * explanations)
 */
public interface XCommand {
	
	/**
	 * Returned when executing commands to indicate that the command failed.
	 */
	static final long FAILED = -1;
	
	/**
	 * special revision for commands that should be executed no matter what the
	 * current revision is
	 */
	static final long FORCED = -1;
	
	/**
	 * revision of new models, objects and fields, before they are assigned a
	 * proper revision number (for example during a transaction).
	 * 
	 * TODO use a different constant? Max: Better yes. 0 = not set/bug. 1...n =
	 * proper revNr. < 0 = special state
	 */
	static final long NEW = 0;
	
	/**
	 * Returned when executing commands to indicate that the command succeeded
	 * but nothing actually changed.
	 */
	static final long NOCHANGE = -2;
	
	/**
	 * only for ADD events (except XFieldCommand), others should use a specific
	 * revision
	 */
	static final long SAFE = -2;
	
	/**
	 * WHAT will be changed?
	 * 
	 * @return the {@link XAddress} of the {@link XModel}, {@link XObject} or
	 *         {@link XField} that will be added, removed or the {@link XField}
	 *         which value was changed; same as getTarget() for transactions
	 */
	XAddress getChangedEntity();
	
	/**
	 * @return the {@link ChangeType} of this command.
	 */
	ChangeType getChangeType();
	
	/**
	 * Returns the {@link XAddress} of the entity that will be changed by this
	 * command.
	 * 
	 * For repository, model and object commands this does NOT include the
	 * model, object or field being added or removed.
	 * 
	 * @return the {@link XAddress} of the entity that is to be changed
	 */
	XAddress getTarget();
	
}
