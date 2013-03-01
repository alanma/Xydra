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
 * There are general two types of XCommands. Forced commands and normal (safe)
 * commands. The only difference is, that forced XCommands are always succeeding
 * (they do no pre-checks), whereas safe ones fail, if they cannot be executed.
 * Suppose you want to add an XModel to an XRepository, but your chosen XId is
 * already taken, executing a safe XCommand will fail and return an error code,
 * whereas the same XCommand with isForced set to true will still "succeed", but
 * actually change nothing. So forced XCommands are used if you do not care if
 * the actions described in the XCommand are actually executed by this command
 * and only care if they were executed at some point in time, for example you
 * might not interested whether the XModel you wanted to add already existed or
 * if you actually added it, you are only interested in the fact that it exists
 * after you've sent your XCommand for execution, i.e. you're only interested in
 * the post-condition.
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
     * Returned when executing commands to indicate that the command failed.
     */
    static final long FAILED = -1;
    
    /**
     * A special revision that is returned for commands that should be executed
     * no matter what the current revision of the entity that is to be changed
     * is.
     * 
     * Commands with this revision number were (most likely) created by a
     * createForced...Command method of {@XCommandFactory}. We
     * strongly advise to NOT use this revision number in manually created
     * commands!
     */
    static final long FORCED = -1;
    
    /**
     * The returned revision of new models, objects and fields, before they are
     * assigned a proper revision number (for example during a transaction).
     * 
     * New models always have revision number 0 when created.
     */
    static final long NEW = 0;
    
    /**
     * Returned when executing commands to indicate that the command succeeded
     * but nothing actually changed.
     */
    static final long NOCHANGE = -2;
    
    /**
     * The returned revision of ADD-type XCommands for XModels and XObjects
     * (since these commands do not necessarily refer to a specific revision of
     * the targeted entity). Only used in XCommands as a marker that the
     * XCommand is a safe command and not a forced command and never used as the
     * revision number of any entity or event.
     * 
     * Commands with this revision number were (most likely) created by a
     * createSafe...Command method of {@XCommandFactory}. We
     * strongly advise NOT use this revision number in manually created
     * commands!
     */
    static final long SAFE = -2;
    
    /**
     * A threshold indicating that the revision number should be interpreted as
     * relative to another command in a batch of commands.
     */
    static final long RELATIVE_REV = Long.MAX_VALUE / 2;
    
    // TODO make revision numbers in commands stricter & document better
    static final long NONEXISTANT = SAFE;
    
    /**
     * WHAT will be changed?
     * 
     * @return the {@link XAddress} of the {@link XModel}, {@link XObject} or
     *         {@link XField} that will be added or removed -- or the
     *         {@link XField} which value was changed; same as getTarget() for
     *         transactions
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
