package org.xydra.core.model.delta;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;


/**
 * An {@link XWritableField} that represents changes to an
 * {@link XReadableField}.
 *
 * An {@link XReadableField} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XReadableField}
 * and allow changes on its {@link XValue}. The changes do not happen directly
 * on the passed {@link XReadableField} but rather on a sort of copy that
 * emulates the passed {@link XReadableField}. A ChangedField provides methods
 * to compare the current state to the state the passed {@link XReadableField}
 * was in at creation time.
 *
 * @author dscharrer
 *
 */
public class ChangedField implements XWritableField, IFieldDiff {

    private final XReadableField base;
    boolean changed = false;
    private XValue value;

    /**
     * Wrap an {@link XReadableField} to record a set of changes made. Multiple
     * changes will be combined as much as possible such that a minimal set of
     * changes remains.
     *
     * Note that this is a very lightweight wrapper intended for a short
     * lifetime. As a consequence, the wrapped {@link XReadableField} is not
     * copied and changes to it (as opposed to this {@link ChangedField}) may
     * result in undefined behavior of the {@link ChangedField}.
     *
     * @param base The {@link XReadableField} this ChangedField will encapsulate
     *            and represent
     */
    public ChangedField(final XReadableField base) {
        this.value = base.getValue();
        this.base = base;
    }

    public boolean executeCommand(final XFieldCommand fieldCommand) {
        return ChangeExecutor.executeFieldCommand(fieldCommand, this);
    }

    @Override
    public XAddress getAddress() {
        return this.base.getAddress();
    }

    @Override
    public XId getId() {
        return this.base.getId();
    }

    @Override
    public XValue getInitialValue() {
        return getOldValue();
    }

    /**
     * @return The {@link XValue} the encapsulated {@link XReadableField} had at
     *         the creation time of this ChangedField.
     */
    public XValue getOldValue() {
        return this.base.getValue();
    }

    /**
     * Return the revision number of the wrapped {@link XReadableField}. The
     * revision number does not increase with changes to this
     * {@link ChangedField}.
     *
     * @return the revision number of the original {@link XReadableField}
     */
    @Override
    public long getRevisionNumber() {
        return this.base.getRevisionNumber();
    }

    @Override
    public XType getType() {
        return XType.XFIELD;
    }

    @Override
    public XValue getValue() {
        return this.value;
    }

    /**
     * @return true, if the current {@link XValue} of this ChangedField is
     *         different from the value of the underlying {@link XReadableField}
     */
    @Override
    public boolean isChanged() {
        return this.changed;
    }

    @Override
    public boolean isEmpty() {
        return this.value == null;
    }

    @Override
    public boolean setValue(final XValue value) {
        // reset changed flag if the value is reset to the base field's value
        final boolean changes = !XI.equals(getValue(), value);
        this.changed = !XI.equals(value, this.base.getValue());
        this.value = value;
        return changes;
    }

}
