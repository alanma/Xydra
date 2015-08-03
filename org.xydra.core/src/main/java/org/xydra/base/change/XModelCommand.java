package org.xydra.base.change;

import org.xydra.base.XId;


/**
 * An {@link XCommand} for adding/removing objects to/from the specified model
 *
 */
public interface XModelCommand extends XAtomicCommand {

    /**
     * @return The {@link XId} of the model this command refers to
     */
    XId getModelId();

    /**
     * @return The {@link XId} of the object this command will remove/add if it
     *         can be successfully executed
     */
    XId getObjectId();

    /**
     * @return the {@link XId} of the Parent-repository of the model this
     *         command refers to (may be null)
     */
    XId getRepositoryId();

    /**
     * @return the current revision number of the object which will be
     *         added/removed
     */
    @Override
    long getRevisionNumber();

    /**
     * A forced add will succeed even if an object with the specified
     * {@link XId} already exists, while a safe add will only succeed if no such
     * object exists.
     *
     * A forced remove will succeed whether an object with the specified
     * {@link XId} exists or not, while a safe remove will only succeed if such
     * an object exists.
     *
     * Furthermore forced commands will ignore the current revision number of
     * the specified model while safe commands can only be executed if their
     * revision number fits to the current revision number.
     *
     * @return true, if this event is forced.
     */
    @Override
    boolean isForced();

}
