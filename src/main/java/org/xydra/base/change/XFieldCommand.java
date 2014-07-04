package org.xydra.base.change;

import org.xydra.base.XId;
import org.xydra.base.value.XValue;


/**
 * An {@link XCommand} for adding, removing or changing the {@link XValue} of
 * the specified field
 * 
 */

public interface XFieldCommand extends XAtomicCommand {
    
    /**
     * @return the {@link XId} of the field this command will add/remove
     */
    XId getFieldId();
    
    /**
     * @return the {@link XId} of the Parent-model of the field this command
     *         refers to (may be null)
     */
    XId getModelId();
    
    /**
     * @return the {@link XId} of the Parent-object of the field this command
     *         refers to (may be null)
     */
    XId getObjectId();
    
    /**
     * @return the {@link XId} of the Parent-repository of the field this
     *         command refers to (may be null)
     */
    XId getRepositoryId();
    
    /**
     * @return the current revision number of field which value will be changed
     *         by this command
     */
    @Override
    long getRevisionNumber();
    
    /**
     * @return the {@link XValue} set by this command. Always null for commands
     *         of type REMOVE.
     */
    XValue getValue();
    
    /**
     * A forced add will succeed even if the {@link XValue} of the specified
     * field is set, while a safe add will only succeed if the value isn't set.
     * 
     * A forced remove/change will succeed whether the {@link XValue} of the
     * specified field is set or not while a safe remove/change will only
     * succeed if the {@link XValue} is set.
     * 
     * Furthermore forced commands will ignore the current revision number of
     * the specified field while safe commands can only be executed if their
     * revision number fits to the current revision number.
     * 
     * @return true, if this event is forced.
     */
    @Override
    boolean isForced();
    
}
