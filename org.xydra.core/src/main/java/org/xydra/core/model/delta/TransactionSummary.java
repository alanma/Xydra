package org.xydra.core.model.delta;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;

/**
 * Transform an {@link XTransactionEvent} back into what actually changed
 * 
 * @author xamde
 */
public class TransactionSummary {

	/**
	 * @param fieldEvent
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull
	 * @return
	 */
	public static XValue getOldValue(XFieldEvent fieldEvent, XChangeLog changeLog,
			XReadableModel baseSnapshotModel) {

		if (fieldEvent instanceof XReversibleFieldEvent) {
			XReversibleFieldEvent rfe = (XReversibleFieldEvent) fieldEvent;
			// we need the previous value here
			return rfe.getOldValue();
		} else {
			long revisionNumber = fieldEvent.getOldFieldRevision();
			XEvent oldEvent = changeLog.getEventAt(revisionNumber);
			if (oldEvent == null) {
				// read from last known snapshot

				if (baseSnapshotModel == null) {
					return null;
				}
				XId objectId = fieldEvent.getObjectId();
				XReadableObject baseSnapshotObject = baseSnapshotModel.getObject(objectId);
				if (baseSnapshotObject == null)
					return null;
				XReadableField baseSnapshotField = baseSnapshotObject.getField(fieldEvent
						.getFieldId());
				if (baseSnapshotField == null)
					return null;
				XValue value = baseSnapshotField.getValue();
				return value;
			} else if (oldEvent instanceof XFieldEvent) {
				XFieldEvent oldFieldEvent = (XFieldEvent) oldEvent;
				return oldFieldEvent.getNewValue();
			} else if (oldEvent instanceof XTransactionEvent) {
				XTransactionEvent xte = (XTransactionEvent) oldEvent;
				// find right atomic event
				for (XAtomicEvent ae : xte) {
					if (ae instanceof XFieldEvent
							&& ae.getChangedEntity().equals(fieldEvent.getChangedEntity())) {
						// we found it
						XFieldEvent oldFieldEvent = (XFieldEvent) ae;
						return oldFieldEvent.getNewValue();
					}
				}
				return null;
			} else {
				return null;
			}
		}
	}

	private SummaryModel sm;

	/**
	 * Use the txn event and the change log to reconstruct what changed.
	 * 
	 * Result is in {@link #getSummaryModel()}.
	 * 
	 * @param txnEvent
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull
	 */
	public TransactionSummary(XTransactionEvent txnEvent, XChangeLog changeLog,
			XReadableModel baseSnapshotModel) {
		XAddress modelAddress = XX.resolveModel(txnEvent.getChangedEntity());
		this.sm = new SummaryModel(modelAddress);
		for (XAtomicEvent ae : txnEvent) {
			switch (ae.getTarget().getAddressedType()) {
			case XREPOSITORY: {
				this.sm.apply(ae);
			}
				break;
			case XMODEL: {
				SummaryObject so = this.sm.createOrGet(ae.getChangedEntity().getObject());
				so.apply(ae);
			}
				break;
			case XOBJECT: {
				SummaryObject so = this.sm.createOrGet(ae.getChangedEntity().getObject());
				SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
				sf.apply(ae);
			}
				break;
			case XFIELD: {
				SummaryObject so = this.sm.createOrGet(ae.getChangedEntity().getObject());
				SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
				SummaryValue sv = sf.createOrGet();
				sv.apply(ae, changeLog, baseSnapshotModel);
			}
				break;
			default:
				break;

			}
		}
	}

	public SummaryModel getSummaryModel() {
		return this.sm;
	}

}
