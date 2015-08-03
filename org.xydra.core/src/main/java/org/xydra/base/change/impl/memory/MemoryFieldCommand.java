package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XFieldCommand}
 */

public class MemoryFieldCommand extends MemoryAtomicCommand implements XFieldCommand {

    private static final long serialVersionUID = -1637754092944391876L;

    /** For GWT serialisation only - Do not use. */
    public MemoryFieldCommand() {
    }

    /**
     * Creates a new {@link XFieldCommand} of the add-type. Will add the given
     * {@link XValue} to the specified field, if possible.
     *
     * @param target The {@link XAddress} of the field which value is to be set.
     * @param fieldRevision The current revision number of the field which
     *            {@link XValue} is to be set. The field {@link XId} of the
     *            given address must not be null
     * @param newValue The new {@link XValue} for the specified field - must not
     *            be null
     * @return Creates a new {@link XFieldCommand} of the add-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field or if the given {@link XValue} is null
     */
    public static XFieldCommand createAddCommand(final XAddress target, final long fieldRevision,
            final XValue newValue) {

        if(newValue == null) {
			throw new IllegalArgumentException("an ADD command must have a non-null value");
		}

        return new MemoryFieldCommand(target, ChangeType.ADD, fieldRevision, newValue);
    }

    /**
     * Creates a new {@link XFieldCommand} of the change-type. Will change the
     * current {@link XValue} of the specified field to the given {@link XValue}
     * , if possible.
     *
     * @param target The {@link XAddress} of the field which value is to be set.
     * @param fieldRevision The current revision number of the field which value
     *            is to be set. The field {@link XId} of the given address must
     *            not be null
     * @param newValue The new {@link XValue} for the specified field - must not
     *            be null
     * @return Creates a new {@link XFieldCommand} of the change-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field
     * @throws IllegalArgumentException if newValue is null
     */
    public static XFieldCommand createChangeCommand(final XAddress target, final long fieldRevision,
            final XValue newValue) {

        if(newValue == null) {
			throw new IllegalArgumentException("a CHANGE command must have a non-null value");
		}

        return new MemoryFieldCommand(target, ChangeType.CHANGE, fieldRevision, newValue);
    }

    /**
     * Creates a new {@link XFieldCommand} of the remove-type. Will remove the
     * current {@link XValue} from the specified field, if possible.
     *
     * @param target The {@link XAddress} of the field which value is to be set.
     * @param fieldRevision The current revision number of the field which
     *            {@link XValue} is to be set. The field {@link XId} of the
     *            given address must not be null
     * @return Creates a new {@link XFieldCommand} of the remove-type for the
     *         specified target.
     * @throws IllegalArgumentException if the given {@link XAddress} does not
     *             specify an field
     */
    public static XFieldCommand createRemoveCommand(final XAddress target, final long fieldRevision) {
        return new MemoryFieldCommand(target, ChangeType.REMOVE, fieldRevision, null);
    }

    private XValue newValue;

    private MemoryFieldCommand(final XAddress target, final ChangeType changeType, final long fieldRevision,
            final XValue newValue) {
        super(target, changeType, fieldRevision);

        if(target.getField() == null) {
			throw new IllegalArgumentException(
                    "targets of XFieldCommands must specify a field, was:" + target);
		}

        this.newValue = newValue;
    }

    @Override
    public boolean equals(final Object object) {

        if(!super.equals(object)) {
			return false;
		}

        if(!(object instanceof XFieldCommand)) {
			return false;
		}
        final XFieldCommand command = (XFieldCommand)object;

        if(!XI.equals(this.newValue, command.getValue())) {
			return false;
		}

        return true;
    }

    @Override
    public XAddress getChangedEntity() {
        return getTarget();
    }

    @Override
    public XValue getValue() {
        return this.newValue;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        // newValue
        result ^= this.newValue == null ? 0 : this.newValue.hashCode();

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
        sb.append("     FieldCommand");
        addChangeTypeTarget(sb);
        // add what is added/removed/changed
        sb.append(" ->*" + getValue() + "*");
        addIntentRev(sb);
        return sb.toString();
    }
}
