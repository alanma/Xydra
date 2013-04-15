package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XCommand} for adding/removing fields to/from the specified object
 */
public interface XObjectCommand extends XAtomicCommand {
    
    /**
     * @return the {@link XId} of the field this command will add/remove
     */
    XId getFieldId();
    
    /**
     * @return the {@link XId} of the Parent-model of the object this command
     *         refers to (may be null)
     */
    XId getModelId();
    
    /**
     * @return the {@link XId} of the object this command refers to
     */
    XId getObjectId();
    
    /**
     * @return the {@link XId} of the Parent-repository of the object this
     *         command refers to (may be null)
     */
    XId getRepositoryId();
    
    /**
     * @return the current revision number of the field which will be
     *         added/removed
     */
    @Override
    long getRevisionNumber();
    
    /**
     * A forced add will succeed even if an field with the specified {@link XId}
     * already exists, while a safe add will only succeed if no such field
     * exists.
     * 
     * A forced remove will succeed whether an field with the specified
     * {@link XId} exists or not, while a safe remove will only succeed if such
     * an field exists.
     * 
     * Furthermore forced commands will ignore the current revision number of
     * the specified object while safe commands can only be executed if their
     * revision number fits to the current revision number.
     * 
     * @return true, if this event is forced.
     */
    @Override
    boolean isForced();
    
}
