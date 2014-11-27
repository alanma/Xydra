package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XReversibleFieldEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XObject;
import org.xydra.core.util.DumpUtils;
import org.xydra.index.iterator.IFilter;
import org.xydra.index.iterator.Iterators;

/**
 * Transform an {@link XTransactionEvent} back into what actually changed
 * 
 * @author xamde
 */
public class TransactionSummary {

	public class Change {
		/** range -1, 0, 1 */
		public int state = 0;

		public void add() {
			assert this.state <= 0;
			this.state++;
		}

		public void remove() {
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

		public void apply(ChangeType changeType) {
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
		public Change change = new Change();

		public void apply(XAtomicEvent ae) {
			this.change.apply(ae.getChangeType());
		}

	}

	public static <E extends SummaryEntity> Iterator<Map.Entry<XId, E>> filterAdded(
			Iterator<Map.Entry<XId, E>> it) {
		Iterator<Map.Entry<XId, E>> result = Iterators.filter(it, new IFilter<Map.Entry<XId, E>>() {

			@Override
			public boolean matches(Map.Entry<XId, E> entry) {
				return ((SummaryEntity) entry.getValue()).change.isAdded();
			}
		});
		return result;
	}

	public static <E extends SummaryEntity> Iterator<Map.Entry<XId, E>> filterRemoved(
			Iterator<Map.Entry<XId, E>> it) {
		Iterator<Map.Entry<XId, E>> result = Iterators.filter(it, new IFilter<Map.Entry<XId, E>>() {

			@Override
			public boolean matches(Map.Entry<XId, E> entry) {
				return ((SummaryEntity) entry.getValue()).change.isRemoved();
			}
		});
		return result;
	}

	public class SummaryModel extends SummaryEntity {
		public Map<XId, SummaryObject> map = new HashMap<>();
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

		public Iterator<Entry<XId, SummaryObject>> getAdded() {
			return filterAdded(getChildren());
		}

		public Iterator<Entry<XId, SummaryObject>> getRemoved() {
			return filterRemoved(getChildren());
		}

		public Iterator<Entry<XId, SummaryObject>> getChildren() {
			return this.map.entrySet().iterator();
		}

		public XAddress getAddress() {
			return this.modelAddress;
		}

		@Override
		public String toString() {
			return "M " + this.change + "\n" + DumpUtils.toStringBuilder(this.map);
		}

	}

	public class SummaryObject extends SummaryEntity {
		public Map<XId, SummaryField> map = new HashMap<>();

		public SummaryField createOrGet(XId field) {
			SummaryField sf = this.map.get(field);
			if (sf == null) {
				sf = new SummaryField();
				this.map.put(field, sf);
			}
			return sf;
		}

		public Iterator<Entry<XId, SummaryField>> getAdded() {
			return filterAdded(getChildren());
		}

		public Iterator<Entry<XId, SummaryField>> getRemoved() {
			return filterRemoved(getChildren());
		}

		Iterator<Entry<XId, SummaryField>> getChildren() {
			return this.map.entrySet().iterator();
		}

		/**
		 * @param xo
		 * @param fieldId
		 * @param remove
		 * @return
		 */
		public XValue getFieldValue(XObject xo, XId fieldId, boolean remove) {
			SummaryField f = this.map.get(fieldId);
			if (f != null && f.summaryValue != null) {
				return remove ? f.summaryValue.oldValue : f.summaryValue.newValue;
			}
			if (xo != null) {
				return xo.getFieldValue(fieldId);
			}
			return null;
		}

		// public SimpleObject toSimpleObject(SummaryModel sm, XId objectId) {
		// SimpleObject so = new SimpleObject(XX.resolveObject(sm.modelAddress,
		// objectId));
		//
		// if (this.change.isAdded()) {
		//
		// for (Entry<XId, SummaryField> entry : this.map.entrySet()) {
		// assert !entry.getValue().change.isRemoved();
		// XRevWritableField field = so.createField(entry.getKey());
		// SummaryField sf = entry.getValue();
		// field.setValue(sf.summaryValue.newValue);
		// }
		//
		// Iterator<Entry<XId, SummaryField>> addIt = getAdded();
		// while (addIt.hasNext()) {
		// Map.Entry<XId, SummaryField> entry = addIt.next();
		// XRevWritableField field = so.createField(entry.getKey());
		// SummaryField sf = entry.getValue();
		// field.setValue(sf.summaryValue.newValue);
		// }
		// } else if (this.change.isRemoved()) {
		// Iterator<Entry<XId, SummaryField>> addIt = getRemoved();
		// while (addIt.hasNext()) {
		// Map.Entry<XId, SummaryField> entry = addIt.next();
		// XRevWritableField field = so.createField(entry.getKey());
		// SummaryField sf = entry.getValue();
		// field.setValue(sf.summaryValue.oldValue);
		// }
		// }
		//
		// return so;
		// }

		@Override
		public String toString() {
			return "O " + this.change + "\n" + DumpUtils.toStringBuilder(this.map);
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
			return "F " + this.change + "\n" + this.summaryValue.toString();
		}

	}

	public class SummaryValue extends SummaryEntity {
		public XValue oldValue = null;
		public XValue newValue = null;

		public void apply(XAtomicEvent ae, XChangeLog changeLog) {
			this.change.apply(ae.getChangeType());
			XFieldEvent xfe = (XFieldEvent) ae;
			switch (ae.getChangeType()) {

			case ADD:
				this.newValue = xfe.getNewValue();
				break;
			case CHANGE:
				this.oldValue = getOldValue(xfe, changeLog);
				this.newValue = xfe.getNewValue();
				break;
			case REMOVE:
				this.oldValue = getOldValue(xfe, changeLog);
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
	 * @return
	 */
	public static XValue getOldValue(XFieldEvent fieldEvent, XChangeLog changeLog) {

		if (fieldEvent instanceof XReversibleFieldEvent) {
			XReversibleFieldEvent rfe = (XReversibleFieldEvent) fieldEvent;
			// we need the previous value here
			return rfe.getOldValue();
		} else {
			long revisionNumber = fieldEvent.getOldFieldRevision();
			XEvent oldEvent = changeLog.getEventAt(revisionNumber);
			if (oldEvent instanceof XFieldEvent) {
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

	public TransactionSummary(XTransactionEvent txnEvent, XChangeLog changeLog) {
		XAddress modelAddress = XX.resolveModel(txnEvent.getChangedEntity());
		this.sm = new SummaryModel(modelAddress);
		for (XAtomicEvent ae : txnEvent) {
			switch (ae.getTarget().getAddressedType()) {
			case XFIELD: {
				SummaryObject so = this.sm.createOrGet(ae.getChangedEntity().getObject());
				SummaryField sf = so.createOrGet(ae.getChangedEntity().getField());
				SummaryValue sv = sf.createOrGet();
				sv.apply(ae, changeLog);
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
			case XREPOSITORY:
				this.sm.apply(ae);
				break;
			default:
				break;

			}
		}
		// postProcess();
	}

	// /**
	// * If a neutral object has only added fields, the object is considered
	// * added. Vice versa for removed.
	// *
	// */
	// private void postProcess() {
	// for (Entry<XId, SummaryObject> oEntry : this.sm.map.entrySet()) {
	// SummaryObject so = oEntry.getValue();
	// if (so.change.isNeutral()) {
	// boolean addedField = false;
	// boolean removedField = false;
	// for (Entry<XId, SummaryField> fEntry : so.map.entrySet()) {
	// SummaryField sf = fEntry.getValue();
	// // update from children
	// if (sf.change.isNeutral()) {
	// SummaryField sv = fEntry.getValue();
	// sf.change.state = sv.change.state;
	// }
	// // process change
	// addedField |= sf.change.isAdded();
	// removedField |= sf.change.isRemoved();
	// }
	// // process change
	// if (addedField && !removedField) {
	// so.change.state = 1;
	// }
	// if (removedField && !addedField) {
	// so.change.state = -1;
	// }
	// }
	// }
	// }

	public SummaryModel getSummaryModel() {
		return this.sm;
	}

}
