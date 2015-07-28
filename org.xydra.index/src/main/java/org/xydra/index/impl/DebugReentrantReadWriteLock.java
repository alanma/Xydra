package org.xydra.index.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.util.SharedExceptionUtils;

/**
 * A drop-in replacement of {@link ReentrantReadWriteLock} which provides more
 * debug information.
 *
 * Configurable via log#isDebugEnabled
 *
 * The lock count is not used. The first call to lock() locks, thr first call to
 * unlock() unlocks. Redundant calls of lock() and unlock() are silently
 * ignored.
 *
 * I.e. It can be locked by a thread multiple times (even when already locked)
 * without causing any errors.
 *
 * I.e. It can be unlocked by a thread multiple times (even when it is already
 * unlocked) without causing any errors.
 *
 * @author xamde
 */
@RunsInGWT(false)
public class DebugReentrantReadWriteLock implements ReadWriteLock {

	private final ReentrantReadWriteLock reentrant = new ReentrantReadWriteLock();

	class DebugReadLock implements Lock {

		@Override
		public void lock() {
			readOperationStart();
			DebugReentrantReadWriteLock.this.reentrant.readLock().lock();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			readOperationStart();
			DebugReentrantReadWriteLock.this.reentrant.readLock().lockInterruptibly();
		}

		@Override
		public Condition newCondition() {
			return DebugReentrantReadWriteLock.this.reentrant.readLock().newCondition();
		}

		@Override
		public boolean tryLock() {
			final boolean b = DebugReentrantReadWriteLock.this.reentrant.readLock().tryLock();
			if (b) {
				readOperationStart();
			}
			return b;
		}

		@Override
		public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
			final boolean b = DebugReentrantReadWriteLock.this.reentrant.readLock().tryLock(time, unit);
			if (b) {
				readOperationStart();
			}
			return b;
		}

		@Override
		public void unlock() {
			try {
				DebugReentrantReadWriteLock.this.reentrant.readLock().unlock();
			} catch (final IllegalMonitorStateException e) {
				log.warn("Lock issue, ignored",e);
			}
			readOperationEnd();
		}

	}

	class DebugWriteLock implements Lock {

		@Override
		public void lock() {
			writeOperationStart();
			DebugReentrantReadWriteLock.this.reentrant.writeLock().lock();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			writeOperationStart();
			DebugReentrantReadWriteLock.this.reentrant.writeLock().lockInterruptibly();
		}

		@Override
		public Condition newCondition() {
			return DebugReentrantReadWriteLock.this.reentrant.writeLock().newCondition();
		}

		@Override
		public boolean tryLock() {
			final boolean b = DebugReentrantReadWriteLock.this.reentrant.writeLock().tryLock();
			if (b) {
				writeOperationStart();
			}
			return b;
		}

		@Override
		public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
			final boolean b = DebugReentrantReadWriteLock.this.reentrant.writeLock().tryLock(time, unit);
			if (b) {
				writeOperationStart();
			}
			return b;
		}

		@Override
		public void unlock() {
			DebugReentrantReadWriteLock.this.reentrant.writeLock().unlock();
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

	private final long maxWaitForWriteLockMillis = 10 * 1000;

	@Setting("Collection of stack-traces of threads that acquire read-locks")
	private static final boolean ALWAYS_ON = true;

	/**
	 * Behavior depends on setting of log. If isDebugEnabled, debug support is
	 * on.
	 */
	@SuppressWarnings("unused")
	public DebugReentrantReadWriteLock() {
		if (ALWAYS_ON || log.isDebugEnabled()) {
			this.debugReadLock = new DebugReadLock();
			this.debugWriteLock = new DebugWriteLock();
		} else {
			this.debugReadLock = this.reentrant.readLock();
			this.debugWriteLock = this.reentrant.writeLock();
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

		if (this.reentrant.getReadHoldCount() == 0) {
			final long id = Thread.currentThread().getId();
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
		} catch (final RuntimeException e) {
			final String stackTrace = SharedExceptionUtils.toString(e);
			final long id = Thread.currentThread().getId();
			this.openReads.index(id, stackTrace);
		}
		stats();
	}

	private void stats() {
		if (log.isDebugEnabled()) {
			log.debug("this thread openReads=" + this.reentrant.getReadHoldCount() + "/"
					+ this.openReadCount + " openWrites=" + this.reentrant.getWriteHoldCount()
					+ "/" + this.openWriteCount);
		}
	}

	@Override
	public Lock writeLock() {
		return this.debugWriteLock;
	}

	/**
	 * Write operation just ended, we released the lock already
	 */
	private void writeOperationEnd() {
		this.openWriteCount--;

		if (this.reentrant.getWriteHoldCount() == 0) {
			final long id = Thread.currentThread().getId();
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
		} catch (final RuntimeException e) {
			final String stackTrace = SharedExceptionUtils.toString(e);
			final long id = Thread.currentThread().getId();
			this.openWrites.index(id, stackTrace);
		}
		stats();

		// lock gymnastics
		boolean canLock = false;
		final long start = System.nanoTime();
		while (!canLock) {
			canLock = this.reentrant.writeLock().tryLock();
			if (canLock) {
				this.reentrant.writeLock().unlock();
			} else {
				final long now = System.nanoTime();
				final long duration = now - start;
				final long millisWaited = duration / (1000 * 1000);
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
					} catch (final InterruptedException e) {
						throw new RuntimeException("Error", e);
					}
				}
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					throw new RuntimeException("Error", e);
				}
			}
		}
	}

}
