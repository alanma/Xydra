package org.xydra.core.model.delta;

import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;

public class SummaryValue extends SummaryEntity {
	public XValue oldValue = null;
	public XValue newValue = null;

	/**
	 * @param ae
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull
	 */
	public void apply(XAtomicEvent ae, XChangeLog changeLog, XReadableModel baseSnapshotModel) {
		this.change.apply(ae.getChangeType());
		XFieldEvent xfe = (XFieldEvent) ae;
		switch (ae.getChangeType()) {

		case ADD:
			this.newValue = xfe.getNewValue();
			break;
		case CHANGE:
			this.oldValue = TransactionSummary.getOldValue(xfe, changeLog, baseSnapshotModel);
			this.newValue = xfe.getNewValue();
			break;
		case REMOVE:
			this.oldValue = TransactionSummary.getOldValue(xfe, changeLog, baseSnapshotModel);
			break;
		case TRANSACTION:
		default:
			break;
		}

	}

	@Override
	public String toString() {
		return "V " + this.change + "\n"

		+ "V old: " + this.oldValue + "\n"

		+ "V new: " + this.newValue + "";
	}

}