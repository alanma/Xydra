package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.impl.memory.MemoryModelEvent;
import org.xydra.base.change.impl.memory.MemoryObjectEvent;
import org.xydra.base.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.base.value.XValue;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.index.query.Pair;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.GaeUtils2;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SText;
import org.xydra.xgae.util.AsyncEntity;

/**
 * Code to handle saving and loading of XEvents and XValues stored on the GAE
 * datastore.
 *
 * Events are stored as properties in the XCHANGE GAE entities normally managed
 * by {@link GaeChange}. Only for {@link XFieldEvent XFieldEvents},
 * {@link XValue XValues} whose XML-encoding is longer than
 * {@link #MAX_VALUE_SIZE} are stored in their own GAE entity.
 *
 * The values stored for the events may also be referenced by the internal field
 * state by the revision number that set the value and a special index
 * indicating where the value is stored (as there could have been multiple
 * events in a transaction). A value of {@link #TRANSINDEX_NONE} indicates that
 * there is no value (null), a positive value (including 0) indicates that the
 * value is stored in it's own GAE entity and the value can be used with
 * {@link KeyStructure#createValueKey(XAddress, long, int)} to get the key for
 * that entity. All other values can be passed to
 * {@link #getInternalValueId(int)} to get an index into the values (
 * {@link #PROP_EVENT_VALUES}) stored in the XCHANGE entity.
 *
 * Encoding on AppEngine:
 *
 * <pre>
 * Int - types
 * Str - targets
 * Text value = value | marker -> value is stored as its own entity
 * Long - object revision
 * Long - field revision
 * Bool - implied or not
 *
 * revNr + index in List
 * </pre>
 *
 * @author dscharrer
 *
 */
public class GaeEvents {

	/**
	 * Enumeration to map between stored event type and {@link XEvent} types.
	 */
	private enum EventType {

		AddModel(1), RemoveModel(2), AddObject(3), RemoveObject(4), AddField(5), RemoveField(6), AddValue(
				7), ChangeValue(8), RemoveValue(9);

		public final int id;

		private EventType(final int id) {
			this.id = id;
		}

		static EventType get(final int id) {
			switch (id) {
			case 1:
				return AddModel;
			case 2:
				return RemoveModel;
			case 3:
				return AddObject;
			case 4:
				return RemoveObject;
			case 5:
				return AddField;
			case 6:
				return RemoveField;
			case 7:
				return AddValue;
			case 8:
				return ChangeValue;
			case 9:
				return RemoveValue;
			default:
				return null;
			}
		}

		public ChangeType getChangeType() {
			switch (this) {
			case AddField:
				return ChangeType.ADD;
			case AddModel:
				return ChangeType.ADD;
			case AddObject:
				return ChangeType.ADD;
			case AddValue:
				return ChangeType.ADD;
			case ChangeValue:
				return ChangeType.CHANGE;
			case RemoveField:
				return ChangeType.REMOVE;
			case RemoveModel:
				return ChangeType.REMOVE;
			case RemoveObject:
				return ChangeType.REMOVE;
			case RemoveValue:
				return ChangeType.REMOVE;
			default:
				assert false;
				return null;
			}
		}

		public XType getTargetType() {
			switch (this) {
			case AddField:
				return XType.XOBJECT;
			case AddModel:
				return XType.XREPOSITORY;
			case AddObject:
				return XType.XMODEL;
			case AddValue:
				return XType.XFIELD;
			case ChangeValue:
				return XType.XFIELD;
			case RemoveField:
				return XType.XOBJECT;
			case RemoveModel:
				return XType.XREPOSITORY;
			case RemoveObject:
				return XType.XMODEL;
			case RemoveValue:
				return XType.XFIELD;
			default:
				assert false;
				return null;
			}
		}

		public static EventType get(final XType entity, final ChangeType change) {
			assert change != ChangeType.TRANSACTION;
			switch (entity) {
			case XREPOSITORY:
				if (change == ChangeType.ADD) {
					return AddModel;
				} else {
					assert change == ChangeType.REMOVE;
					return RemoveModel;
				}
			case XMODEL:
				if (change == ChangeType.ADD) {
					return AddObject;
				} else {
					assert change == ChangeType.REMOVE;
					return RemoveObject;
				}
			case XOBJECT:
				if (change == ChangeType.ADD) {
					return AddField;
				} else {
					assert change == ChangeType.REMOVE;
					return RemoveField;
				}
			case XFIELD:
				switch (change) {
				case ADD:
					return AddValue;
				case CHANGE:
					return ChangeValue;
				case REMOVE:
					return RemoveValue;
				default:
					assert false;
					return null;
				}
			default:
				assert false;
				return null;
			}
		}
	}

