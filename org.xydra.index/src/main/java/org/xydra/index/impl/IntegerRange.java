package org.xydra.index.impl;

import org.xydra.index.IRange;

/**
 * In most cases using this class is not performant. Using two primitive
 * integers should almost always be faster.
 *
 * @author xamde
 */
public class IntegerRange implements IRange<Integer> {

	public IntegerRange(final int start, final int end) {
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

	public void setStart(final int start) {
		this.start = start;
		assertValid();
	}

	public void setEnd(final int end) {
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

	public void expandToInclude(final int value) {
		if (value < this.start) {
			this.start = value;
		} else if (value > this.end) {
			this.end = value;
		}
	}

}
