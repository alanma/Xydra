package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XCommand} for adding/removing models to/from the specified
 * repository
 */
public interface XRepositoryCommand extends XAtomicCommand {
    
    /**
     * @return the {@link XId} of the model this command will add/remove
     */
    XId getModelId();
    
    /**
     * @return the {@link XId} of the repository this command refers to
     */
    XId getRepositoryId();
    
    /**
     * @return the current revision number of the model which will be
     *         added/removed
     */
    @Override
    long getRevisionNumber();
    
    /**
     * A forced add will succeed even if an model with the specified {@link XId}
     * already exists, while a safe add will only succeed if no such model
     * exists.
     * 
     * A forced remove will succeed whether an model with the specified
     * {@link XId} exists or not, while a safe remove will only succeed if such
     * an model exists.
     * 
     * Furthermore forced commands will ignore the current revision number of
     * the specified model while safe commands can only be executed if their
     * revision number fits to the current revision number.
     * 
     * @return true, if this event is forced.
     */
    @Override
    boolean isForced();
    
    /**
     * TODO can this return {@link ChangeType#CHANGE} ?
     * 
     * @see org.xydra.base.change.XCommand#getChangeType()
     */
    @Override
    ChangeType getChangeType();
    
}
