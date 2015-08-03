package org.xydra.core.model.delta;

import org.xydra.base.change.ChangeType;

/**
 * State: added, changed, removed
 *
 * @author xamde
 */
public class Change {

	/** range -1, 0, 1 */
	private AtomicChangeType state = AtomicChangeType.Change;

	public long lastRev;

	private int changeEvents = 0;

	void apply(final ChangeType changeType) {
		switch (changeType) {
		case ADD:
			switch (this.state) {
			case Add:
				throw new AssertionError();
			case Change:
				this.state = AtomicChangeType.Add;
				break;
			case Remove:
				this.state = AtomicChangeType.Remove;
				break;
			}
			break;
		case REMOVE:
			switch (this.state) {
			case Add:
				this.state = AtomicChangeType.Remove;
				break;
			case Change:
				this.state = AtomicChangeType.Remove;
				break;
			case Remove:
				throw new AssertionError();
			}
			break;
		case TRANSACTION:
		case CHANGE:
			this.changeEvents++;
			break;
		}
	}

	public AtomicChangeType getAtomicChangeType() {
		return this.state;
	}

	public boolean isAdded() {
		return getAtomicChangeType() == AtomicChangeType.Add;
	}

	/**
	 * @return true if neither added nor removed. If it really changed, depends
	 *         on the children.
	 */
	public boolean isChanged() {
		return getAtomicChangeType() == AtomicChangeType.Change;
	}

	public int getChangeEvents() {
		return this.changeEvents;
	}

	public boolean isRemoved() {
		return getAtomicChangeType() == AtomicChangeType.Remove;
	}

	@Override
	public String toString() {
		switch (this.state) {
		case Remove:
			return "Removed";
		case Change:
			return "Changed";
		case Add:
			return "Added";
		}
		throw new AssertionError();
	}
}