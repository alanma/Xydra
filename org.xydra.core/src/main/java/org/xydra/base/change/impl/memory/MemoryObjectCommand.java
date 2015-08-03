package org.xydra.base.change.impl.memory;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;


/**
 * An implementation of {@link XObjectCommand}
 *
 */
public class MemoryObjectCommand extends MemoryAtomicCommand implements XObjectCommand {

    private static final long serialVersionUID = 3817731036782868280L;

    /** For GWT serialisation only - Do not use. */
    public MemoryObjectCommand() {
    }

    /**
     * Creates a new {@link XObjectCommand} of the add-type. Will add a new
     * field with the specified {@link XId} to the object this event applies to,
     * if possible.
     *
     * @param target The target of this command - object {@link XId} must not be
     *            null, field {@link XId} has to be null
     * @param isForced determines whether this command will be a forced or a
     *            safe command.
     * @param fieldId The {@link XId} for the field which is to be added
     * @return A new {@link XObjectCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an field or if the given fieldId is null
     */
    public static XObjectCommand createAddCommand(final XAddress target, final boolean isForced, final XId fieldId) {
        if(isForced) {
            return createAddCommand(target, XCommand.FORCED, fieldId);
        } else {
            return createAddCommand(target, XCommand.SAFE_STATE_BOUND, fieldId);
        }
    }

    /**
     * For description see {@link MemoryModelCommand}.createAddCommand
     *
     * @param target
     * @param fieldRevision
     * @param fieldId
     *
     * @return A new {@link XObjectCommand} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an field or if the given fieldId is null
     */
    public static XObjectCommand createAddCommand(final XAddress target, final long fieldRevision, final XId fieldId) {

        if(fieldRevision != XCommand.FORCED && fieldRevision != XCommand.SAFE_STATE_BOUND
                && fieldRevision < 0) {
			throw new RuntimeException("invalid revision for an XObjectCommand of type ADD: "
                    + fieldRevision);
		}

        return new MemoryObjectCommand(target, ChangeType.ADD, fieldRevision, fieldId);
    }

    /**
     * Creates a new {@link XObjectCommand} of the remove-type. Will remove the
     * specified field from the object this event applies to, if possible.
     *
     * @param target The target of this command - object {@link XId} must not be
     *            null, field {@link XId} has to be null
     * @param fieldRevision The current revision number of the field which is to
     *            be removed
     * @param fieldId The {@link XId} of the field which is to be removed
     * @return A new {@link XObjectCommand} of the remove-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an field or if the given fieldId is null
     */
    public static XObjectCommand createRemoveCommand(final XAddress target, final long fieldRevision,
            final XId fieldId) {
        return new MemoryObjectCommand(target, ChangeType.REMOVE, fieldRevision, fieldId);
    }

    /** Id of the field being added or removed */
    private XId fieldId;

    private MemoryObjectCommand(final XAddress target, final ChangeType changeType, final long fieldRevision,
            final XId fieldId) {
        super(target, changeType, fieldRevision);

        if(target.getObject() == null) {
			throw new IllegalArgumentException("target must specify an object, was:" + target);
		}

        if(target.getField() != null) {
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		}

        if(fieldId == null) {
			throw new IllegalArgumentException("the field id must not be null");
		}

        this.fieldId = fieldId;

    }

    @Override
    public boolean equals(final Object object) {

        if(!super.equals(object)) {
			return false;
		}

        if(!(object instanceof XObjectCommand)) {
			return false;
		}
        final XObjectCommand command = (XObjectCommand)object;

        if(!this.fieldId.equals(command.getFieldId())) {
			return false;
		}

        return true;
    }

    @Override
    public XAddress getChangedEntity() {
        return Base.resolveField(getTarget(), getFieldId());
    }

    @Override
    public XId getFieldId() {
        return this.fieldId;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        // newValue
        result ^= this.fieldId.hashCode();

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
        sb.append("    ObjectCommand");
        addChangeTypeTarget(sb);
        // add what is added/removed/changed
        sb.append(" '" + getFieldId() + "'");
        addIntentRev(sb);
        return sb.toString();
    }

}
