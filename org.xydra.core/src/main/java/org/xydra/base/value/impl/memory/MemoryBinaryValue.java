package org.xydra.base.value.impl.memory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XBinaryValue;

/**
 * An implementation of {@link XBinaryValue}
 * 
 * @author dscharrer
 * 
 */
public class MemoryBinaryValue implements XBinaryValue, Serializable {

	private static final long serialVersionUID = -674503742791516328L;

	// non-final to be GWT-Serializable
	protected byte[] content;

	// empty constructor for GWT-Serializable
	protected MemoryBinaryValue() {
	}

	public MemoryBinaryValue(byte[] content) {
		this.content = new byte[content.length];
		System.arraycopy(content, 0, this.content, 0, content.length);
	}

	public MemoryBinaryValue(Collection<Byte> content) {
		this.content = new byte[content.size()];
		int i = 0;
		for (byte b : content) {
			this.content[i++] = b;
		}
	}

	@Override
	public byte[] contents() {
		byte[] array = new byte[this.content.length];
		System.arraycopy(this.content, 0, array, 0, this.content.length);
		return array;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof XBinaryValue
				&& Arrays.equals(this.contents(), ((XBinaryValue) other).contents());
	}

	public Byte get(int index) {
		return this.content[index];
	}

	@Override
	public ValueType getType() {
		return ValueType.Binary;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.content);
	}

	@Override
	public String toString() {
		return Arrays.toString(this.content);
	}

	@Override
	public byte[] getValue() {
		return contents();
	}

}
