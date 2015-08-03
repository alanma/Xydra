package org.xydra.core.model.delta;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XChangeLog;

public class OldValues {

	/**
	 * Returns the old value of a remove-event or change-event.
	 *
	 * @param fieldEvent
	 * @param changeLog
	 * @param baseSnapshotModel @CanBeNull is used if the changeLog does not
	 *            start at revision 0
	 * @return @CanBeNull
	 */
	public static XValue getOldValue(final XFieldEvent fieldEvent, final XChangeLog changeLog,
			final XReadableModel baseSnapshotModel) {
		assert fieldEvent.getChangeType() == ChangeType.REMOVE
				|| fieldEvent.getChangeType() == ChangeType.CHANGE;

		if (fieldEvent instanceof XReversibleFieldEvent) {
			final XReversibleFieldEvent rfe = (XReversibleFieldEvent) fieldEvent;
			// we need the previous value here
			return rfe.getOldValue();
		} else {
			final long revisionNumber = fieldEvent.getOldFieldRevision();
			final XEvent oldEvent = changeLog.getEventAt(revisionNumber);
			if (oldEvent == null) {
				// read from last known snapshot
				if (baseSnapshotModel == null) {
					return null;
				}
				final XId objectId = fieldEvent.getObjectId();
				final XReadableObject baseSnapshotObject = baseSnapshotModel.getObject(objectId);
				if (baseSnapshotObject == null) {
					return null;
				}
				final XReadableField baseSnapshotField = baseSnapshotObject.getField(fieldEvent
						.getFieldId());
				if (baseSnapshotField == null) {
					return null;
				}
				final XValue value = baseSnapshotField.getValue();
				return value;
			} else if (oldEvent instanceof XFieldEvent) {
				final XFieldEvent oldFieldEvent = (XFieldEvent) oldEvent;
				return oldFieldEvent.getNewValue();
			} else if (oldEvent instanceof XTransactionEvent) {
				final XTransactionEvent xte = (XTransactionEvent) oldEvent;
				// find right atomic event
				for (final XAtomicEvent ae : xte) {
					if (ae instanceof XFieldEvent
							&& ae.getChangedEntity().equals(fieldEvent.getChangedEntity())) {
						// we found it
						final XFieldEvent oldFieldEvent = (XFieldEvent) ae;
						return oldFieldEvent.getNewValue();
					}
				}
				return null;
			} else {
				return null;
			}
		}
	}

}
