package org.xydra.core.model.delta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;
import org.xydra.sharedutils.XyAssert;


/**
 * An {@link XWritableObject} that represents changes to an
 * {@link XReadableObject}.
 *
 * An {@link XReadableObject} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XReadableObject}
 * and allow changes on its set of {@link XReadableField XBaseFields}. The
 * changes do not happen directly on the passed {@link XReadableField} but
 * rather on a sort of copy that emulates the passed {@link XReadableObject}. A
 * ChangedObject provides methods to compare the current state to the state the
 * passed {@link XReadableObject} was in at creation time.
 *
 * @author dscharrer
 *
 */
public class ChangedObject implements XWritableObject, IObjectDiff, XExistsWritableObject {

    /**
     * Apply the changes represented by the changedObject to the given
     * baseObject.
     *
     * @param changedObject of which the changes are applied to the given
     *            baseObject
     * @param baseObject should be a writable version of the object used to
     *            create the changedObject
     */
    public static void commitTo(final ChangedObject changedObject, final XWritableObject baseObject) {
        XyAssert.xyAssert(changedObject != null);
        assert changedObject != null;
        XyAssert.xyAssert(baseObject != null);
        assert baseObject != null;
        XyAssert.xyAssert(changedObject.getId().equals(baseObject.getId()));
        for(final SimpleField field : changedObject.getNewFields()) {
            final XWritableField baseField = baseObject.createField(field.getId());
            baseField.setValue(field.getValue());
        }
        for(final ChangedField field : changedObject.getChangedFields()) {
            if(field.isChanged()) {
                final XWritableField baseField = baseObject.createField(field.getId());
                baseField.setValue(field.getValue());
            }
        }
        for(final XId removed : changedObject.getRemovedFields()) {
            baseObject.removeField(removed);
        }
    }

    // Fields that are not in base and have been added.
    // Contains no XIds that are in removed or changed.
    private final Map<XId,SimpleField> added = new HashMap<XId,SimpleField>(2);

    private final XReadableObject base;

    // Fields that are in base and have not been removed.
    // While they were changed once, those changes might have been reverted.
    // Contains no XIds that are in added or removed.
    private final Map<XId,ChangedField> changed = new HashMap<XId,ChangedField>(2);

    private boolean objectExists = true;

    // Fields that are in base but have been removed.
    // Contains no XIds that are in added or changed.
    private final Set<XId> removed = new HashSet<XId>(2);

    /**
     * Wrap an {@link XReadableObject} to record a set of changes made. Multiple
     * changes will be combined as much as possible such that a minimal set of
     * changes remains.
     *
     * Note that this is a very lightweight wrapper intended for a short
     * lifetime. As a consequence, the wrapped {@link XReadableObject} is not
     * copied and changes to it or any contained fields (as opposed to this
     * {@link ChangedObject}) may result in undefined behavior of the
     * {@link ChangedObject}.
     *
     * @param base The {@link XReadableObject} this ChangedObject will
     *            encapsulate and represent
     */
    public ChangedObject(final XReadableObject base) {
        XyAssert.xyAssert(base != null);
        assert base != null;
        this.base = base;
    }

    private boolean checkSetInvariants() {

        for(final XId id : this.removed) {
            XyAssert.xyAssert(!this.added.containsKey(id) && !this.changed.containsKey(id));
            XyAssert.xyAssert(this.base.hasField(id));
        }

        for(final XId id : this.added.keySet()) {
            XyAssert.xyAssert(!this.removed.contains(id) && !this.changed.containsKey(id));
            assert !this.base.hasField(id) : "base " + this.base.getAddress()
                    + " cannot have field " + id + " which also appers in ADDED";
            XyAssert.xyAssert(id.equals(this.added.get(id).getId()));
        }

        for(final XId id : this.changed.keySet()) {
            XyAssert.xyAssert(!this.removed.contains(id) && !this.added.containsKey(id));
            XyAssert.xyAssert(this.base.hasField(id));
            XyAssert.xyAssert(id.equals(this.changed.get(id).getId()));
        }

        return true;
    }

    /**
     * Remove all fields.
     */
    public void clear() {

        this.added.clear();
        this.changed.clear();
        for(final XId id : this.base) {
            // IMPROVE maybe add a "cleared" flag to remove all fields more
            // efficiently?
            this.removed.add(id);
        }

        XyAssert.xyAssert(checkSetInvariants());
    }

