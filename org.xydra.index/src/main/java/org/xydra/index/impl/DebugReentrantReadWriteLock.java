package org.xydra.index.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.util.SharedExceptionUtils;

/**
 * A drop-in replacement of {@link ReentrantReadWriteLock} which provides more
 * debug information.
 * 
 * Configurable via log#isDebugEnabled
 * 
 * It can be closed by a thread multiple times (even when it is already closed)
 * without causing any errors.
 * 
 * @author xamde
 */
@RunsInGWT(false)
public class DebugReentrantReadWriteLock extends ReentrantReadWriteLock implements ReadWriteLock {

	class DebugReadLock implements Lock {

		@Override
		public void lock() {
			readOperationStart();
			DebugReentrantReadWriteLock.super.readLock().lock();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			readOperationStart();
			DebugReentrantReadWriteLock.super.readLock().lockInterruptibly();
		}

		@Override
		public Condition newCondition() {
			return DebugReentrantReadWriteLock.super.readLock().newCondition();
		}

		@Override
		public boolean tryLock() {
			boolean b = DebugReentrantReadWriteLock.super.readLock().tryLock();
			if (b) {
				readOperationStart();
			}
			return b;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			boolean b = DebugReentrantReadWriteLock.super.readLock().tryLock(time, unit);
			if (b) {
				readOperationStart();
			}
			return b;
		}

		@Override
		public void unlock() {
			DebugReentrantReadWriteLock.super.readLock().unlock();
			readOperationEnd();
		}

	}

	class DebugWriteLock implements Lock {

		@Override
		public void lock() {
			writeOperationStart();
			DebugReentrantReadWriteLock.super.writeLock().lock();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			writeOperationStart();
			DebugReentrantReadWriteLock.super.writeLock().lockInterruptibly();
		}

		@Override
		public Condition newCondition() {
			return DebugReentrantReadWriteLock.super.writeLock().newCondition();
		}

		@Override
		public boolean tryLock() {
			boolean b = DebugReentrantReadWriteLock.super.writeLock().tryLock();
			if (b) {
				writeOperationStart();
			}
			return b;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			boolean b = DebugReentrantReadWriteLock.super.writeLock().tryLock(time, unit);
			if (b) {
				writeOperationStart();
			}
			return b;
		}

		@Override
		public void unlock() {
			DebugReentrantReadWriteLock.super.writeLock().unlock();
			writeOperationEnd();
		}

	}

	private static final Logger log = LoggerFactory.getLogger(DebugReentrantReadWriteLock.class);

	private int openReadCount = 0;

	/*
	 * thread -> set of stack traces. A thread may open and close a read access
	 * at very different places. We can only track ALL open reads and close
	 * them, when this thread released all of them. No other kind of mapping is
	 * possible.
	 */
	MapSetIndex<Long, String> openReads = MapSetIndex.createWithFastEntrySets();

	private int openWriteCount = 0;

	MapSetIndex<Long, String> openWrites = MapSetIndex.createWithFastEntrySets();

	private transient Lock debugReadLock;

	private transient Lock debugWriteLock;

	private long maxWaitForWriteLockMillis = 10 * 1000;

	/**
	 * Behavior depends on setting of log. If isDebugEnabled, debug support is
	 * on.
	 */
	public DebugReentrantReadWriteLock() {
		if (log.isDebugEnabled()) {
			this.debugReadLock = new DebugReadLock();
			this.debugWriteLock = new DebugWriteLock();
		} else {
			this.debugReadLock = super.readLock();
			this.debugWriteLock = super.writeLock();
		}
	}

	@Override
	public Lock readLock() {
		return this.debugReadLock;
	}

	/**
	 * Read operation just ended, we released the lock already
	 */
	private void readOperationEnd() {
		this.openReadCount--;

		if (super.getReadHoldCount() == 0) {
			long id = Thread.currentThread().getId();
			this.openReads.deIndex(id);

			return;
		}
		stats();
	}

	/**
	 * Read operation will start, about the get the lock ...
	 */
	private void readOperationStart() {
		this.openReadCount++;
		try {
			throw new RuntimeException("CALL");
		} catch (RuntimeException e) {
			String stackTrace = SharedExceptionUtils.toString(e);
			long id = Thread.currentThread().getId();
			this.openReads.index(id, stackTrace);
		}
		stats();
	}

	private void stats() {
		if (log.isDebugEnabled())
			log.debug("openReads=" + super.getReadHoldCount() + "/" + this.openReadCount
					+ " openWrites=" + super.getWriteHoldCount() + "/" + this.openWriteCount);
	}

	@Override
	public Lock writeLock() {
		return this.debugWriteLock;
	}

	/**
	 * Write operation just ended, we released the lock alwritey
	 */
	private void writeOperationEnd() {
		this.openWriteCount--;

		if (super.getWriteHoldCount() == 0) {
			long id = Thread.currentThread().getId();
			this.openWrites.deIndex(id);

			return;
		}
		stats();
	}

	/**
	 * Write operation will start, about the get the lock ...
	 */
	private void writeOperationStart() {
		this.openWriteCount++;
		try {
			throw new RuntimeException("CALL");
		} catch (RuntimeException e) {
			String stackTrace = SharedExceptionUtils.toString(e);
			long id = Thread.currentThread().getId();
			this.openWrites.index(id, stackTrace);
		}
		stats();

		// lock gymnastics
		boolean canLock = false;
		long start = System.nanoTime();
		while (!canLock) {
			canLock = super.writeLock().tryLock();
			if (canLock)
				super.writeLock().unlock();
			else {
				long now = System.nanoTime();
				long duration = now - start;
				long millisWaited = duration / (1000 * 1000);
				log.info("Could not get write lock, waited already " + millisWaited
						+ ". Will wait " + this.maxWaitForWriteLockMillis + ".");
				if (millisWaited > this.maxWaitForWriteLockMillis) {
					// waited over k=10 seconds
					log.warn("Could not get lock even after waiting "
							+ this.maxWaitForWriteLockMillis + " ms");
					log.info("Reads have been recorded from these threads / code locations:");
					this.openReads.dump();
					log.info("Thread sleeps now for 1 hour to let you debug");
					try {
						Thread.sleep(60 * 60 * 1000);
					} catch (InterruptedException e) {
						throw new RuntimeException("Error", e);
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException("Error", e);
				}
			}
		}
	}

}
