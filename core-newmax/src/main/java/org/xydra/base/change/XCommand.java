package org.xydra.base.change;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.change.impl.memory.RevisionConstants;


/**
 * XCommands are objects, that represent future change operations on entities,
 * for example on an {@link org.xydra.core.model.XModel}. They can be used to
 * add new entities, remove entities, execute transactions, change values etc.
 * 
 * There are general two types of XCommands. Forced commands and normal (safe)
 * commands. The only difference is, that forced XCommands are always succeeding
 * (they do no pre-checks), whereas safe ones fail, if they cannot find the
 * right pre-conditions. Suppose you want to add an XModel to an XRepository,
 * but your chosen XId is already taken, executing a safe XCommand will fail and
 * return an error code, whereas the same XCommand with isForced set to true
 * will still "succeed", but actually change nothing. So forced XCommands are
 * used if you do not care if the actions described in the XCommand are actually
 * executed by this command and only care if they were executed at some point in
 * time, for example you might not interested whether the XModel you wanted to
 * add already existed or if you actually added it, you are only interested in
 * the fact that it exists after you've sent your XCommand for execution, i.e.
 * you're only interested in the post-condition.
 * 
 * Not every XCommand-Type comes in these two variations, but if this is
 * possible, the XCommand needs to specify a "isForced"-command returning a
 * boolean to tell whether it is forced or not. For example, see
 * {@link XModelCommand}.
 * 
 * See the more specific types (for example {@link XModelEvent} for further
 * explanations).
 */
public interface XCommand extends Serializable {
    
    /**
     * Command result code. Returned when executing commands to indicate that
     * the command failed.
     * 
     */
    static final long FAILED = RevisionConstants.COMMAND_FAILED;
    
    /**
     * Command request code. A special revision that is returned for commands
     * that should be executed no matter what the current revision of the entity
     * that is to be changed is.
     * 
     * Commands with this revision number were (most likely) created by a
     * createForced...Command method of {@XCommandFactory}. We
     * strongly advise to NOT use this revision number in manually created
     * commands!
     */
    static final long FORCED = RevisionConstants.COMMAND_INTENT_FORCED;
    
    /**
     * Command result code. The returned revision of new models, objects and
     * fields. New models always have revision number 0 when created.
     * 
     * During a txn, the revision number is
     * {@link RevisionConstants#REVISION_OF_ENTITY_NOT_SET}
     */
    static final long NEW = RevisionConstants.JUST_CREATED_ENTITY;
    
    /**
     * Command result code. Returned when executing commands to indicate that
     * the command succeeded but nothing actually changed.
     */
    static final long NOCHANGE = RevisionConstants.NOCHANGE;
    
    /**
     * Command request code. The returned revision of ADD-type XCommands for
     * XModels and XObjects (since these commands do not necessarily refer to a
     * specific revision of the targeted entity). Only used in XCommands as a
     * marker that the XCommand is a safe command and not a forced command and
     * never used as the revision number of any entity or event.
     * 
     * Commands with this revision number were (most likely) created by a
     * createSafe...Command method of {@XCommandFactory}. We
     * strongly advise NOT use this revision number in manually created
     * commands!
     */
    static final long SAFE_STATE_BOUND = RevisionConstants.COMMAND_INTENT_SAFE_STATE_BOUND;
    
    /**
     * A threshold indicating that the revision number should be interpreted as
     * relative to another command in a batch of commands.
     */
    static final long RELATIVE_REV = Long.MAX_VALUE / 2;
    
    /** The entity with this revision number does not exist yet */
    static final long NONEXISTANT = RevisionConstants.NOT_EXISTING;
    
    /**
     * WHAT will be changed?
     * 
     * @return the {@link XAddress} of the {@link org.xydra.core.model.XModel},
     *         {@link org.xydra.core.model.XObject} or
     *         {@link org.xydra.core.model.XField} that will be added or removed
     *         -- or the {@link org.xydra.core.model.XField} which value was
     *         changed; same as getTarget() for transactions
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