	// Properties stored in the "change" GAE entity.
	private static final String PROP_EVENT_TYPES = "eventTypes";
	private static final String PROP_EVENT_TARGETS = "eventTargets";
	private static final String PROP_EVENT_VALUES = "eventValues";
	private static final String PROP_EVENT_REVS_OBJECT = "objectRevisions";
	private static final String PROP_EVENT_REVS_FIELD = "fieldRevisions";
	private static final String PROP_EVENT_IMPLIED = "eventIsImplied";

	// Value for PROP_EVENT_VALUES if the XValue is stored externally.
	private static final SText VALUE_EXTERN = XGae.get().datastore().createText("extern");

	// Properties stored in the "value" GAE entity.
	private static final String PROP_VALUE = "value";

	// Maximum size for XML-encoded XValues to store in change entities.
	/*
	 * TODO should we set this to 500 (the GAE limit for String properties) so
	 * we can use a List<String> for PROP_EVENT_VALUES?
	 */
	private static final int MAX_VALUE_SIZE = 1024;

	// Parameter for getValue() to represent a null XValue.
	public static final int TRANSINDEX_NONE = -1;

	/**
	 * A reference to a (possibly asynchronously loaded) {@link XValue} stored
	 * on the GAE datastore.
	 */
	public static class AsyncValue {

		private final AsyncEntity future;
		private final int transIndex;
		private XValue value;

		/**
		 * Load a value asynchronously.
		 *
		 * @param future
		 *            The entity containing the value.
		 * @param transIndex
		 *            The index of the value in the entity. See
		 *            {@link GaeEvents#getValue(XAddress, long, int)}
		 */
		private AsyncValue(final AsyncEntity future, final int transIndex) {
			this.future = future;
			this.transIndex = transIndex;
			assert transIndex != TRANSINDEX_NONE;
		}

		/**
		 * Construct with an already-loaded value.
		 */
		protected AsyncValue(final XValue value) {
			this.value = value;
			this.future = null;
			this.transIndex = TRANSINDEX_NONE;
		}

		public XValue get() {

			if (this.value == null && this.transIndex != TRANSINDEX_NONE) {

				final SEntity eventEntity = this.future.get();
				if (eventEntity == null) {
					return null;
				}

				SText eventXml;
				if (this.transIndex < 0) {
					final int realindex = getInternalValueId(this.transIndex);
					@SuppressWarnings("unchecked")
					final
					List<SText> eventValues = (List<SText>) eventEntity
							.getAttribute(PROP_EVENT_VALUES);
					if (eventValues == null || realindex >= eventValues.size()) {
						return null;
					}
					eventXml = eventValues.get(realindex);
					if (eventXml == null) {
						return null;
					}
				} else {
					eventXml = (SText) eventEntity.getAttribute(PROP_VALUE);
				}

				final XydraElement eventElement = new XmlParser().parse(eventXml.getValue());

				this.value = SerializedValue.toValue(eventElement);

				assert this.value != null;
			}

			return this.value;
		}

	}

	private static final AsyncValue VALUE_NULL = new AsyncValue(null);

	/**
	 * @param revisionNumber
	 *            The revision number of the change the event is part of.
	 * @param transindex
	 *            The index of the event in the change as returned by
	 *            {@link #saveEvents(XAddress, Entity, List)} or
	 *            {@link #loadAtomicEvents(XAddress, long, XId, Entity, boolean)}
	 * @return Never null but {@link AsyncValue#get()} may return null.
	 */
	protected static AsyncValue getValue(final XAddress modelAddr, final long revisionNumber, final int transindex) {

		if (transindex == TRANSINDEX_NONE) {
			return VALUE_NULL;
		}

		if (transindex < TRANSINDEX_NONE) {
			final SKey changeKey = KeyStructure.createChangeKey(modelAddr, revisionNumber);
			return new AsyncValue(GaeUtils2.getEntityFromMemcacheAndAsyncDatatore(changeKey),
					transindex);
		} else {
			return getExternalValue(modelAddr, revisionNumber, transindex);
		}
	}

	private static AsyncValue getExternalValue(final XAddress modelAddr, final long revisionNumber,
			final int transindex) {
		final SKey valueKey = KeyStructure.createValueKey(modelAddr, revisionNumber, transindex);
		return new AsyncValue(GaeUtils2.getEntityFromMemcacheAndAsyncDatatore(valueKey), transindex);
	}