    /**
     * Count the minimal number of {@link XCommand XCommands} that would be
     * needed to transform the original {@link XReadableObject} to the current
     * state which is represented by this ChangedObject.
     *
     * @param max An upper bound for counting the amount of needed
     *            {@link XCommand XCommands}. Note that setting this bound to
     *            little may result in the return of an integer which does not
     *            actually represent the minimal amount of needed
     *            {@link XCommand XCommands} for the transformation.
     * @return the amount of needed {@link XCommand XCommands} for the
     *         transformation
     */
    public int countCommandsNeeded(final int max) {
        int n = this.removed.size() + this.added.size();
        if(n < max) {
            for(final XReadableField field : this.added.values()) {
                if(!field.isEmpty()) {
                    n++;
                    if(n >= max) {
                        return n;
                    }
                }
            }
            for(final ChangedField field : this.changed.values()) {
                n += field.isChanged() ? 1 : 0;
                if(n >= max) {
                    return n;
                }
            }
            for(final XId fieldId : this.removed) {
                final XReadableField oldField = getOldField(fieldId);
                if(oldField.getValue() != null) {
                    n++;
                    if(n >= max) {
                        return n;
                    }
                }
            }
        }
        return n;
    }

    /**
     * Count the number of {@link XEvent XEvents} that would be needed to log
     * the transformation of the original {@link XReadableModel} to the current
     * state which is represented by this ChangedModel.
     *
     * This is different to {@link #countCommandsNeeded} in that a removed
     * object or field may cause several events while only needing one command.
     *
     * @param max An upper bound for counting the amount of needed
     *            {@link XEvent XEvents}. Note that setting this bound to little
     *            may result in the return of an integer which does not actually
     *            represent the minimal amount of needed {@link XEvent XEvents}
     *            for the transformation.
     * @return the amount of needed {@link XEvent XEvents} for the
     *         transformation
     */
    public int countEventsNeeded(final int max) {
        int n = this.removed.size() + this.added.size();
        if(n < max) {
            for(final XId fieldId : this.removed) {
                // removing field itself already counted
                final XReadableField oldField = getOldField(fieldId);
                if(!oldField.isEmpty()) {
                    n++; // removing the value
                    if(n >= max) {
                        return n;
                    }
                }
            }
            for(final XReadableField field : this.added.values()) {
                if(!field.isEmpty()) {
                    n++;
                    if(n >= max) {
                        return n;
                    }
                }
            }
            for(final ChangedField field : this.changed.values()) {
                n += field.isChanged() ? 1 : 0;
                if(n >= max) {
                    return n;
                }
            }
        }
        return n;
    }

    @Override
    public XWritableField createField(final XId fieldId) {

        final XWritableField oldField = getField(fieldId);
        if(oldField != null) {
            return oldField;
        }

        final XReadableField field = this.base.getField(fieldId);
        if(field != null) {

            // If the field previously existed it must have been removed
            // previously and we can merge the remove and add changes.
            XyAssert.xyAssert(this.removed.contains(fieldId));
            XyAssert.xyAssert(!this.changed.containsKey(fieldId));
            this.removed.remove(fieldId);
            final ChangedField newField = new ChangedField(field);
            newField.setValue(null);
            this.changed.put(fieldId, newField);

            XyAssert.xyAssert(checkSetInvariants());

            return newField;

        } else {

            // Otherwise, the field is completely new.
            final XAddress fieldAddr = Base.resolveField(getAddress(), fieldId);
            final SimpleField newField = new SimpleField(fieldAddr);
            newField.setRevisionNumber(getRevisionNumber());
            this.added.put(fieldId, newField);

            XyAssert.xyAssert(checkSetInvariants());

            return newField;
        }

    }

    public boolean executeCommand(final XCommand command) {
        return ChangeExecutor.executeAnyCommand(command, this);
    }

    @Override
    public boolean exists() {
        return this.objectExists;
    }

    @Override
    public Collection<? extends XReadableField> getAdded() {
        return this.added.values();
    }

    @Override
    public XAddress getAddress() {
        return this.base.getAddress();
    }

    /**
     * @return an {@link Iterable} of the fields that already existed in the
     *         original {@link XReadableObject} but have been changed. Note:
     *         their current state might be the same as the original one. Use
     *         {@link ChangedField#isChanged()} to check if they are actually
     *         different form the original field.
     */
    public Iterable<ChangedField> getChangedFields() {
        return this.changed.values();
    }

