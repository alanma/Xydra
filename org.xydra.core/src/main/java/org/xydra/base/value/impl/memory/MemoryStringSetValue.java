package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XStringSetValue;


/**
 * An implementation of {@link XStringSetValue}
 *
 * @author dscharrer
 *
 */
public class MemoryStringSetValue extends MemorySetValue<String> implements XStringSetValue,
        Serializable {

	private static final long serialVersionUID = -83885798275571937L;

	// empty constructor for GWT-Serializable
	protected MemoryStringSetValue() {
	}

	public MemoryStringSetValue(final Collection<String> contents) {
		super(contents);
	}

	public MemoryStringSetValue(final String[] contents) {
		super(contents);
	}

	@Override
	public MemoryStringSetValue add(final String entry) {
		final MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.add(entry);
		return v;
	}

	@Override
	public String[] contents() {
		return toArray(new String[size()]);
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XStringSetValue && checkEquals((XStringSetValue)other);
	}

	@Override
	public ValueType getComponentType() {
		return ValueType.String;
	}

	@Override
	public ValueType getType() {
		return ValueType.StringSet;
	}

	@Override
	public int hashCode() {
		return getHashCode();
	}

	@Override
	public MemoryStringSetValue remove(final String entry) {
		final MemoryStringSetValue v = new MemoryStringSetValue(this.set);
		v.set.remove(entry);
		return v;
	}

	@Override
	public String[] toArray() {
		return contents();
	}

}
