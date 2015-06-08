package org.xydra.core.model.delta;

import org.xydra.base.change.ChangeType;

public class Change {
	/** range -1, 0, 1 */
	private int state = 0;

	private void add() {
		assert this.state <= 0;
		this.state++;
	}

	public AtomicChangeType getAtomicChangeType() {
		switch (this.state) {
		case -1:
			return AtomicChangeType.Remove;
		case 0:
			return AtomicChangeType.Change;
		case 1:
			return AtomicChangeType.Add;
		default:
			throw new AssertionError();
		}
	}

	private void remove() {
		assert this.state >= 0;
		this.state--;
	}

	public boolean isAdded() {
		return this.state > 0;
	}

	public boolean isRemoved() {
		return this.state < 0;
	}

	public boolean isNeutral() {
		return this.state == 0;
	}

	void apply(ChangeType changeType) {
		switch (changeType) {
		case ADD:
			add();
			break;
		case REMOVE:
			remove();
			break;
		case TRANSACTION:
		case CHANGE:
			break;
		}
	}

	@Override
	public String toString() {
		switch (this.state) {
		case -1:
			return "REM";
		case 0:
			return "NOP";
		case 1:
			return "ADD";
		}
		throw new AssertionError();
	}
}