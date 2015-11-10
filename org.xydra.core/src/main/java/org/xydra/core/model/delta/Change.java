package org.xydra.core.model.delta;

import org.xydra.base.change.ChangeType;

/**
 * State: added, changed, removed
 *
 * @author xamde
 */
public class Change {

	/** Added, Changed, Removed, null = Added+Removed */
	private ChangeSummaryType state = ChangeSummaryType.Neutral;

	public long lastRev;

	private int changeEvents = 0;

	void apply(final ChangeType changeType) {
		switch (changeType) {
		case ADD:
			switch (this.state) {
			case Added:
				throw new AssertionError("Cannot add something, which is already added");
			case Neutral:
				this.state = ChangeSummaryType.Added;
				break;
			case Removed:
				this.state = ChangeSummaryType.Neutral;
				break;
			}
			break;
		case REMOVE:
			switch (this.state) {
			case Added:
				this.state = ChangeSummaryType.Neutral;
				break;
			case Neutral:
				this.state = ChangeSummaryType.Removed;
				break;
			case Removed:
				throw new AssertionError("Cannot remove something, which is already removed");
			}
			break;
		case TRANSACTION:
		case CHANGE:
			this.changeEvents++;
			break;
		}
	}

	public ChangeSummaryType getChangeSummaryType() {
		return this.state;
	}

	public boolean isAdded() {
		return getChangeSummaryType() == ChangeSummaryType.Added;
	}

	/**
	 * @return true if neither added nor removed. If it really changed, depends
	 *         on the children.
	 */
	public boolean isChanged() {
		return getChangeSummaryType() == ChangeSummaryType.Neutral;
	}

	public int getChangeEvents() {
		return this.changeEvents;
	}

	public boolean isRemoved() {
		return getChangeSummaryType() == ChangeSummaryType.Removed;
	}

	@Override
	public String toString() {
		switch (this.state) {
		case Removed:
			return "Removed";
		case Neutral:
			return "Neutral";
		case Added:
			return "Added";
		default:
			break;
		}
		throw new AssertionError();
	}
}