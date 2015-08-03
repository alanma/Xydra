package org.xydra.base.change.impl.memory;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;


/**
 * An implementation of {@link XRepositoryCommand}.
 *
 */

public class MemoryRepositoryCommand extends MemoryAtomicCommand implements XRepositoryCommand {

    private static final long serialVersionUID = -6723215151804666417L;

    /** For GWT serialisation only - Do not use. */
    public MemoryRepositoryCommand() {
    }

    /**
     * Creates a new {@link XRepositoryCommand} of the add-type. Will add a new
     * model with the specified {@link XId} to the repository this event applies
     * to, if possible.
     *
     * @param target The {@link XAddress} of the repository this command applies
     *            to - repository {@link XId} must not be null, model, object &
     *            field {@link XId} must be null
     * @param isForced determines whether this command will be a forced or a
     *            safe command.
     * @param modelId The {@link XId} for the model which is to be added
     * @return A new {@link XRepositoryCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             refer to an repository or if the given modelId is null
     */
    public static XRepositoryCommand createAddCommand(final XAddress target, final boolean isForced, final XId modelId) {
        if(isForced) {
            return createAddCommand(target, XCommand.FORCED, modelId);
        } else {
            return createAddCommand(target, XCommand.SAFE_STATE_BOUND, modelId);
        }
    }

    /**
     * For description see {@link MemoryModelCommand createAddCommand}
     *
     * @param target
     * @param modelRevision
     * @param modelId
     * @return A new {@link XRepositoryCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             refer to an repository or if the given modelId is null
     */
    public static XRepositoryCommand createAddCommand(final XAddress target, final long modelRevision,
            final XId modelId) {

        if(modelRevision != XCommand.FORCED && modelRevision != XCommand.SAFE_STATE_BOUND
                && modelRevision < RevisionConstants.NOT_EXISTING) {
			throw new RuntimeException("invalid revision for an XObjectCommand of type ADD: "
                    + modelRevision);
		}

        return new MemoryRepositoryCommand(target, ChangeType.ADD, modelRevision, modelId);
    }

    /**
     * Creates a new {@link XRepositoryCommand} of the remove-type. Will remove
     * the model with the specified {@link XId} from the repository this event
     * applies to, if possible.
     *
     * @param target The {@link XAddress} of the repository this command applies
     *            to - repository {@link XId} must not be null, model, object &
     *            field {@link XId} must be null
     * @param modelRevision Can be {@link XCommand#FORCED},
     *            {@link XCommand#SAFE_STATE_BOUND} or positive
     * @param modelId The {@link XId} of the model which is to be removed
     * @return A new {@link XRepositoryCommand} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             refer to an repository or if the given modelId is null
     */
    public static XRepositoryCommand createRemoveCommand(final XAddress target, final long modelRevision,
            final XId modelId) {
        return new MemoryRepositoryCommand(target, ChangeType.REMOVE, modelRevision, modelId);
    }

    /** Id of the model to be added or removed */
    private XId modelId;

    private MemoryRepositoryCommand(final XAddress target, final ChangeType changeType, final long modelRevision,
            final XId modelId) {
        super(target, changeType, modelRevision);

        if(target.getRepository() == null) {
			throw new IllegalArgumentException("target must specify a repository, was:" + target);
		}

        if(target.getModel() != null) {
			throw new IllegalArgumentException("target must not specify a model, was:" + target);
		}

        if(target.getObject() != null) {
			throw new IllegalArgumentException("target must not specify an object, was:" + target);
		}

        if(target.getField() != null) {
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		}

        if(modelId == null) {
			throw new IllegalArgumentException("the model id must not be null");
		}

        this.modelId = modelId;
    }

    @Override
    public boolean equals(final Object object) {

        if(!super.equals(object)) {
			return false;
		}

        if(!(object instanceof XRepositoryCommand)) {
			return false;
		}
        final XRepositoryCommand command = (XRepositoryCommand)object;

        if(!this.modelId.equals(command.getModelId())) {
			return false;
		}

        return true;
    }

    @Override
    public XAddress getChangedEntity() {
        return Base.resolveModel(getTarget(), getModelId());
    }

    @Override
    public XId getModelId() {
        return this.modelId;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        // newValue
        result ^= this.modelId.hashCode();

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
        sb.append(" RepositoryCommand");
        addChangeTypeTarget(sb);
        // add what is added/removed/changed
        sb.append(" '" + getModelId() + "'");
        addIntentRev(sb);
        return sb.toString();
    }

}
