package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.iterator.ReadOnlyIterator;
import org.xydra.xgae.annotations.XGaeOperation;

/**
 * Implementation of the lock semantics used by {@link IGaeChangesService}.
 *
 * Locks can be acquired for each part of the MOF tree.
 *
 * A lock on a {@link XModel} or {@link XObject} implies locks on all contained
 * {@link XObject XObjects} and {@link XField XFields}.
 *
 * Internally a set of mutually exclusive MOF addresses is used. None of the
 * addresses lies within the address range of another one.
 *
 * @author dscharrer
 *
 */
public class GaeLocks implements Iterable<XAddress> {

	public static final long serialVersionUID = 4334940263327007176L;

	private final Set<XAddress> locks;

	/**
	 * Get the minimal locks needed to execute the given {@link XCommand}.
	 *
	 * An address in the locks means that exclusive access is required to the
	 * entity referred to by that address, as well as all descendant entities.
	 * Also, a read lock on the ancestors of that entity is implied.
	 *
	 * @param command
	 *            The command to calculate the locks for.
	 * @return never null
	 */
	@XGaeOperation()
	public static GaeLocks createLocks(final XCommand command) {
		final GaeLocks gaeLocks = new GaeLocks();
		if (command instanceof XTransaction) {

			final XTransaction transaction = (XTransaction) command;
			final Set<XAddress> tempLocks = new HashSet<XAddress>();
			for (final XAtomicCommand atomicCommand : transaction) {
				final XAddress lock = atomicCommand.getChangedEntity();
				assert lock != null;
				/*
				 * IMPROVE: ADD events don't need to lock the whole added entity
				 * (they don't care if children change)
				 */
				tempLocks.add(lock);
			}
			for (final XAddress lock : tempLocks) {
				if (!hasMoreGeneralLock(tempLocks, lock)) {
					gaeLocks.add(lock);
				}
			}

		} else {
			final XAddress lock = command.getChangedEntity();
			assert lock != null;
			gaeLocks.add(lock);
		}
		return gaeLocks;
	}

	private void add(final XAddress lockAddress) {
		assert lockAddress != null;
		this.locks.add(lockAddress);
	}

	/**
	 * Load a set of locks.
	 *
	 * @param encodedLocks
	 *            The locks to load.
	 */
	protected GaeLocks(final List<String> encodedLocks) {
		this.locks = new HashSet<XAddress>((int) (encodedLocks.size() / 0.75));
		for (final String s : encodedLocks) {
			this.locks.add(Base.toAddress(s));
		}
	}

	public GaeLocks() {
		this.locks = new HashSet<XAddress>();
	}

	List<String> encode() {
		final List<String> lockStrs = new ArrayList<String>(this.locks.size());
		for (final XAddress a : this.locks) {
			lockStrs.add(a.toURI());
		}
		return lockStrs;
	}

	/**
	 * @param addr
	 * @return true if the specified locks are sufficient to write to the entity
	 *         at the given address.
	 */
	public boolean canWrite(final XAddress addr) {
		for (final XAddress lock : this.locks) {
			if (lock.equalsOrContains(addr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param addr
	 * @return true if the specified locks are sufficient to read from the
	 *         entity at the given address.
	 */
	public boolean canRead(final XAddress addr) {
		for (final XAddress lock : this.locks) {
			if (addr.equalsOrContains(lock) || lock.contains(addr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param other
	 * @return if the two sets of locks conflict, i.e. one of them requires a
	 *         lock that is implied by the other set (wild-card locks are
	 *         respected).
	 */
	public boolean isConflicting(final GaeLocks other) {
		for (final XAddress lock : this.locks) {
			if (other.locks.contains(lock) || hasMoreGeneralLock(other.locks, lock)) {
				return true;
			}
		}
		for (final XAddress lock : other.locks) {
			if (hasMoreGeneralLock(this.locks, lock)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles wild-cards in locks.
	 *
	 * @return true if the given set contains any locks that imply the given
	 *         lock (but are not the same).
	 */
	static private boolean hasMoreGeneralLock(final Set<XAddress> locks, final XAddress lock) {
		XAddress l = lock.getParent();
		while (l != null) {
			if (locks.contains(l)) {
				return true;
			}
			l = l.getParent();
		}
		return false;
	}

	/**
	 * @param addr
	 * @return true if the specified locks are sufficient to delete the entity
	 *         at the given address.
	 */
	public boolean canRemove(final XAddress addr) {
		return canWrite(addr);
	}

	/**
	 * @return number of locks
	 */
	public int size() {
		return this.locks.size();
	}

	@Override
	public Iterator<XAddress> iterator() {
		return new ReadOnlyIterator<XAddress>(this.locks.iterator());
	}

	@Override
	public String toString() {
		return Arrays.toString(encode().toArray());
	}

	/**
	 * @return if a complete model lock is represented
	 */
	public boolean isLockingTheModel() {
		if (this.locks.size() == 1) {
			final XAddress xa = this.locks.iterator().next();
			if (xa.getAddressedType() == XType.XMODEL) {
				return true;
			}
		}
		return false;
	}

}