	/**
	 * Save the given events in the given change entity.
	 *
	 * The changeEntity is not actually put in the datastore by this method,
	 * only properties are set. Other additional GAE entities might be saved to
	 * the datastore though.
	 *
	 * @return a list of indices that can be used later to load XValues using
	 *         {@link #getValue(XAddress, long, int)}, as well as a list of GAE
	 *         entities that have been saved asynchronously.
	 */
	protected static Pair<int[], List<Future<SKey>>> saveEvents(final XAddress modelAddr,
			final SEntity changeEntity, final List<XAtomicEvent> events) {

		final List<Integer> types = new ArrayList<Integer>();
		final List<String> targets = new ArrayList<String>();
		final List<SText> values = new ArrayList<SText>();
		final List<Long> objectRevs = new ArrayList<Long>();
		final List<Long> fieldRevs = new ArrayList<Long>();
		final List<Boolean> implied = new ArrayList<Boolean>();

		final int[] valueIds = new int[events.size()];

		final List<Future<SKey>> futures = new ArrayList<Future<SKey>>();

		for (int i = 0; i < events.size(); i++) {
			final XAtomicEvent ae = events.get(i);

			XyAssert.xyAssert(
					events.size() == 1 || ae.inTransaction(),
					"Multiple events should be in a txn. Events:" + events.size() + " inTxn?"
							+ ae.inTransaction() + " event: %s", ae);

			// TODO enable stricter test? disallow txns with a single event?
			// XyAssert.xyAssert((events.size() == 1) ^ ae.inTransaction(),
			// "Multiple events should be in a txn and single events should not be in a txn. Events:"
			// + events.size() + " inTxn?" + ae.inTransaction() + " event: %s",
			// ae);

			types.add(EventType.get(ae.getTarget().getAddressedType(), ae.getChangeType()).id);
			targets.add(ae.getTarget().toString());
			implied.add(ae.isImplied());

			if (ae instanceof XRepositoryEvent) {
				// nothing to set;
				values.add(XGae.get().datastore()
						.createText(((XRepositoryEvent) ae).getModelId().toString()));
			} else if (ae instanceof XModelEvent) {
				objectRevs.add(ae.getOldObjectRevision());
				values.add(XGae.get().datastore()
						.createText(((XModelEvent) ae).getObjectId().toString()));
			} else if (ae instanceof XObjectEvent) {
				objectRevs.add(ae.getOldObjectRevision());
				fieldRevs.add(ae.getOldFieldRevision());
				values.add(XGae.get().datastore()
						.createText(((XObjectEvent) ae).getFieldId().toString()));
			} else {
				assert ae instanceof XFieldEvent;
				objectRevs.add(ae.getOldObjectRevision());
				fieldRevs.add(ae.getOldFieldRevision());

				final XValue xv = ((XFieldEvent) ae).getNewValue();
				if (xv == null) {
					values.add(null);
					valueIds[i] = TRANSINDEX_NONE;
				} else {
					final XydraOut out = new XmlOut(false);
					SerializedValue.serialize(xv, out);
					final String valueStr = out.getData();
					final SText value = XGae.get().datastore().createText(valueStr);

					if (valueStr.length() > MAX_VALUE_SIZE) {
						final SKey k = KeyStructure.createValueKey(modelAddr,
								ae.getOldModelRevision() + 1, i);
						final SEntity e = XGae.get().datastore().createEntity(k);
						e.setAttribute(PROP_VALUE, value);
						futures.add(XGae.get().datastore().async().putEntity(e));
						values.add(VALUE_EXTERN);
						valueIds[i] = i;
					} else {
						assert values.size() == i;
						values.add(value);
						valueIds[i] = getInternalValueId(i);
					}
				}
			}

		}

		changeEntity.setAttribute(PROP_EVENT_TYPES, types);
		changeEntity.setAttribute(PROP_EVENT_TARGETS, targets);
		changeEntity.setAttribute(PROP_EVENT_VALUES, values);
		if (!objectRevs.isEmpty()) {
			changeEntity.setAttribute(PROP_EVENT_REVS_OBJECT, objectRevs);
		}
		if (!fieldRevs.isEmpty()) {
			changeEntity.setAttribute(PROP_EVENT_REVS_FIELD, fieldRevs);
		}
		changeEntity.setAttribute(PROP_EVENT_IMPLIED, implied);

		return new Pair<int[], List<Future<SKey>>>(valueIds, futures);
	}

	/**
	 * @return the parameter for {@link #getValue(XAddress, long, int)} to
	 *         retrieve the value for the i-th atomic event that is stored in
	 *         the change entity itself.
	 */
	private static int getInternalValueId(final int i) {
		return TRANSINDEX_NONE - 1 - i;
	}