    @Override
    public XWritableField getField(final XId fieldId) {
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        XyAssert.xyAssert(this.base != null);
        assert this.base != null;
        XyAssert.xyAssert(checkSetInvariants());

        final SimpleField newField = this.added.get(fieldId);
        if(newField != null) {
            return newField;
        }

        ChangedField changedField = this.changed.get(fieldId);
        if(changedField != null) {
            return changedField;
        }

        if(this.removed.contains(fieldId)) {
            return null;
        }

        final XReadableField field = this.base.getField(fieldId);
        if(field == null) {
            return null;
        }

        changedField = new ChangedField(field);
        this.changed.put(fieldId, changedField);

        XyAssert.xyAssert(checkSetInvariants());

        return changedField;
    }

    @Override
    public XId getId() {
        return this.base.getId();
    }

    /**
     * @return the {@link SimpleField SimpleFields} that have been added to this
     *         ChangedObject and were not contained in the original
     *         {@link XReadableObject}
     */
    public Iterable<SimpleField> getNewFields() {
        return this.added.values();
    }

    /**
     * @param fieldId
     * @return the {@link XReadableField} with the given {@link XId} as it
     *         exists in the original {@link XReadableField}.
     */
    public XReadableField getOldField(final XId fieldId) {
        return this.base.getField(fieldId);
    }

    @Override
    public Collection<? extends IFieldDiff> getPotentiallyChanged() {
        return this.changed.values();
    }

    @Override
    public Collection<XId> getRemoved() {
        return this.removed;
    }

    /**
     * @return the {@link XId XIds} of the {@link XReadableField XBaseFields}
     *         that existed in the original {@link XReadableObject} but have
     *         been removed
     */
    public Iterable<XId> getRemovedFields() {
        return this.removed;
    }

    /**
     * Return the revision number of the wrapped {@link XReadableObject}. The
     * revision number does not increase with changes to this
     * {@link ChangedObject}.
     *
     * @return the revision number of the original {@link XReadableObject}
     */
    @Override
    public long getRevisionNumber() {
        return this.base.getRevisionNumber();
    }

    @Override
    public XType getType() {
        return XType.XOBJECT;
    }

    /**
     * @return false if there are no changes. True if there are changes.
     */
    @Override
    public boolean hasChanges() {
        return !this.added.isEmpty() || !this.removed.isEmpty() || countCommandsNeeded(1) > 0;
    }

    @Override
    public boolean hasField(final XId fieldId) {
        return this.added.containsKey(fieldId)
                || !this.removed.contains(fieldId) && this.base.hasField(fieldId);
    }

    public boolean isChanged() {
        boolean changed = !this.added.isEmpty();
        changed |= !this.removed.isEmpty();
        for(final ChangedField cf : this.changed.values()) {
            changed |= cf.isChanged();
        }
        return changed;
    }

    @Override
    public boolean isEmpty() {

        if(!this.added.isEmpty()) {
            return false;
        }

        if(this.removed.isEmpty()) {
            return this.base.isEmpty();
        }

        if(this.changed.size() > this.removed.size()) {
            return false;
        }

        for(final XId fieldId : this.base) {
            if(!this.removed.contains(fieldId)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterator<XId> iterator() {

        final Iterator<XId> filtered = new AbstractFilteringIterator<XId>(this.base.iterator()) {
            @Override
            protected boolean matchesFilter(final XId entry) {
                return !ChangedObject.this.removed.contains(entry);
            }
        };

        return new BagUnionIterator<XId>(filtered, this.added.keySet().iterator());
    }

    @Override
    public boolean removeField(final XId fieldId) {

        if(this.added.containsKey(fieldId)) {

            // Never existed in base, so removing from added is sufficient.
            XyAssert.xyAssert(!this.base.hasField(fieldId) && !this.changed.containsKey(fieldId));
            XyAssert.xyAssert(!this.removed.contains(fieldId));

            this.added.remove(fieldId);

            XyAssert.xyAssert(checkSetInvariants());

            return true;

        } else if(!this.removed.contains(fieldId) && this.base.hasField(fieldId)) {

            // Exists in base and not removed yet.
            XyAssert.xyAssert(!this.added.containsKey(fieldId));

            this.removed.add(fieldId);
            this.changed.remove(fieldId);

            XyAssert.xyAssert(checkSetInvariants());

            return true;

        }

        return false;
    }

    @Override
    public void setExists(final boolean entityExists) {
        this.objectExists = entityExists;
    }

}
