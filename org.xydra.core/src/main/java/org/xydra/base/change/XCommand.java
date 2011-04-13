package org.xydra.base.change;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * XCommands are objects, that represent future change operations on entities,
 * for example on an {@link XModel}. They can be used to add new entities,
 * remove entities, execute transactions, change values etc.
 * 
 * There are two types of XCommands. Forced commands and normal commands. he
 * only difference is, that forced XCommands are always succeeding, whereas
 * unforced ones fail, if they cannot be executed. Suppose you want to add an
 * XModel to an XRepository, but your chosen XID is already taken, executing an
 * unforced XCommand will fail and return an error code, whereas the same
 * XCommand with isForced set to true will still "succeed", but actually change
 * nothing. So forced XCommands are used if you do not care if the actions
 * described in the XCommand are actually executed by this command and only care
 * if they were executed at some point in time, for example you might not
 * interested wether the XModel you wanted to add already existed or if you
 * actually added it, your only interested in the fact that it exists after
 * you've sent your XCommand for execution, i.e. you're only interested in the
 * post-condition.
 * 
 * See the more specific types (for example {@link XModelEvent} for further
 * explanations)
 */
public interface XCommand extends Serializable {
	
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
