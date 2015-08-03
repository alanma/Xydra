package org.xydra.core.model.delta;

import java.util.Iterator;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableModel;
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
	public static SummaryModel createTransactionSummary(final XTransactionEvent txnEvent,
			final XChangeLog changeLog, final XReadableModel baseSnapshotModel) {
		final XAddress modelAddress = Base.resolveModel(txnEvent.getChangedEntity());
		final SummaryModel sm = new SummaryModel(modelAddress);
		applyTransactionEent(changeLog, baseSnapshotModel, txnEvent, sm);
		return sm;
	}

	private static void applyTransactionEent(final XChangeLog changeLog,
			final XReadableModel baseSnapshotModel, final XTransactionEvent txnEvent, final SummaryModel sm) {
		for (final XAtomicEvent ae : txnEvent) {
			applyAtomicEvent(changeLog, baseSnapshotModel, ae, sm);
		}
	}

	private static void applyAtomicEvent(final XChangeLog changeLog, final XReadableModel baseSnapshotModel,
			final XAtomicEvent ae, final SummaryModel sm) {
		switch (ae.getTarget().getAddressedType()) {
		case XREPOSITORY: {
			sm.apply(ae);
		}
			break;
		case XMODEL: {
			final SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			so.apply(ae);
		}
			break;
		case XOBJECT: {
			final SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			final SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
			sf.apply(ae);
		}
			break;
		case XFIELD: {
			final SummaryObject so = sm.createOrGet(ae.getChangedEntity().getObject());
			final SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
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
	public static SummaryModel createSummaryModel(final XChangeLog changeLog,
			final XReadableModel baseSnapshotModel, final long first, final long last) {
		final XAddress modelAddress = changeLog.getBaseAddress();
		final SummaryModel sm = new SummaryModel(modelAddress);

		final Iterator<XEvent> events = changeLog.getEventsBetween(first, last + 1);
		while (events.hasNext()) {
			final XEvent xe = events.next();
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
