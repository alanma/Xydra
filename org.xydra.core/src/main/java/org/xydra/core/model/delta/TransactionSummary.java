package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XAddress;
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
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XObject;
import org.xydra.core.util.DumpUtils;

/**
 * Transform an {@link XTransactionEvent} back into what actually changed
 * 
 * @author xamde
 */
public class TransactionSummary {

	public enum AtomicChangeType {
		Add, Change, Remove;

		@Override
		public String toString() {
			switch (this) {
			case Add:
				return "+";
			case Remove:
				return "-";
			case Change:
				return "~";
			default:
				throw new AssertionError();
			}
		}
	}

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

		private void apply(ChangeType changeType) {
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

	abstract class SummaryEntity {
		protected Change change = new Change();

		public void apply(XAtomicEvent ae) {
			this.change.apply(ae.getChangeType());
		}

	}

	public class SummaryModel extends SummaryEntity {
		private Map<XId, SummaryObject> map = new HashMap<XId, SummaryObject>();
		private XAddress modelAddress;

		public SummaryModel(XAddress modelAddress) {
			this.modelAddress = modelAddress;
		}

		public SummaryObject createOrGet(XId object) {
			SummaryObject so = this.map.get(object);
			if (so == null) {
				so = new SummaryObject();
				this.map.put(object, so);
			}
			return so;
		}

		/**
		 * @return all summaryObjects, including those that have only been
		 *         changed (i.e. have children that changed)
		 */
		public Iterator<Entry<XId, SummaryObject>> getChildren() {
			return this.map.entrySet().iterator();
		}

		public XAddress getAddress() {
			return this.modelAddress;
		}

		@Override
		public String toString() {
			return "M-" + this.change + "\n" + DumpUtils.toStringBuilder(this.map);
		}

	}

	public class SummaryObject extends SummaryEntity {
		private Map<XId, SummaryField> id2summaryField = new HashMap<XId, SummaryField>();

		public SummaryField createOrGet(XId field) {
			SummaryField sf = this.id2summaryField.get(field);
			if (sf == null) {
				sf = new SummaryField();
				this.id2summaryField.put(field, sf);
			}
			return sf;
		}

		Iterator<Entry<XId, SummaryField>> getChildren() {
			return this.id2summaryField.entrySet().iterator();
		}

		/**
		 * @param xo
		 * @param fieldId
		 * @param remove
		 * @return
		 */
		public XValue getFieldValue(XObject xo, XId fieldId, boolean remove) {
			SummaryField f = this.id2summaryField.get(fieldId);
			if (f != null && f.summaryValue != null) {
				return remove ? f.summaryValue.oldValue : f.summaryValue.newValue;
			}
			if (xo != null) {
				return xo.getFieldValue(fieldId);
			}
			return null;
		}

		@Override
		public String toString() {
			return "O-" + this.change + "\n" + DumpUtils.toStringBuilder(this.id2summaryField);
		}

		public AtomicChangeType getAtomichChangeType() {
			return this.change.getAtomicChangeType();
		}

		public SummaryField getSummaryField(XId fieldId) {
			return this.id2summaryField.get(fieldId);
		}

		public Set<Entry<XId, SummaryField>> getSummaryFields() {
			return this.id2summaryField.entrySet();
		}

	}

	public class SummaryField extends SummaryEntity {
		public SummaryValue summaryValue = null;

		public SummaryValue createOrGet() {
			if (this.summaryValue == null) {
				this.summaryValue = new SummaryValue();
			}
			return this.summaryValue;
		}

		@Override
		public String toString() {
			return "F-" + this.change + "\n" + this.summaryValue.toString();
		}

	}

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
				this.oldValue = getOldValue(xfe, changeLog, baseSnapshotModel);
				this.newValue = xfe.getNewValue();
				break;
			case REMOVE:
				this.oldValue = getOldValue(xfe, changeLog, baseSnapshotModel);
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
				XReadableObject baseSnapshotObject = baseSnapshotModel.getObject(fieldEvent
						.getObjectId());
				if (baseSnapshotObject == null)
					return null;
				XReadableField baseSnapshotField = baseSnapshotObject.getField(fieldEvent
						.getFieldId());
				if (baseSnapshotField == null)
					return null;
				return baseSnapshotField.getValue();
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
