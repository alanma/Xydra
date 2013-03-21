package org.xydra.core.change;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.util.Clock;
import org.xydra.index.IndexUtils;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A helper class to minimise the number and size of persistence read accesses.
 * Supports pre-filling the cache.
 * 
 * Change operations directly change the underlying base model.
 * 
 * Does not support revision numbers.
 * 
 * TODO IMPROVE support age of cache entries (maybe with MapMapIndex and E=Pair
 * 
 * @author xamde
 */
@Deprecated
public class ReadCachingWritableModel extends AbstractDelegatingWritableModel implements
        XStateWritableModel {
	
	private static final Logger log = LoggerFactory.getLogger(ReadCachingWritableModel.class);
	
	private static final XId NOFIELD = XX.toId("_NoFieldReadCache");
	
	/**
	 * Denote an object which has no known fields or a field of which the value
	 * is not known.
	 */
	private static final XValue NOVALUE = XV.toValue("_NoValueReadCache");
	
	/**
	 * Index has the structure (object, field, value) with the notion to
	 * represent content in this model within a given repository.
	 * 
	 * An object without fields is represented as (objectId, NONE, NOVALUE).
	 * 
	 * A field without values is (objectId, fieldId, NOVALUE).
	 */
	private MapMapIndex<XId,XId,XValue> cache;
	
	/**
	 * Cache of stuff that is known to NOT exist
	 */
	private MapMapIndex<XId,XId,XValue> antiCache;
	
	/**
	 * True, if this cache knows all objects contained in the model. Says
	 * nothing about the objects state, fields and values.
	 */
	private boolean knowsAllObjectIds = false;
	
	private Set<XId> objectIdsOfWhichAllFieldIdsAreKnown = new HashSet<XId>();
	
	private Set<Pair<XId,XId>> fieldsOfWhichTheValueIsKnown = new HashSet<Pair<XId,XId>>();
	
	private final XWritableModel base;
	
	/**
	 * @param base any {@link XWritableModel}, should not be itself a
	 *            {@link ReadCachingWritableModel}
	 * @param prefetchModel if true, construct one that pre-fetches all model
	 *            content at constructor call time
	 */
	public ReadCachingWritableModel(final XWritableModel base, boolean prefetchModel) {
		XyAssert.xyAssert(base != null);
		assert base != null;
		XyAssert.xyAssert(!(base instanceof ReadCachingWritableModel));
		this.base = base;
		this.cache = new MapMapIndex<XId,XId,XValue>();
		this.antiCache = new MapMapIndex<XId,XId,XValue>();
		if(prefetchModel) {
			prefetchModel();
		}
	}
	
	public void prefetchModel() {
		Clock c = new Clock().start();
		this.retrieveAllObjectsIdsFromBaseAndCache();
		Set<XId> ids = this.idsAsSet();
		for(XId objectId : ids) {
			prefetchObject(objectId);
		}
		log.info("Prefetching " + ids.size() + " objects in model '" + this.base.getId()
		        + "' took " + c.stopAndGetDuration("prefetech") + " ms");
	}
	
	public void prefetchObject(XId objectId) {
		this.retrieveAllFieldIdsOfObjectFromSourceAndCache(this.base, objectId);
		for(XId fieldId : this.object_idsAsSet(objectId)) {
			this.retrieveValueFromSourceAndCache(this.base, objectId, fieldId);
		}
	}
	
	/**
	 * Pre-fetches all model content at constructor call time from given
	 * persistence via snapshots.
	 * 
	 * @param base any {@link XWritableModel}, should not be itself a
	 *            {@link ReadCachingWritableModel}
	 * @param persistence for loading snapsnots
	 */
	public ReadCachingWritableModel(final XWritableModel base, XydraPersistence persistence) {
		XyAssert.xyAssert(base != null);
		assert base != null;
		XyAssert.xyAssert(!(base instanceof ReadCachingWritableModel));
		this.base = base;
		this.cache = new MapMapIndex<XId,XId,XValue>();
		// prefetching
		Clock c = new Clock().start();
		
		XWritableModel snapshot = persistence.getModelSnapshot(new GetWithAddressRequest(
		        getAddress(), true));
		for(XId objectId : snapshot) {
			this.cache.index(objectId, NOFIELD, NOVALUE);
			this.retrieveAllFieldIdsOfObjectFromSourceAndCache(snapshot, objectId);
			for(XId fieldId : this.object_idsAsSet(objectId)) {
				this.retrieveValueFromSourceAndCache(snapshot, objectId, fieldId);
			}
		}
		this.knowsAllObjectIds = true;
		
		Set<XId> ids = this.idsAsSet();
		log.info("Prefetching " + ids.size() + " objects in model '" + base.getId() + "' took "
		        + c.stopAndGetDuration("prefetech") + " ms");
	}
	
	@Override
	public XWritableObject createObject(@NeverNull XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		if(!hasObject(objectId)) {
			this.antiCache.index(objectId, NOFIELD, NOVALUE);
		}
		return new WrappedObject(objectId);
	}
	
	@Override
	protected XValue field_getValue(XId objectId, XId fieldId) {
		assert hasObject(objectId) : "Object '" + resolveObject(objectId)
		        + "' not found when looking for field '" + fieldId + "'";
		XyAssert.xyAssert(getObject(objectId).hasField(fieldId));
		
		Pair<XId,XId> p = new Pair<XId,XId>(objectId, fieldId);
		XValue result;
		if(this.fieldsOfWhichTheValueIsKnown.contains(p)) {
			result = this.cache.lookup(objectId, fieldId);
		} else {
			result = retrieveValueFromSourceAndCache(this.base, objectId, fieldId);
		}
		if(result != null && result.equals(NOVALUE)) {
			return null;
		}
		
		return result;
	}
	
	private XValue retrieveValueFromSourceAndCache(XWritableModel sourceModel, XId objectId,
	        XId fieldId) {
		XyAssert.xyAssert(sourceModel != null);
		assert sourceModel != null;
		/* base might never have seen object or field */
		XWritableObject sourceObject = sourceModel.getObject(objectId);
		if(sourceObject == null) {
			return null;
		}
		XWritableField sourceField = sourceObject.getField(fieldId);
		if(sourceField == null) {
			return null;
		}
		XValue sourceValue = sourceField.getValue();
		XyAssert.xyAssert(sourceValue == null || !sourceValue.equals(NOVALUE));
		XyAssert.xyAssert(sourceObject.getId().equals(objectId));
		XyAssert.xyAssert(sourceField.getId().equals(fieldId));
		// index also if null
		this.cache.index(objectId, fieldId, sourceValue);
		this.fieldsOfWhichTheValueIsKnown.add(new Pair<XId,XId>(objectId, fieldId));
		
		XyAssert.xyAssert(field_getValue(objectId, fieldId) == null
		        || !field_getValue(objectId, fieldId).equals(NOVALUE));
		
		return sourceValue;
	}
	
	@Override
	protected boolean field_setValue(XId objectId, XId fieldId, XValue value) {
		throw new RuntimeException("a read cache cannot set values");
		// XyAssert.xyAssert(objectId != null); assert objectId != null;
		// XyAssert.xyAssert(fieldId != null); assert fieldId != null;
		// assert hasObject(objectId) : "Expected " + objectId;
		// XyAssert.xyAssert(getObject(objectId).hasField(fieldId));
		//
		// // NOP?
		// XValue v = field_getValue(objectId, fieldId);
		// if(bothNullOrEqual(v, value)) {
		// return false;
		// }
		//
		// // index
		// this.cache.index(objectId, fieldId, value);
		//
		// return true;
	}
	
	@SuppressWarnings("unused")
	private static boolean bothNullOrEqual(Object a, Object b) {
		if(a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
	
	@Override
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	/**
	 * Allows to end a transaction and go back to using the base object.
	 * 
	 * @return the base object that has been used to created this wrapped
	 *         {@link DiffWritableObject}.
	 */
	public XWritableModel getBase() {
		return this.base;
	}
	
	@Override
	public XId getId() {
		return this.base.getId();
	}
	
	@Override
	public boolean hasObject(@NeverNull XId objectId) {
		if(this.cache.containsKey(new EqualsConstraint<XId>(objectId), new Wildcard<XId>())) {
			return true;
		}
		// else
		if(this.knowsAllObjectIds) {
			return false;
		}
		// else:
		if(this.antiCache.containsKey(new EqualsConstraint<XId>(objectId), new Wildcard<XId>())) {
			return false;
		}
		boolean b = this.base.hasObject(objectId);
		// index
		if(b) {
			this.cache.index(objectId, NOFIELD, NOVALUE);
		} else {
			this.antiCache.index(objectId, NOFIELD, NOVALUE);
		}
		return b;
	}
	
	protected Set<XId> idsAsSet() {
		if(this.knowsAllObjectIds) {
			return IndexUtils.toSet(this.cache.key1Iterator());
		} else {
			return retrieveAllObjectsIdsFromBaseAndCache();
		}
	}
	
	private Set<XId> retrieveAllObjectsIdsFromBaseAndCache() {
		Set<XId> set = IndexUtils.toSet(this.base.iterator());
		for(XId objectId : set) {
			this.cache.index(objectId, NOFIELD, NOVALUE);
		}
		this.knowsAllObjectIds = true;
		return set;
	}
	
	@Override
	public boolean isEmpty() {
		return this.idsAsSet().isEmpty();
	}
	
	@Override
	public Iterator<XId> iterator() {
		return this.idsAsSet().iterator();
	}
	
	@Override
	protected XWritableField object_createField(XId objectId, XId fieldId) {
		throw new RuntimeException("a read cache cannot access create");
		//
		// XyAssert.xyAssert(objectId != null); assert objectId != null;
		// XyAssert.xyAssert(fieldId != null); assert fieldId != null;
		// XyAssert.xyAssert(this.hasObject(objectId));
		// if(!object_hasField(objectId, fieldId)) {
		// // index
		// this.cache.index(objectId, fieldId, NOVALUE);
		// }
		// return new WrappedField(objectId, fieldId);
	}
	
	@Override
	protected boolean object_hasField(XId objectId, XId fieldId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		XyAssert.xyAssert(fieldId != null);
		assert fieldId != null;
		if(this.cache.tupleIterator(new EqualsConstraint<XId>(objectId),
		        new EqualsConstraint<XId>(fieldId)).hasNext()) {
			return true;
		}
		// else
		if(this.objectIdsOfWhichAllFieldIdsAreKnown.contains(objectId)) {
			return false;
		}
		// else
		if(this.antiCache.tupleIterator(new EqualsConstraint<XId>(objectId),
		        new EqualsConstraint<XId>(fieldId)).hasNext()) {
			return false;
		}
		// else
		boolean baseHasField = this.base.hasObject(objectId)
		        && this.base.getObject(objectId).hasField(fieldId);
		// index
		if(baseHasField) {
			this.cache.index(objectId, fieldId, NOVALUE);
		} else {
			this.antiCache.index(objectId, fieldId, NOVALUE);
		}
		return baseHasField;
	}
	
	@Override
	protected boolean object_isEmpty(XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		return object_idsAsSet(objectId).isEmpty();
	}
	
	@Override
	protected Iterator<XId> object_iterator(XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		return object_idsAsSet(objectId).iterator();
	}
	
	@Override
	protected boolean object_removeField(XId objectId, XId fieldId) {
		throw new RuntimeException("a read cache cannot access remove");
		// XyAssert.xyAssert(objectId != null); assert objectId != null;
		// XyAssert.xyAssert(fieldId != null); assert fieldId != null;
		// boolean b = object_hasField(objectId, fieldId);
		// // deIndex
		// this.cache.deIndex(objectId, fieldId);
		// return b;
	}
	
	protected Set<XId> object_idsAsSet(XId objectId) {
		XyAssert.xyAssert(objectId != null);
		assert objectId != null;
		if(this.objectIdsOfWhichAllFieldIdsAreKnown.contains(objectId)) {
			// add all from cache
			Set<XId> set = new HashSet<XId>();
			Iterator<KeyKeyEntryTuple<XId,XId,XValue>> it = this.cache.tupleIterator(
			        new EqualsConstraint<XId>(objectId), new Wildcard<XId>());
			while(it.hasNext()) {
				KeyKeyEntryTuple<XId,XId,XValue> entry = it.next();
				set.add(entry.getKey2());
			}
			return set;
		} else {
			return retrieveAllFieldIdsOfObjectFromSourceAndCache(this.base, objectId);
		}
	}
	
	private Set<XId> retrieveAllFieldIdsOfObjectFromSourceAndCache(XWritableModel sourceModel,
	        XId objectId) {
		Set<XId> set;
		XWritableObject sourceObject = sourceModel.getObject(objectId);
		if(sourceObject == null) {
			set = Collections.emptySet();
		} else {
			set = IndexUtils.toSet(sourceObject.iterator());
			// index
			for(XId fieldId : set) {
				this.cache.index(objectId, fieldId, NOVALUE);
			}
		}
		this.objectIdsOfWhichAllFieldIdsAreKnown.add(objectId);
		return set;
	}
	
	@Override
	public boolean removeObject(@NeverNull XId objectId) {
		throw new RuntimeException("A read-cache cannot be changed");
		// XyAssert.xyAssert(objectId != null); assert objectId != null;
		// // deIndex
		// IndexUtils.deIndex(this.cache, new EqualsConstraint<XId>(objectId),
		// new Wildcard<XId>());
		//
		// return this.base.removeObject(objectId);
	}
	
	public long getRevisionNumber() {
		log.debug("Returning outdated base-revision number");
		return this.base.getRevisionNumber();
	}
	
	@Override
	protected long object_getRevisionNumber(XId objectId) {
		XWritableObject object = this.base.getObject(objectId);
		if(object == null) {
			return UNDEFINED;
		}
		return object.getRevisionNumber();
	}
	
	@Override
	protected long field_getRevisionNumber(XId objectId, XId fieldId) {
		XWritableObject object = this.base.getObject(objectId);
		if(object == null) {
			return UNDEFINED;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return UNDEFINED;
		}
		return field.getRevisionNumber();
	}
	
}
