package org.xydra.core.change;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.delta.IFieldDiff;
import org.xydra.core.model.delta.IModelDiff;
import org.xydra.core.model.delta.IObjectDiff;
import org.xydra.core.util.DumpUtils;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.ITransformer;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * Read and write caching. All once read objects are not read again. All once
 * read fields are not read again. This {@link SessionCachedModel} read-caches
 * existent and non-existent requests. Allows write changes of all kinds, which
 * are kept in an internal buffer. This internal buffer is used to answer
 * queries.
 *
 * Unable to report correct revision numbers. Instead, the constant
 * {@value #UNDEFINED} ( {@link #UNDEFINED}) is returned.
 *
 * A SessionModels state looks like this: <li>knowsAllObjects?</li> <li>For each
 * object: one {@link EntityState} or Unknown</li>.
 *
 * The SessionModel gets to know its state only via
 * {@link #indexModel(XReadableModel)} and {@link #indexObject(XReadableObject)}
 * .
 *
 * @author xamde
 */
public class SessionCachedModel implements XWritableModel, IModelDiff {

	private long rev = UNDEFINED;

	private static class CachedEntity {
		private final XAddress address;
		protected EntityState state;

		public CachedEntity(final XAddress address, final EntityState state) {
			XyAssert.xyAssert(address != null);
			assert address != null;
			XyAssert.xyAssert(state != null);
			assert state != null;
			this.address = address;
			this.state = state;
		}

		public XAddress getAddress() {
			return this.address;
		}

		protected long rev = UNDEFINED;

		public long getRevisionNumber() {
			return this.rev;
		}

		public boolean isAdded() {
			return this.state == EntityState.Added;
		}

		public boolean isPresent() {
			return this.state.isPresent();
		}

		public boolean isRemoved() {
			return this.state == EntityState.Removed;
		}

	}

	private static class CachedField extends CachedEntity implements XWritableField, IFieldDiff {

		private XValue current;
		private XValue initial;

		public CachedField(final XAddress address, final EntityState state, final XValue initialValue) {
			super(address, state);
			this.initial = initialValue;
			this.current = this.initial;
		}

		@Override
		public XId getId() {
			return super.address.getField();
		}

		@Override
		public XValue getInitialValue() {
			return this.initial;
		}

		@Override
		public XType getType() {
			return XType.XFIELD;
		}

		@Override
		public XValue getValue() {
			return this.current;
		}

		@Override
		public boolean isChanged() {
			return this.state.isChanged() || isValueChanged();
		}

		@Override
		public boolean isEmpty() {
			return this.current == null;
		}

		public boolean isValueAdded() {
			return this.initial == null && this.current != null;
		}

		public boolean isValueChanged() {
			return this.initial == null && this.current != null
					|| this.initial != null && this.current == null
					|| this.initial != null && !this.initial.equals(this.current);
		}

		public boolean isValueRemoved() {
			return this.initial != null && this.current == null;
		}

		@Override
		public boolean setValue(final XValue value) {
			this.current = value;
			return isValueChanged();
		}

		@Override
		public String toString() {
			return "F:" + getId() + " {\n" + DumpUtilsBase.toStringBuffer(this) + "Changes: \n"
					+ DumpUtils.changesToString(this).toString() + "}";
		}

		public void discardChanges() {
			this.current = this.initial;
		}

	}

	private static class CachedObject extends CachedEntity implements XWritableObject, IObjectDiff {
		/** FieldId -> CachedObject */
		private final Map<XId, CachedField> cachedFields;

		public CachedObject(final XAddress address, final EntityState state) {
			super(address, state);
			this.cachedFields = new HashMap<XId, SessionCachedModel.CachedField>(2);
		}

		@Override
		public CachedField createField(final XId fieldId) {
			// first, consult caches
			final CachedField cf = this.cachedFields.get(fieldId);
			if (cf != null) {
				// create only if possible
				if (cf.state == EntityState.NotPresent) {
					cf.state = EntityState.Added;
				}
				if (cf.state == EntityState.Removed) {
					cf.state = EntityState.Present;
				}
				return cf;
			}

			// not known, might be present in base, record as added anyway
			return setFieldState(fieldId, EntityState.Added, null);
		}

		@Override
		public Collection<? extends XReadableField> getAdded() {
			final List<XReadableField> list = new LinkedList<XReadableField>();
			for (final CachedField cf : this.cachedFields.values()) {
				if (cf.isAdded()) {
					list.add(cf);
				}
			}
			return list;
		}

		@SuppressWarnings("unused")
		@Override
		public CachedField getField(final XId fieldId) {
			final CachedField cf = this.cachedFields.get(fieldId);
			if (WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && cf == null) {
				log.warn("Field '" + fieldId + "' not prefetched in " + getAddress()
						+ ". Return getField=null.");
			}
			if (cf != null && !cf.isPresent()) {
				return null;
			}
			return cf;
		}

		@Override
		public XId getId() {
			return super.address.getObject();
		}

		@Override
		public Collection<? extends IFieldDiff> getPotentiallyChanged() {
			final List<IFieldDiff> list = new LinkedList<IFieldDiff>();
			for (final CachedField cf : this.cachedFields.values()) {
				if (cf.isChanged() && !cf.isAdded() && !cf.isRemoved()) {
					list.add(cf);
				}
			}
			return list;
		}

		@Override
		public Collection<XId> getRemoved() {
			final List<XId> list = new LinkedList<XId>();
			for (final CachedField cf : this.cachedFields.values()) {
				if (cf.isRemoved()) {
					list.add(cf.getId());
				}
			}
			return list;
		}

		@Override
		public XType getType() {
			return XType.XOBJECT;
		}

		@Override
		public boolean hasChanges() {
			if (this.state.isChanged()) {
				return true;
			}
			for (final CachedField cf : this.cachedFields.values()) {
				if (cf.isChanged()) {
					return true;
				}
			}
			return false;
		}

		@SuppressWarnings("unused")
		@Override
		public boolean hasField(final XId fieldId) {
			final CachedField cf = this.cachedFields.get(fieldId);
			if (cf != null) {
				return cf.isPresent();
			}
			if (WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && cf == null) {
				log.warn("Field '" + fieldId + "' not prefetched in " + getAddress()
						+ ". Return hasField=false.");
			}
			return false;
		}

		/**
		 * Does not overwrite already changed fields
		 *
		 * @param baseObject base object
		 */
		public void indexFieldsFrom(final XReadableObject baseObject) {
			for (final XId fieldId : baseObject) {
				CachedField cf = this.cachedFields.get(fieldId);
				final XReadableField baseField = baseObject.getField(fieldId);
				final XValue baseValue = baseField.getValue();
				if (cf == null) {
					cf = setFieldState(fieldId, EntityState.Present, baseValue);
				} else {
					if (!cf.isChanged()) {
						cf.state = EntityState.Present;
						cf.setValue(baseValue);
					}
				}
				cf.rev = baseField.getRevisionNumber();
			}
		}

		@Override
		public boolean isEmpty() {
			for (final CachedField cf : this.cachedFields.values()) {
				if (cf.isPresent()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Iterator<XId> iterator() {
			// return all Added + Present fields only
			return new TransformingIterator<Map.Entry<XId, CachedField>, XId>(

			new AbstractFilteringIterator<Map.Entry<XId, CachedField>>(this.cachedFields.entrySet()
					.iterator()) {
				@Override
				protected boolean matchesFilter(final Map.Entry<XId, CachedField> entry) {
					return entry.getValue().isPresent();
				}
			}, new ITransformer<Map.Entry<XId, CachedField>, XId>() {

				@Override
				public XId transform(final Map.Entry<XId, CachedField> entry) {
					return entry.getKey();
				}
			});
		}

		@SuppressWarnings("unused")
		@Override
		public boolean removeField(final XId fieldId) {
			final CachedField cf = this.cachedFields.get(fieldId);
			if (cf == null) {
				setFieldState(fieldId, EntityState.Removed, null);
				if (WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && WARN_ON_REMOVES) {
					log.warn("Field '" + fieldId + "' not prefetched in " + getAddress()
							+ ". Removing anyway.");
				}
				return false;
			} else {
				final boolean b = cf.isPresent();
				cf.state = EntityState.Removed;
				return b;
			}
		}

		private CachedField setFieldState(final XId fieldId, final EntityState state, final XValue initialValue) {
			final CachedField cf = new CachedField(Base.resolveField(getAddress(), fieldId), state,
					initialValue);
			this.cachedFields.put(fieldId, cf);
			return cf;
		}

		@Override
		public String toString() {
			return "O:" + getId() + " {\n" + DumpUtilsBase.toStringBuffer(this) + "Changes: \n"
					+ DumpUtils.changesToString(this).toString() + "}";
		}

		public void discardChanges() {
			final Set<XId> fieldsToDelete = new HashSet<XId>();

			final Iterator<XId> iterator = this.cachedFields.keySet().iterator();
			while (iterator.hasNext()) {
				final XId key = iterator.next();
				final CachedField cf = this.cachedFields.get(key);
				if (cf.isAdded()) {
					fieldsToDelete.add(key);
				} else if (cf.isRemoved() || cf.isChanged()) {
					if (cf.isRemoved()) {
						if (cf.getRevisionNumber() < 0) {
							fieldsToDelete.add(key);
						} else {
							cf.state = EntityState.Present;
						}
					}
				}

				cf.discardChanges();
			}

			for (final XId xid : fieldsToDelete) {
				this.cachedFields.remove(xid);
			}
		}

	}

	/**
	 * The state a {@link CachedObject} or {@link CachedField} can be in
	 */
	private static enum EntityState {

		/**
		 * Added. Not present in base model; added later.
		 *
		 * Object: Implies: knows all fields. All fields can only be: Added, Not
		 * present. Object has a number of fields.
		 *
		 * Field: Value can be: Added, Not present
		 */
		Added,

		/**
		 * Not present in base model and not added.
		 *
		 * Object: Implies: There are 0 fields.
		 *
		 * Field: Implies: Value not present.
		 */
		NotPresent,

		/**
		 * Present. Was present in base model and has not been removed.
		 *
		 * Object: (IMPROVE Distinguish: knows all fields or not). This state
		 * includes changed objects, in which only fields or values changed. Has
		 * a number of fields.
		 *
		 * Field: Value can be: Present, Added, Changed, Removed, Not present
		 */
		Present,

		/**
		 * Removed. Was present in base model; removed later.
		 *
		 * Object: Implies: There are 0 fields.
		 *
		 * Field: Implies: Value not present.
		 */
		Removed;

		public EntityState afterSuccesfullCommit() {
			switch (this) {
			case Added:
				return EntityState.Present;
			case NotPresent:
			case Present:
				return this;
			case Removed:
				return EntityState.NotPresent;
			}
			XyAssert.xyAssert(false);
			return null;
		}

		public boolean isChanged() {
			return this == Added || this == Removed;
		}

		public boolean isPresent() {
			return this == Present || this == Added;
		}

	}

	public static class PrefetchException extends RuntimeException {

		private static final long serialVersionUID = 8681544474510411429L;

		public PrefetchException(final String msg) {
			super(msg);
		}

	}

	private static final Logger log = LoggerFactory.getLogger(SessionCachedModel.class);

	private static final long UNDEFINED = -3;

	private static final boolean WARN_ON_FIELDS = false;

	private static final boolean WARN_ON_REMOVES = false;

	/**
	 * Compile-time flag to help debugging. If on, all read-accesses to
	 * non-pre-fetched entities results in a warning.
	 */
	private static final boolean WARN_ON_UNCACHED_ACCESS = false;

	private final XAddress address;

	/** ObjectId -> CachedObject */
	private final Map<XId, CachedObject> cachedObjects;

	private boolean knowsAllObjectIds = false;

	public SessionCachedModel(final XAddress address) {
		this.address = address;
		this.cachedObjects = new HashMap<XId, SessionCachedModel.CachedObject>(2);
	}

	/**
	 * Does not change the sessionCachedModel itself.
	 *
	 * @param txnBuilder
	 */
	public void commitTo(final XTransactionBuilder txnBuilder) {
		for (final CachedObject co : this.cachedObjects.values()) {
			if (co.state == EntityState.Added) {
				txnBuilder.addObjectForced(getAddress(), co);
			} else if (co.state == EntityState.Removed) {
				txnBuilder.removeObject(getAddress(), XCommand.FORCED, co.getId());
			} else {
				// fields might have changed
				for (final CachedField cf : co.cachedFields.values()) {
					if (cf.state == EntityState.Added) {
						txnBuilder.addFieldForced(co.getAddress(), cf);
					} else if (cf.state == EntityState.Removed) {
						txnBuilder.removeField(co.getAddress(), XCommand.FORCED, cf.getId());
					} else {
						// value might have changed
						if (cf.isValueAdded()) {
							txnBuilder.addValue(cf.getAddress(), XCommand.FORCED, cf.current);
						} else if (cf.isValueRemoved()) {
							txnBuilder.removeValue(cf.getAddress(), XCommand.FORCED);
						} else if (cf.isValueChanged()) {
							txnBuilder.setValue(cf.getAddress(), cf.current);
						}
					}
				}
			}
		}
	}

	public void commitTo(final XWritableModel writableModel) {
		for (final CachedObject co : this.cachedObjects.values()) {
			if (co.state == EntityState.Added) {
				final XWritableObject writableObject = writableModel.createObject(co.getId());
				XCopyUtils.copyData(co, writableObject);
			} else if (co.state == EntityState.Removed) {
				writableModel.removeObject(co.getId());
			} else {
				final XWritableObject writableObject = writableModel.getObject(co.getId());
				assert writableObject != null : "base model should know object " + co.getId();
				// fields might have changed
				for (final CachedField cf : co.cachedFields.values()) {
					if (cf.state == EntityState.Added) {
						final XWritableField writableField = writableObject.createField(cf.getId());
						XCopyUtils.copyData(cf, writableField);
					} else if (cf.state == EntityState.Removed) {
						writableObject.removeField(cf.getId());
					} else {
						final XWritableField writableField = writableObject.getField(cf.getId());
						// value might have changed
						if (cf.isValueAdded()) {
							writableField.setValue(cf.getValue());
						} else if (cf.isValueRemoved()) {
							writableField.setValue(null);
						} else if (cf.isValueChanged()) {
							writableField.setValue(cf.getValue());
						}
					}
				}
			}
		}
	}

	@Override
	public XWritableObject createObject(@NeverNull final XId objectId) {
		// first, consult caches
		final CachedObject co = this.cachedObjects.get(objectId);
		if (co != null) {
			// create only if possible
			if (co.state == EntityState.NotPresent) {
				co.state = EntityState.Added;
			}
			if (co.state == EntityState.Removed) {
				co.state = EntityState.Present;
			}
			return co;
		}

		// not known, might be present in base, record as added anyway
		return setObjectState(objectId, EntityState.Added);
	}

	@Override
	public Collection<? extends XReadableObject> getAdded() {
		final List<XReadableObject> list = new LinkedList<XReadableObject>();
		for (final CachedObject co : this.cachedObjects.values()) {
			if (co.isAdded()) {
				list.add(co);
			}
		}
		return list;
	}

	@Override
	public XAddress getAddress() {
		return this.address;
	}

	@Override
	public XId getId() {
		return this.address.getModel();
	}

	@SuppressWarnings("unused")
	@Override
	public XWritableObject getObject(@NeverNull final XId objectId) {
		final XWritableObject xo = this.cachedObjects.get(objectId);
		if (WARN_ON_UNCACHED_ACCESS && xo == null) {
			log.warn("Object '" + objectId + "' not prefetched in " + getAddress()
					+ ". Return getObject=null.");
		}
		return xo;
	}

	@Override
	public Collection<? extends IObjectDiff> getPotentiallyChanged() {
		final List<IObjectDiff> list = new LinkedList<IObjectDiff>();
		for (final CachedObject co : this.cachedObjects.values()) {
			if (!co.isAdded() && !co.isRemoved() && co.hasChanges()) {
				list.add(co);
			}
		}
		return list;
	}

	@Override
	public Collection<XId> getRemoved() {
		final List<XId> list = new LinkedList<XId>();
		for (final CachedObject co : this.cachedObjects.values()) {
			if (co.isRemoved()) {
				list.add(co.getId());
			}
		}
		return list;
	}

	@Override
	public long getRevisionNumber() {
		return this.rev;
	}

	@Override
	public XType getType() {
		return XType.XMODEL;
	}

	public boolean hasChanges() {
		for (final CachedObject co : this.cachedObjects.values()) {
			if (co.hasChanges()) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean hasObject(@NeverNull final XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		final CachedObject co = this.cachedObjects.get(objectId);
		if (co != null) {
			return co.isPresent();
		}
		if (WARN_ON_UNCACHED_ACCESS && co == null) {
			log.warn("Object '" + objectId + "' not prefetched in " + getAddress()
					+ ". Return hasObject=false.");
		}
		return false;
	}

	/**
	 * Set the objects in the baseModel as the current state, not overwriting
	 * the state of already changed objects.
	 *
	 * @param baseModel
	 */
	public void indexModel(final XReadableModel baseModel) {
		long c = 0;
		for (final XId id : baseModel) {
			indexObject(baseModel.getObject(id));
			c++;
		}
		log.info("Indexed " + c + " object in model " + baseModel.getAddress());
		this.rev = baseModel.getRevisionNumber();
		this.knowsAllObjectIds = true;
	}

	public void indexObject(final XReadableObject baseObject) {
		CachedObject co = this.cachedObjects.get(baseObject.getId());
		if (co == null) {
			co = setObjectState(baseObject.getId(), EntityState.Present);
		} else {
			if (log.isTraceEnabled()) {
				log.trace("Avoid re-indexing object " + baseObject.getAddress());
			}
		}

		co.indexFieldsFrom(baseObject);
		co.rev = baseObject.getRevisionNumber();
	}

	@Override
	public boolean isEmpty() {
		if (this.knowsAllObjectIds && this.cachedObjects.isEmpty()) {
			return true;
		}
		// else
		for (final Map.Entry<XId, CachedObject> e : this.cachedObjects.entrySet()) {
			if (e.getValue().isPresent()) {
				return false;
			}
		}
		// else:
		/* we checked all objects and know all of them */
		if (this.knowsAllObjectIds) {
			return true;
		}

		// // else: we don't know it and may not ask base
		// throw new PrefetchException(
		// "Not all objects are known and further loading from base is not permitted. State unknown.");
		// IMPROVE throw exception?
		return true;

	}

	@Override
	public Iterator<XId> iterator() {
		// IMPROVE throw exception?
		if (!this.knowsAllObjectIds) {
			log.warn("We don't know all object ids - returning incomplete set for " + this.address);
		}

		return new TransformingIterator<Map.Entry<XId, CachedObject>, XId>(

		new AbstractFilteringIterator<Map.Entry<XId, CachedObject>>(this.cachedObjects.entrySet()
				.iterator()) {
			@Override
			protected boolean matchesFilter(final Map.Entry<XId, CachedObject> entry) {
				return entry.getValue().isPresent();
			}
		}, new ITransformer<Map.Entry<XId, CachedObject>, XId>() {

			@Override
			public XId transform(final Map.Entry<XId, CachedObject> entry) {
				return entry.getKey();
			}
		});
	}

	/**
	 * Moves all added/removed objects/fields and all changed values to
	 * read-cache.
	 */
	public void markAsCommitted() {
		for (final CachedObject co : this.cachedObjects.values()) {
			co.state = co.state.afterSuccesfullCommit();
			for (final CachedField cf : co.cachedFields.values()) {
				cf.state = cf.state.afterSuccesfullCommit();
				cf.initial = cf.current;
			}
		}
	}

	@SuppressWarnings("unused")
	@Override
	public boolean removeObject(@NeverNull final XId objectId) {
		final CachedObject co = this.cachedObjects.get(objectId);
		if (co == null) {
			if (WARN_ON_UNCACHED_ACCESS && WARN_ON_REMOVES) {
				log.warn("Object '" + objectId + "' not prefetched in " + getAddress()
						+ ". Removing anyway.");
			}
			setObjectState(objectId, EntityState.Removed);
			return false;
		} else {
			final boolean b = co.isPresent();
			co.state = EntityState.Removed;
			return b;
		}
	}

	/**
	 * Allows to set knowsAllObjectIds to true even if
	 * {@link #indexModel(XReadableModel)} was never called. All relevant
	 * objects should have been indexed via
	 * {@link #indexObject(XReadableObject)}. Everything else will be considered
	 * non-existent.
	 *
	 * @param b
	 */
	public void setKnowsAllObjectIds(final boolean b) {
		XyAssert.xyAssert(b || !this.knowsAllObjectIds);
		this.knowsAllObjectIds = b;
	}

	private CachedObject setObjectState(final XId id, final EntityState objectState) {
		final CachedObject co = new CachedObject(Base.resolveObject(getAddress(), id), objectState);
		this.cachedObjects.put(id, co);
		XyAssert.xyAssert(co.getId().equals(id));
		return co;
	}

	@Override
	public String toString() {
		return "State including changes:\nM:" + getId() + " {\n"
				+ DumpUtilsBase.toStringBuffer(this) + "Changes: \n"
				+ DumpUtils.changesToString(this).toString() + "}";
	}

	public boolean isKnownObject(final XId objectId) {
		final CachedObject co = this.cachedObjects.get(objectId);
		return co != null;
	}

	public boolean knowsAllObjects() {
		return this.knowsAllObjectIds;
	}

	/**
	 * Removes all changes in the SessionCachedModel, its XObjects, their
	 * XFields their XValues
	 */
	public void discardAllChanges() {

		final Set<XId> objectsToDelete = new HashSet<XId>();

		for (final Entry<XId, CachedObject> e : this.cachedObjects.entrySet()) {
			final XId key = e.getKey();
			final CachedObject co = e.getValue();

			if (co.isAdded()) {
				objectsToDelete.add(key);
			} else if (co.isRemoved() || co.hasChanges()) {
				if (co.isRemoved()) {
					if (co.getRevisionNumber() < 0) {
						objectsToDelete.add(key);
					} else {
						co.state = EntityState.Present;
					}
				}
				co.discardChanges();
			}
		}

		for (final XId xid : objectsToDelete) {
			this.cachedObjects.remove(xid);
		}
	}

}
