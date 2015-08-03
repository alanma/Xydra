package org.xydra.core.model.delta;

import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;

/**
 * State: {@link Change} (of the field); old {@link XValue}; new {@link XValue};
 *
 * @author xamde
 */
public class SummaryField extends SummaryEntity {

	/* the super.change is about the field */

	public SummaryField(final XId id) {
		super(id);
	}

	/* this is about the value */
	private XValue newValue = null;

	/* this is about the value */
	private XValue oldValue = null;

	@Override
	public String toString() {
		return toString("").toString();
	}

	/**
	 * @param ae
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull
	 */
	public void apply(final XAtomicEvent ae, final XChangeLog changeLog, final XReadableModel baseSnapshotModel) {
		if (ae instanceof XObjectEvent) {
			this.change.apply(ae.getChangeType());
		}
		final XFieldEvent xfe = (XFieldEvent) ae;
		switch (ae.getChangeType()) {
		case ADD:
			this.newValue = xfe.getNewValue();
			break;
		case CHANGE:
			// update only if not already set
			if (this.oldValue == null) {
				this.oldValue = OldValues.getOldValue(xfe, changeLog, baseSnapshotModel);
			}
			this.newValue = xfe.getNewValue();
			break;
		case REMOVE:
			// update only if not already set
			if (this.oldValue == null) {
				this.oldValue = OldValues.getOldValue(xfe, changeLog, baseSnapshotModel);
				this.newValue = null;
			}
			break;
		case TRANSACTION:
		default:
			break;
		}

	}

	/**
	 * @return @CanBeNull
	 */
	public XValue getNewValue() {
		return this.newValue;
	}

	/**
	 * @return @CanBeNull
	 */
	public XValue getOldValue() {
		return this.oldValue;
	}

	public StringBuilder toString(final String indent) {
		final StringBuilder b = new StringBuilder();
		b.append(indent + "Field." + this.change + " '" + getId() + "'  ");
		b.append(indent);
		switch (this.change.getAtomicChangeType()) {
		case Add:
			b.append("+ '" + this.newValue + "'");
			break;
		case Change:
			b.append("Changed: ['" + this.oldValue + "'->'" + this.newValue + "']");
			break;
		case Remove:
			b.append("- '" + this.oldValue + "'");
			break;
		default:
			throw new AssertionError();
		}
		b.append(" Events: " + super.appliedEvents + "\n");
		return b;
	}

	public boolean isNoOp() {
		if (this.oldValue == null && this.newValue == null) {
			return true;
		}
		if (this.oldValue == this.newValue) {
			return true;
		}

		return false;
	}

}