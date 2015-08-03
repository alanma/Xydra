package org.xydra.base.change.impl.memory;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;


/**
 * An implementation of {@link XModelCommand}
 *
 */

public class MemoryModelCommand extends MemoryAtomicCommand implements XModelCommand {

    private static final long serialVersionUID = -8516270265119646260L;

    /** For GWT serialisation only - Do not use. */
    public MemoryModelCommand() {
    }

    /**
     * Creates a new {@link XModelCommand} of the add-type. Will add a new
     * object with the specified {@link XId} to the model this event applies to,
     * if possible.
     *
     * @param target The target of this command - the model {@link XId} must not
     *            be null, object & field {@link XId} must be null
     * @param isForced determines whether this command will be a forced or a
     *            safe command.
     * @param objectId The {@link XId} for the object which is to be added
     * @return A new {@link XModelCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an model or if the given objectId is null
     */
    public static XModelCommand createAddCommand(final XAddress target, final boolean isForced, final XId objectId) {
        if(isForced) {
            return createAddCommand(target, XCommand.FORCED, objectId);
        } else {
            return createAddCommand(target, XCommand.SAFE_STATE_BOUND, objectId);
        }
    }

    /**
     * Creates a new {@link XModelCommand} of the add-type. Will add a new
     * object with the specified {@link XId} to the model this event applies to,
     * if possible.
     *
     * @param target The target of this command - the model {@link XId} must not
     *            be null, object & field {@link XId} must be null
     * @param objectRevision Can be {@link XCommand#FORCED},
     *            {@link XCommand#SAFE_STATE_BOUND} or positive
     * @param objectId The {@link XId} for the object which is to be added
     * @return A new {@link XModelCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an model, if the given objectRevison is neither
     *             {@link XCommand#FORCED} or {@link XCommand#SAFE_STATE_BOUND}
     *             or if the given objectId is null
     */
    public static XModelCommand createAddCommand(final XAddress target, final long objectRevision, final XId objectId) {

        if(objectRevision != XCommand.FORCED && objectRevision != XCommand.SAFE_STATE_BOUND
                && objectRevision < 0) {
			throw new IllegalArgumentException(
                    "invalid revision for an XObjectCommand of type ADD: " + objectRevision);
		}

        return new MemoryModelCommand(target, ChangeType.ADD, objectRevision, objectId);
    }

    /**
     * Creates a new {@link XModelCommand} of the remove-type. Will remove the
     * specified object from the model this event applies to, if possible.
     *
     * @param target The target of this command - the model {@link XId} must not
     *            be null, object & field {@link XId} must be null
     * @param objectRevision The current revision number of the object which is
     *            to be removed
     * @param objectId The {@link XId} of the object which is to be removed
     * @return A new {@link XModelCommand} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an model or if the given objectId is null
     */
    public static XModelCommand createRemoveCommand(final XAddress target, final long objectRevision,
            final XId objectId) {
        return new MemoryModelCommand(target, ChangeType.REMOVE, objectRevision, objectId);
    }

    /** Id of the object being added or removed */
    private XId objectId;

    private MemoryModelCommand(final XAddress target, final ChangeType changeType, final long objectRevision,
            final XId objectId) {
        super(target, changeType, objectRevision);

        if(target.getModel() == null) {
			throw new IllegalArgumentException("target must specify a model, was:" + target);
		}

        if(target.getObject() != null) {
			throw new IllegalArgumentException("target must not specify an object, was:" + target);
		}

        if(target.getField() != null) {
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		}

        if(objectId == null) {
			throw new IllegalArgumentException("the object id must not be null");
		}

        this.objectId = objectId;

    }

    @Override
    public boolean equals(final Object object) {

        if(!super.equals(object)) {
			return false;
		}

        if(!(object instanceof XModelCommand)) {
			return false;
		}
        final XModelCommand command = (XModelCommand)object;

        if(!this.objectId.equals(command.getObjectId())) {
			return false;
		}

        return true;
    }

    @Override
    public XAddress getChangedEntity() {
        return Base.resolveObject(getTarget(), getObjectId());
    }

    @Override
    public XId getObjectId() {
        return this.objectId;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        // newValue
        result ^= this.objectId.hashCode();

        return result;
    }

    /**
     * Format: {MOF}Command
     *
     * {'ADD'|'REMOVE'}
     *
     * @{target *{id/value}, where xRef = '-' for
     *          {@link RevisionConstants#REVISION_OF_ENTITY_NOT_SET} and '?' for
     *          {@link RevisionConstants#REVISION_NOT_AVAILABLE}.
     *
     *          {'Forced'|'Safe(State)'|'Safe(' {rev} ')' }
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("     ModelCommand");
        addChangeTypeTarget(sb);
        // add what is added/removed/changed
        sb.append(" '" + getObjectId() + "'");
        addIntentRev(sb);
        return sb.toString();
    }

}
