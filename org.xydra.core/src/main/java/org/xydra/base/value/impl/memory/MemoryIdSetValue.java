package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;

import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIdSetValue;


/**
 * An implementation of {@link XIdSetValue}
 *
 * @author dscharrer
 *
 */
public class MemoryIdSetValue extends MemorySetValue<XId> implements XIdSetValue, Serializable {

    private static final long serialVersionUID = -83885798275571937L;

    // empty constructor for GWT-Serializable
    protected MemoryIdSetValue() {
    }

    public MemoryIdSetValue(final Collection<XId> contents) {
        super(contents);
    }

    public MemoryIdSetValue(final XId[] contents) {
        super(contents);
    }

    @Override
    public XIdSetValue add(final XId entry) {
        final MemoryIdSetValue v = new MemoryIdSetValue(this.set);
        v.set.add(entry);
        return v;
    }

    @Override
    public XId[] contents() {
        return toArray(new XId[size()]);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof XIdSetValue && checkEquals((XIdSetValue)other);
    }

    @Override
    public ValueType getComponentType() {
        return ValueType.Id;
    }

    @Override
    public ValueType getType() {
        return ValueType.IdSet;
    }

    @Override
    public int hashCode() {
        return getHashCode();
    }

    @Override
    public XIdSetValue remove(final XId entry) {
        final MemoryIdSetValue v = new MemoryIdSetValue(this.set);
        v.set.remove(entry);
        return v;
    }

    @Override
    public XId[] toArray() {
        return contents();
    }

}
