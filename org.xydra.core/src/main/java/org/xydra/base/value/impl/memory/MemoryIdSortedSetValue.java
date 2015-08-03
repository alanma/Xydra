package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIdSortedSetValue;
import org.xydra.base.value.XSetDiffable;
import org.xydra.base.value.XSetValue;
import org.xydra.base.value.impl.memory.MemorySetValue.SetDiff;


/**
 * An implementation of {@link XIdSortedSetValue}
 *
 * @author dscharrer, voelkel
 *
 */
public class MemoryIdSortedSetValue extends MemoryIdListValue implements XIdSortedSetValue,
        Serializable {

    private static final long serialVersionUID = -83885798275571937L;

    // empty constructor for GWT-Serializable
    protected MemoryIdSortedSetValue() {
    }

    public MemoryIdSortedSetValue(final Collection<XId> contents) {
        super(contents);
    }

    public MemoryIdSortedSetValue(final XId[] contents) {
        super(contents);
    }

    @Override
    public MemoryIdSortedSetValue add(final XId entry) {
        if(contains(entry)) {
            // no need to add it
            return this;
        } else {
            final XId[] newList = MemoryIdListValue.createArrayWithEntryInsertedAtPosition(
                    contents(), contents().length, entry);
            return new MemoryIdSortedSetValue(newList);
        }
    }

    @Override
    public ValueType getType() {
        return ValueType.IdSortedSet;
    }

    @Override
    public MemoryIdSortedSetValue remove(final XId entry) {
        // find it
        final int index = indexOf(entry);
        if(index == -1) {
            // not possible to remove it
            return this;
        } else {
            final XId[] newList = MemoryIdListValue.createArrayWithEntryRemovedAtPosition(
                    contents(), index);
            return new MemoryIdSortedSetValue(newList);
        }
    }

    @Override
    public Set<XId> toSet() {
        final Set<XId> copy = new HashSet<XId>();
        final XId[] list = contents();
        for(int i = 0; i < list.length; i++) {
            copy.add(list[i]);
        }
        return copy;
    }

    @Override
    public SortedSet<XId> toSortedSet() {
        final SortedSet<XId> copy = new TreeSet<XId>();
        final XId[] list = contents();
        for(int i = 0; i < list.length; i++) {
            copy.add(list[i]);
        }
        return copy;
    }

    @Override
    public XSetDiffable.XSetDiff<XId> computeDiff(final XSetValue<XId> otherSet) {
        final SetDiff<XId> diff = new SetDiff<XId>();

        diff.added.addAll(otherSet.toSet());
        diff.removed.addAll(toSet());
        // remove (this set-intersection other) from added and removed
        diff.removed.removeAll(diff.added);
        diff.added.removeAll(toSet());

        return diff;

    }

}