	/**
	 * Load the individual events associated with the given change.
	 *
	 * This method should only be called if the change entity actually contains
	 * events.
	 *
	 * @param change
	 *            The change whose events should be loaded.
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	@SuppressWarnings("unchecked")
	protected static Pair<XAtomicEvent[], int[]> loadAtomicEvents(final XAddress modelAddr, final long rev,
			final XId actor, final SEntity changeEntity) {

		/*
		 * Load the event properties that were set in saveEvents().
		 *
		 * Be careful with types, as GAE might not return the exact same type
		 * that was set.
		 */
		final List<Number> types = (List<Number>) changeEntity.getAttribute(PROP_EVENT_TYPES);
		final List<String> targets = (List<String>) changeEntity.getAttribute(PROP_EVENT_TARGETS);
		final List<SText> values = (List<SText>) changeEntity.getAttribute(PROP_EVENT_VALUES);
		final List<Number> objectRevs = (List<Number>) changeEntity.getAttribute(PROP_EVENT_REVS_OBJECT);
		final List<Number> fieldRevs = (List<Number>) changeEntity.getAttribute(PROP_EVENT_REVS_FIELD);
		final List<Boolean> implied = (List<Boolean>) changeEntity.getAttribute(PROP_EVENT_IMPLIED);

		XyAssert.xyAssert(types != null, "changeEntity.PROP_EVENT_TYPES was null");
		assert types != null && targets != null && values != null && implied != null;

		final XAtomicEvent[] events = new XAtomicEvent[types.size()];

		assert targets.size() == events.length && implied.size() == events.length
				&& values.size() == events.length;

		final int[] valueIds = new int[events.length];

		final boolean inTrans = events.length > 1;

		int ori = 0, fri = 0;

		final long modelRev = rev - 1;

		for (int i = 0; i < events.length; i++) {
			final EventType type = EventType.get(types.get(i).intValue());

			final XAddress target = Base.toAddress(targets.get(i));
			final boolean isImplied = implied.get(i);

			switch (type.getTargetType()) {
			case XREPOSITORY: {
				final XId modelId = Base.toId(values.get(i).getValue());
				switch (type.getChangeType()) {
				case ADD:
					events[i] = MemoryRepositoryEvent.createAddEvent(actor, target, modelId,
							modelRev, inTrans);
					break;
				case REMOVE:
					events[i] = MemoryRepositoryEvent.createRemoveEvent(actor, target, modelId,
							modelRev, inTrans);
					break;
				default:
					assert false;
				}
				break;
			}
			case XMODEL: {
				final XId objectId = Base.toId(values.get(i).getValue());
				final long objectRev = objectRevs.get(ori++).longValue();
				switch (type.getChangeType()) {
				case ADD:
					events[i] = MemoryModelEvent.createAddEvent(actor, target, objectId, modelRev,
							inTrans);
					break;
				case REMOVE:
					events[i] = MemoryModelEvent.createRemoveEvent(actor, target, objectId,
							modelRev, objectRev, inTrans, isImplied);
					break;
				default:
					assert false;
				}
				break;
			}
			case XOBJECT: {
				final XId fieldId = Base.toId(values.get(i).getValue());
				final long objectRev = objectRevs.get(ori++).longValue();
				final long fieldRev = fieldRevs.get(fri++).longValue();
				switch (type.getChangeType()) {
				case ADD:
					events[i] = MemoryObjectEvent.createAddEvent(actor, target, fieldId, modelRev,
							objectRev, inTrans);
					break;
				case REMOVE:
					events[i] = MemoryObjectEvent.createRemoveEvent(actor, target, fieldId,
							modelRev, objectRev, fieldRev, inTrans, isImplied);
					break;
				default:
					assert false;
				}
				break;
			}
			case XFIELD: {
				final SText valueTxt = values.get(i);
				final long objectRev = objectRevs.get(ori++).longValue();
				final long fieldRev = fieldRevs.get(fri++).longValue();
				AsyncValue value;
				if (valueTxt == null) {
					assert type.getChangeType() == ChangeType.REMOVE;
					value = VALUE_NULL;
					valueIds[i] = TRANSINDEX_NONE;
				} else {
					assert type.getChangeType() != ChangeType.REMOVE;
					final boolean isExtern = VALUE_EXTERN.equals(valueTxt);
					if (!isExtern) {
						final String valueXml = valueTxt.getValue();
						final XydraElement eventElement = new XmlParser().parse(valueXml);
						value = new AsyncValue(SerializedValue.toValue(eventElement));
						valueIds[i] = getInternalValueId(i);
					} else {
						value = getExternalValue(modelAddr, rev, i);
						valueIds[i] = i;
					}
				}
				assert value != null;
				events[i] = new GaeFieldEvent(actor, target, value, type.getChangeType(), modelRev,
						objectRev, fieldRev, inTrans, isImplied);
			}

			}
		}

		return new Pair<XAtomicEvent[], int[]>(events, valueIds);
	}

	static int getEventIndex(final int transindex) {
		return transindex < TRANSINDEX_NONE ? getInternalValueId(transindex) : transindex;
	}

}
