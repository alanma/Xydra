/*
 * Written by Doug Lea with assistance from members of JCP JSR-166 Expert Group
 * and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.locks;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
public class ReentrantReadWriteLock implements ReadWriteLock {

	/**
	 * Returns the lock used for reading.
	 *
	 * @return the lock used for reading.
	 */
	@Override
	public Lock readLock() {
		return new DoNothingLock();
	}

	/**
	 * Returns the lock used for writing.
	 *
	 * @return the lock used for writing.
	 */
	@Override
	public Lock writeLock() {
		return new DoNothingLock();
	}

	public int getReadHoldCount() {
		return 0;
	}

	public int getWriteHoldCount() {
		return 0;
	}

}
