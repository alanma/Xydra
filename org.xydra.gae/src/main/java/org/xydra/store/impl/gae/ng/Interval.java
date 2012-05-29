package org.xydra.store.impl.gae.ng;

import org.xydra.sharedutils.XyAssert;


/**
 * 
 * Can safely handled end == Long.MAX_VALUE
 * 
 * @author xamde
 */
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
	
	/**
	 * Shrink interval by moving the start closer to the end so that it has the
	 * required new size
	 * 
	 * @param newSize
	 */
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
		/* avoid long overflow when start=0 or -1 and end = Long.MAX_VALUE */
		long sizeComputed = this.end - this.start + 1;
		if(sizeComputed < 0) {
			return Long.MAX_VALUE;
		} else {
			return Math.max(0, sizeComputed);
		}
	}
	
	/**
	 * Does not change this interval.
	 * 
	 * @return a non-overlapping interval of the same size, but with bigger
	 *         numbers. I.e. the interval [4,7] (size=4) becomes [8,11]
	 */
	public Interval moveRight() {
		return new Interval(this.end + 1, this.end + this.size());
	}
	
	/**
	 * @param maxEnd
	 * @return a potentially #empty interval
	 */
	public Interval moveRightAndShrinkToKeepEndMaxAt(long maxEnd) {
		Interval i = this.moveRight();
		if(maxEnd < i.end) {
			i.end = maxEnd;
		}
		return i;
	}
	
	@Override
	public String toString() {
		return "[" + this.start + "," + this.end + "]=" + size();
	}
	
	/**
	 * @param maxSize
	 * @return either a copy of this interval if not larger than maxSize or a
	 *         sub-interval of the requested size with the same start as this
	 *         interval
	 */
	public Interval getSubInterval(int maxSize) {
		if(this.size() <= maxSize) {
			return this.copy();
		} else {
			return new Interval(this.start, this.start + maxSize - 1);
		}
	}
	
	public Interval firstHalf() {
		return new Interval(this.start, this.start + ((this.size() + 1) / 2) - 1);
	}
	
	public static void main(String[] args) {
		System.out.println(new Interval(2, 5).getSubInterval(10));
	}
	
}
