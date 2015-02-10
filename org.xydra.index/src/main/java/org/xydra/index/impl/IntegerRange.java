package org.xydra.index.impl;

import org.xydra.index.IRange;

/**
 * In most cases using this class is not performant. Using two primitive
 * integers should almost always be faster.
 * 
 * @author xamde
 */
public class IntegerRange implements IRange<Integer> {

	public IntegerRange(int start, int end) {
		super();
		this.start = start;
		this.end = end;
		assertValid();
	}

	private int start;
	private int end;

	@Override
	public Integer getStart() {
		return this.start;
	}

	@Override
	public Integer getEnd() {
		return this.end;
	}

	public void setStart(int start) {
		this.start = start;
		assertValid();
	}

	public void setEnd(int end) {
		this.end = end;
		assertValid();
	}

	private void assertValid() {
		assert this.start <= this.end : "Invalid range [" + this.start + "," + this.end + "]";
	}

	@Override
	public String toString() {
		return "[" + this.start + "," + this.end + "]";
	}

	public void expandToInclude(int value) {
		if (value < this.start) {
			this.start = value;
		} else if (value > this.end) {
			this.end = value;
		}
	}

}
