package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;

/**
 * Transform an {@link XTransactionEvent} back into what actually changed
 * 
 * @author xamde
 */
public class Summarizer {

	/**
	 * Use the txn event and the change log to reconstruct what effectively
	 * changed.
	 * 
	 * Result is in {@link #getSummaryModel()}.
	 * 
	 * @param txnEvent
	 * @param changeLog just after the event was applied
	 * @param baseSnapshotModel @CanBeNull
	 */
	public static SummaryModel createTransactionSummary(XTransactionEvent txnEvent,
			XChangeLog changeLog, XReadableModel baseSnapshotModel) {
		XAddress modelAddress = XX.resolveModel(txnEvent.getChangedEntity());
		SummaryModel sm = new SummaryModel(modelAddress);
		applyTransactionEent(changeLog, baseSnapshotModel, txnEvent, sm);
		return sm;
	}

	private static void applyTransactionEent(XChangeLog changeLog,
			XReadableModel baseSnapshotModel, XTransactionEvent txnEvent, SummaryModel sm) {
		for (XAtomicEvent ae : txnEvent) {
			applyAtomicEvent(changeLog, baseSnapshotModel, ae, sm);
		}
	}

	private static void applyAtomicEvent(XChangeLog changeLog, XReadableModel baseSnapshotModel,
			XAtomicEvent ae, SummaryModel sm) {
		switch (ae.getTarget().getAddressedType()) {
		case XREPOSITORY: {
			sm.apply(ae);
		}
			break;
		case XMODEL: {
			SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			so.apply(ae);
		}
			break;
		case XOBJECT: {
			SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
			sf.apply(ae);
		}
			break;
		case XFIELD: {
			SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
			sf.apply(ae, changeLog, baseSnapshotModel);
		}
			break;
		default:
			break;

		}
	}

	/**
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull is only used if changeLog does not
	 *            start at 0
	 * @param first
	 * @param last
	 * @return
	 */
	public static SummaryModel createSummaryModel(XChangeLog changeLog,
			XReadableModel baseSnapshotModel, long first, long last) {
		XAddress modelAddress = changeLog.getBaseAddress();
		SummaryModel sm = new SummaryModel(modelAddress);

		Iterator<XEvent> events = changeLog.getEventsBetween(first, last + 1);
		while (events.hasNext()) {
			XEvent xe = events.next();
			if (xe instanceof XAtomicEvent) {
				applyAtomicEvent(changeLog, baseSnapshotModel, (XAtomicEvent) xe, sm);
			} else {
				assert xe instanceof XTransactionEvent;
				applyTransactionEent(changeLog, baseSnapshotModel, (XTransactionEvent) xe, sm);
			}
		}

		sm.removeNoOps();

		return sm;
	}

}
