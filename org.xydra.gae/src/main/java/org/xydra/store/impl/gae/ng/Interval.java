package org.xydra.store.impl.gae.ng;

import org.xydra.sharedutils.XyAssert;


public class Interval {
	
	long end;
	
	long start;
	
	/**
	 * @param start inclusive
	 * @param end inclusive
	 */
	public Interval(long start, long end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	public void adjustStartToFitSizeIfNecessary(long newSize) {
		if(size() > newSize) {
			this.start = this.end - newSize + 1;
		}
		XyAssert.xyAssert(size() <= newSize, "Interval is %s but should be %s", size(), newSize);
	}
	
	public Interval copy() {
		return new Interval(this.start, this.end);
	}
	
	/**
	 * @return true if the interval is empty, i.e. start > end
	 */
	public boolean isEmpty() {
		return this.start > this.end;
	}
	
	/**
	 * @return number of elements in the interval. I.e. from [0,1] there are 2
	 *         elements.
	 */
	public long size() {
		return Math.max(0, this.end - this.start + 1);
	}
	
	/**
	 * @return a non-overlapping interval of the same size, but with bigger
	 *         numbers. I.e. the interval [4,7] (size=4) becomes [8,11]
	 */
	public Interval moveRight() {
		return new Interval(this.end + 1, this.end + this.size());
	}
	
	@Override
	public String toString() {
		return "[" + this.start + "," + this.end + "]";
	}
	
}
