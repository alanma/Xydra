package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.index.XI;


/**
 * An implementation of {@link XDoubleListValue}
 *
 * @author kaidel
 * @author dscharrer
 *
 */
public class MemoryDoubleListValue extends MemoryListValue<Double> implements XDoubleListValue,
        Serializable {

	private static final long serialVersionUID = -2339822884334461166L;

	// non-final to be GWT-Serializable
	private double[] list;

	// empty constructor for GWT-Serializable
	protected MemoryDoubleListValue() {
	}

	public MemoryDoubleListValue(final Collection<Double> content) {
		this.list = new double[content.size()];
		int i = 0;
		for(final double b : content) {
			this.list[i++] = b;
		}
	}

	public MemoryDoubleListValue(final double[] content) {
		this.list = new double[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}

	private MemoryDoubleListValue(final int length) {
		this.list = new double[length];
	}

	@Override
	public XDoubleListValue add(final Double entry) {
		return add(this.list.length, entry);
	}

	@Override
	public XDoubleListValue add(final int index, final Double entry) {
		final int size = this.list.length;
		if(index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryDoubleListValue v = new MemoryDoubleListValue(size + 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		v.list[index] = entry;
		System.arraycopy(this.list, index, v.list, index + 1, size - index);
		return v;
	}

	@Override
	public double[] contents() {
		final double[] array = new double[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof XDoubleListValue
		        && XI.equalsIterator(iterator(), ((XDoubleListValue)other).iterator());
	}

	@Override
	public Double get(final int index) {
		return this.list[index];
	}

	@Override
	public ValueType getComponentType() {
		return ValueType.Double;
	}

	@Override
	public ValueType getType() {
		return ValueType.DoubleList;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}

	@Override
	public XDoubleListValue remove(final Double entry) {
		final int index = indexOf(entry);
		if(index < 0) {
			return this;
		}
		return remove(index);
	}

	@Override
	public XDoubleListValue remove(final int index) {
		final int size = this.list.length;
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		final MemoryDoubleListValue v = new MemoryDoubleListValue(size - 1);
		System.arraycopy(this.list, 0, v.list, 0, index);
		System.arraycopy(this.list, index + 1, v.list, index, size - index - 1);
		return v;
	}

	@Override
	public int size() {
		return this.list.length;
	}

	@Override
	public Double[] toArray() {
		final Double[] array = new Double[this.list.length];
		fillArray(array);
		return array;
	}

	@Override
	public Number[] toNumberArray() {
		final Number[] array = new Number[this.list.length];
		int i = 0;
		for(final Number e : this) {
			array[i++] = e;
		}
		return array;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}

}
