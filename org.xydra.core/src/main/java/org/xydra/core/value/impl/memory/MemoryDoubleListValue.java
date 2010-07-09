package org.xydra.core.value.impl.memory;

import java.util.Arrays;
import java.util.Collection;

import org.xydra.core.XX;
import org.xydra.core.value.XDoubleListValue;


/**
 * An implementation of {@link XDoubleListValue}
 * 
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryDoubleListValue extends MemoryListValue<Double> implements XDoubleListValue {
	
	private static final long serialVersionUID = -2339822884334461166L;
	
	private final double[] list;
	
	public MemoryDoubleListValue(Collection<Double> content) {
		this.list = new double[content.size()];
		int i = 0;
		for(double b : content) {
			this.list[i++] = b;
		}
	}
	
	public MemoryDoubleListValue(double[] content) {
		this.list = new double[content.length];
		System.arraycopy(content, 0, this.list, 0, content.length);
	}
	
	public double[] contents() {
		double[] array = new double[this.list.length];
		System.arraycopy(this.list, 0, array, 0, this.list.length);
		return array;
	}
	
	public Double[] toArray() {
		Double[] array = new Double[this.list.length];
		fillArray(array);
		return array;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof XDoubleListValue
		        && XX.equalsIterator(this.iterator(), ((XDoubleListValue)other).iterator());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.list);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(this.list);
	}
	
	public Double get(int index) {
		return this.list[index];
	}
	
	public int size() {
		return this.list.length;
	}
	
}
