package org.xydra.core.model.delta;

import org.xydra.base.change.ChangeType;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * State: added, changed, removed
 * 
 * @author xamde
 */
public class Change {

	private static final Logger log = LoggerFactory.getLogger(Change.class);

	/** range -1, 0, 1 */
	private AtomicChangeType state = AtomicChangeType.Change;

	public long lastRev;

	void apply(ChangeType changeType) {
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