package org.xydra.core.change;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.IndexUtils;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
public class ReadCachingWritableModel extends AbstractDelegatingWritableModel implements
        XWritableModel {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ReadCachingWritableModel.class);
	
	private static final XID NONE = XX.toId("_NoId");
	
	private static final XValue NOVALUE = XV.toValue("_NoValue");
	
	/**
	 * Index has the structure (object, field, value) with the notion to
	 * represent content in this model within a given repository.
	 * 
	 * An object without fields is represented as (objectId, NONE, NOVALUE).
	 * 
	 * A field without values is (objectId, fieldId, NOVALUE).
	 */
	private MapMapIndex<XID,XID,XValue> cache;
	
	/**
	 * True, if this cache knows all objects contained in the model. Says
	 * nothing about the objects state, fields and values.
	 */
	private boolean knowsAllObjectIds = false;
	
	private Set<XID> objectIdsOfWhichAllFieldIdsAreKnown = new HashSet<XID>();
	
	private Set<Pair<XID,XID>> fieldsOfWhichTheValueIsKnown = new HashSet<Pair<XID,XID>>();
	
	private final XWritableModel base;
	
	public ReadCachingWritableModel(final XWritableModel base) {
		assert base != null;
		assert !(base instanceof ReadCachingWritableModel);
		this.base = base;
		this.cache = new MapMapIndex<XID,XID,XValue>();
		// prefetch
		this.retrieveAllObjectsIdsFromBaseAndCache();
		for(XID objectId : this.idsAsSet()) {
			this.retrieveAllFieldIdsOfObjectFromBaseAndCache(objectId);
			for(XID fieldId : this.object_idsAsSet(objectId)) {
				this.retrieveValueFromBaseAndCache(objectId, fieldId);
			}
		}
	}
	
	@Override
	public XWritableObject createObject(XID objectId) {
		this.cache.index(objectId, NONE, NOVALUE);
		return new WrappedObject(objectId);
	}
	
	@Override
	protected XValue field_getValue(XID objectId, XID fieldId) {
		assert hasObject(objectId) : "Object '" + resolveObject(objectId)
		        + "' not found when looking for field '" + fieldId + "'";
		assert getObject(objectId).hasField(fieldId);
		
		Pair<XID,XID> p = new Pair<XID,XID>(objectId, fieldId);
		if(this.fieldsOfWhichTheValueIsKnown.contains(p)) {
			return this.cache.lookup(objectId, fieldId);
		} else {
			return retrieveValueFromBaseAndCache(objectId, fieldId);
		}
	}
	
	private XValue retrieveValueFromBaseAndCache(XID objectId, XID fieldId) {
		assert this.base != null;
		/* base might never have seen object or field */
		XWritableObject object = this.base.getObject(objectId);
		if(object == null) {
			return null;
		}
		XWritableField field = object.getField(fieldId);
		if(field == null) {
			return null;
		}
		XValue result = field.getValue();
		// index also if null
		this.cache.index(objectId, fieldId, result);
		this.fieldsOfWhichTheValueIsKnown.add(new Pair<XID,XID>(objectId, fieldId));
		return result;
	}
	
	@Override
	protected boolean field_setValue(XID objectId, XID fieldId, XValue value) {
		assert objectId != null;
		assert fieldId != null;
		assert hasObject(objectId) : "Expected " + objectId;
		assert getObject(objectId).hasField(fieldId);
		
		// NOP?
		XValue v = field_getValue(objectId, fieldId);
		if(v.equals(value)) {
			return false;
		}
		
		// index
		this.cache.index(objectId, fieldId, value);
		
		return true;
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
	public XID getID() {
		return this.base.getID();
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		if(this.cache.containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>())) {
			return true;
		} else {
			if(this.knowsAllObjectIds) {
				return false;
			}
			// else:
			boolean b = this.base.hasObject(objectId);
			// index
			this.cache.index(objectId, NONE, NOVALUE);
			return b;
		}
	}
	
	protected Set<XID> idsAsSet() {
		if(this.knowsAllObjectIds) {
			return IndexUtils.toSet(this.cache.key1Iterator());
		} else {
			return retrieveAllObjectsIdsFromBaseAndCache();
		}
	}
	
	private Set<XID> retrieveAllObjectsIdsFromBaseAndCache() {
		Set<XID> set = IndexUtils.toSet(this.base.iterator());
		for(XID objectId : set) {
			this.cache.index(objectId, NONE, NOVALUE);
		}
		this.knowsAllObjectIds = true;
		return set;
	}
	
	public boolean isEmpty() {
		return this.idsAsSet().isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.idsAsSet().iterator();
	}
	
	protected XWritableField object_createField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		assert this.hasObject(objectId);
		if(!object_hasField(objectId, fieldId)) {
			// index
			this.cache.index(objectId, fieldId, NOVALUE);
		}
		return new WrappedField(objectId, fieldId);
	}
	
	protected boolean object_hasField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		if(this.cache.tupleIterator(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(fieldId)).hasNext()) {
			return true;
		} else {
			if(this.objectIdsOfWhichAllFieldIdsAreKnown.contains(objectId)) {
				return false;
			}
			// else
			boolean baseHasField = this.base.hasObject(objectId)
			        && this.base.getObject(objectId).hasField(fieldId);
			// index
			if(baseHasField) {
				this.cache.index(objectId, fieldId, NOVALUE);
			}
			return baseHasField;
		}
	}
	
	protected boolean object_isEmpty(XID objectId) {
		assert objectId != null;
		return object_idsAsSet(objectId).isEmpty();
	}
	
	protected Iterator<XID> object_iterator(XID objectId) {
		assert objectId != null;
		return object_idsAsSet(objectId).iterator();
	}
	
	protected boolean object_removeField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		boolean b = object_hasField(objectId, fieldId);
		// deIndex
		this.cache.deIndex(objectId, fieldId);
		return b;
	}
	
	protected Set<XID> object_idsAsSet(XID objectId) {
		assert objectId != null;
		if(this.objectIdsOfWhichAllFieldIdsAreKnown.contains(objectId)) {
			// add all from cache
			Set<XID> set = new HashSet<XID>();
			Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = this.cache.tupleIterator(
			        new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
			while(it.hasNext()) {
				KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
				set.add(entry.getKey2());
			}
			return set;
		} else {
			return retrieveAllFieldIdsOfObjectFromBaseAndCache(objectId);
		}
	}
	
	private Set<XID> retrieveAllFieldIdsOfObjectFromBaseAndCache(XID objectId) {
		Set<XID> set;
		XWritableObject o = this.base.getObject(objectId);
		if(o == null) {
			set = Collections.emptySet();
			// index
			for(XID fieldId : set) {
				this.cache.index(objectId, fieldId, NOVALUE);
			}
			this.objectIdsOfWhichAllFieldIdsAreKnown.add(objectId);
		} else {
			set = IndexUtils.toSet(o.iterator());
		}
		return set;
	}
	
	public boolean removeObject(XID objectId) {
		assert objectId != null;
		// deIndex
		IndexUtils.deIndex(this.cache, new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
		
		return this.base.removeObject(objectId);
	}
	
}
