package org.xydra.core.change;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.IndexUtils;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A helper class to minimize the number and size of persistence accesses.
 * 
 * Does not support revision numbers.
 * 
 * An implementation of {@link XWritableModel} that works as a diff on top of a
 * base {@link XWritableModel}. Via {@link #toCommandList()} a minimal list of
 * commands that changes the base model into the current state can be created.
 * The base model is changed at no times.
 * 
 * @author xamde
 */
public class DiffWritableModel extends AbstractDelegatingWritableModel implements XWritableModel {
	
	private static final Logger log = LoggerFactory.getLogger(DiffWritableModel.class);
	
	private static final XID NONE = XX.toId("_NoIdDiff");
	
	private static final XValue NOVALUE = XV.toValue("_NoValueDiff");
	
	/*
	 * Each index has the structure (object, field, value) with the notion to
	 * represent content in this model within a given repository.
	 * 
	 * An object without fields is represented as (objectId, NONE, NOVALUE).
	 * 
	 * A field without values is (objectId, fieldId, NOVALUE).
	 */
	MapMapIndex<XID,XID,XValue> added, removed;
	
	private final XWritableModel base;
	
	public DiffWritableModel(final XWritableModel base) {
		assert base != null;
		assert !(base instanceof ReadCachingWritableModel);
		this.base = new ReadCachingWritableModel(base);
		this.added = new MapMapIndex<XID,XID,XValue>();
		this.removed = new MapMapIndex<XID,XID,XValue>();
	}
	
	private XValue getFieldValueFromBase(XID objectId, XID fieldId) {
		XWritableObject o = this.base.getObject(objectId);
		if(o == null)
			return null;
		XWritableField f = o.getField(fieldId);
		if(f == null)
			return null;
		return f.getValue();
	}
	
	@Override
	public XWritableObject createObject(XID objectId) {
		if(hasObject(objectId)) {
			return getObject(objectId);
		}
		
		this.removed.deIndex(objectId, NONE);
		this.added.index(objectId, NONE, NOVALUE);
		return new WrappedObject(objectId);
	}
	
	@Override
	protected XValue field_getValue(XID objectId, XID fieldId) {
		assert hasObject(objectId) : "Object '" + resolveObject(objectId)
		        + "' not found when looking for field '" + fieldId + "'";
		assert getObject(objectId).hasField(fieldId);
		
		XValue value = this.added.lookup(objectId, fieldId);
		if(value != null) {
			if(value == NOVALUE) {
				return null;
			}
			return value;
		} else {
			value = this.removed.lookup(objectId, fieldId);
			if(value != null) {
				return null;
			} else {
				return this.base.getObject(objectId).getField(fieldId).getValue();
			}
		}
	}
	
	@Override
	protected boolean field_setValue(XID objectId, XID fieldId, XValue value) {
		assert objectId != null;
		assert fieldId != null;
		assert hasObject(objectId) : "Missing object with id " + objectId + " to set field "
		        + fieldId;
		assert this.removed.lookup(objectId, fieldId) == null;
		assert getObject(objectId).hasField(fieldId);
		
		XValue v = field_getValue(objectId, fieldId);
		if((v == null && value == null) || (v != null && v.equals(value))) {
			return false;
		}
		
		this.removed.deIndex(objectId, fieldId);
		this.added.index(objectId, fieldId, value);
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
		if(this.added.containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>())) {
			assert !this.removed.containsKey(new EqualsConstraint<XID>(objectId),
			        new EqualsConstraint<XID>(NONE));
			return true;
		} else if(this.removed.containsKey(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(NONE))) {
			assert !this.added
			        .containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
			return false;
		} else {
			return this.base.hasObject(objectId);
		}
	}
	
	protected Set<XID> idsAsSet() {
		return IndexUtils.diff(this.base.iterator(), this.added.key1Iterator(),
		        this.removed.key1Iterator());
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
			this.removed.deIndex(objectId, fieldId);
			this.added.index(objectId, fieldId, NOVALUE);
		}
		return new WrappedField(objectId, fieldId);
	}
	
	protected boolean object_hasField(XID objectId, XID fieldId) {
		assert objectId != null;
		assert fieldId != null;
		if(this.added.tupleIterator(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(fieldId)).hasNext()) {
			return true;
		}
		if(this.removed.tupleIterator(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(fieldId)).hasNext()) {
			return false;
		}
		// else
		return this.base.hasObject(objectId) && this.base.getObject(objectId).hasField(fieldId);
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
		
		if(this.added.containsKey(new EqualsConstraint<XID>(objectId), new EqualsConstraint<XID>(
		        fieldId))) {
			this.added.deIndex(objectId, fieldId);
		} else {
			this.removed.index(objectId, fieldId, NOVALUE);
		}
		
		return b;
	}
	
	protected Set<XID> object_idsAsSet(XID objectId) {
		assert objectId != null;
		// add all from base
		Set<XID> set = new HashSet<XID>();
		if(this.base.hasObject(objectId)) {
			set.addAll(IndexUtils.toSet(this.base.getObject(objectId).iterator()));
		}
		// remove all from removed
		Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = this.removed.tupleIterator(
		        new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
			set.remove(entry.getKey2());
		}
		// add all from added
		it = this.added.tupleIterator(new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
			set.add(entry.getKey2());
		}
		// done
		return set;
	}
	
	public boolean removeObject(XID objectId) {
		assert objectId != null;
		if(this.added.containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>())) {
			// fine
			IndexUtils
			        .deIndex(this.added, new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
			return true;
		} else if(this.removed
		        .containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>())) {
			assert !this.added
			        .containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
			return false;
		} else {
			// base
			return this.base.removeObject(objectId);
		}
	}
	
	public List<XAtomicCommand> toCommandList() {
		List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();
		
		// remove
		Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = this.removed.tupleIterator(
		        new Wildcard<XID>(), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> objectFieldValue = it.next();
			if(objectFieldValue.getKey2().equals(NONE)) {
				// remove object
				list.add(X.getCommandFactory().createForcedRemoveObjectCommand(
				        resolveObject(objectFieldValue.getKey1())));
			} else {
				// remove empty field or field with value
				list.add(X.getCommandFactory().createForcedRemoveFieldCommand(
				        resolveField(objectFieldValue.getKey1(), objectFieldValue.getKey2())));
			}
		}
		
		// add
		it = this.added.tupleIterator(new Wildcard<XID>(), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> e = it.next();
			if(e.getKey2().equals(NONE)) {
				// add object
				list.add(X.getCommandFactory().createForcedAddObjectCommand(getAddress(),
				        e.getKey1()));
			} else if(e.getEntry().equals(NOVALUE)) {
				// add empty field
				list.add(X.getCommandFactory().createForcedAddFieldCommand(
				        resolveObject(e.getKey1()), e.getKey2()));
			} else {
				// value
				XValue currentValue = getFieldValueFromBase(e.getKey1(), e.getKey2());
				if(currentValue == null) {
					// maybe still add field
					if(!this.base.hasObject(e.getKey1())
					        || !this.base.getObject(e.getKey1()).hasField(e.getKey2())) {
						list.add(X.getCommandFactory().createForcedAddFieldCommand(
						        resolveObject(e.getKey1()), e.getKey2()));
					}
					// add value
					list.add(X.getCommandFactory().createForcedAddValueCommand(
					        resolveField(e.getKey1(), e.getKey2()), e.getEntry()));
				} else {
					// change value
					list.add(X.getCommandFactory().createForcedChangeValueCommand(
					        resolveField(e.getKey1(), e.getKey2()), e.getEntry()));
				}
			}
		}
		
		/*
		 * Make sure model commands come before object commands; object commands
		 * come before field commands. This avoid e.g. deleting a field before
		 * deleting its object parent.
		 */
		Collections.sort(list, new Comparator<XAtomicCommand>() {
			@Override
			public int compare(XAtomicCommand a, XAtomicCommand b) {
				return b.getChangedEntity().getAddressedType()
				        .compareTo(a.getChangedEntity().getAddressedType());
			}
		});
		
		return list;
	}
	
	/**
	 * @return txn or null
	 */
	public XTransaction toTransaction() {
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		List<XAtomicCommand> list = toCommandList();
		for(XAtomicCommand command : list) {
			builder.addCommand(command);
		}
		if(builder.isEmpty()) {
			log.info("No command in txn");
			return null;
		}
		return builder.build();
	}
	
	public boolean hasChanges() {
		return !this.added.isEmpty() || !this.removed.isEmpty();
	}
	
}
