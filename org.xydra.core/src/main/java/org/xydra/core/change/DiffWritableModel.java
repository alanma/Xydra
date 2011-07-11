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
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A helper class to minimize the number and size of persistence accesses.
 * 
 * An implementation of {@link XWritableModel} that works as a diff on top of a
 * base {@link XWritableModel}. Via {@link #toCommandList()} a minimal list of
 * commands that changes the base model into the current state can be created.
 * The base model is changed at no times.
 * 
 * @author xamde
 */
public class DiffWritableModel implements XWritableModel {
	
	private static final Logger log = LoggerFactory.getLogger(DiffWritableModel.class);
	
	private static final XID NONE = XX.toId("_NONE");
	private static final XValue NOVALUE = XV.toValue("_NONE");
	
	/* object, field, value */
	MapMapIndex<XID,XID,XValue> added, removed;
	
	private final XWritableModel base;
	
	public DiffWritableModel(final XWritableModel base) {
		assert base != null;
		this.base = base;
		this.added = new MapMapIndex<XID,XID,XValue>();
		this.removed = new MapMapIndex<XID,XID,XValue>();
	}
	
	@Override
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	@Override
	public XID getID() {
		return this.base.getID();
	}
	
	@Override
	public XWritableObject createObject(XID objectId) {
		this.added.index(objectId, NONE, NOVALUE);
		this.removed.deIndex(objectId, NONE);
		return new WrappedObject(objectId);
	}
	
	@Override
	public XWritableObject getObject(XID objectId) {
		if(hasObject(objectId)) {
			return new WrappedObject(objectId);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		if(this.added.containsKey(new EqualsConstraint<XID>(objectId), new Wildcard<XID>())) {
			// fine
			deIndex(this.added, new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
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
	
	/**
	 * TODO Move to Xydra Index Utils
	 * 
	 * @param mapMapIndex ..
	 * @param c1 ..
	 * @param c2 ..
	 */
	public static void deIndex(MapMapIndex<XID,XID,XValue> mapMapIndex, Constraint<XID> c1,
	        Constraint<XID> c2) {
		Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = mapMapIndex.tupleIterator(c1, c2);
		Set<KeyKeyEntryTuple<XID,XID,XValue>> toDelete = new HashSet<KeyKeyEntryTuple<XID,XID,XValue>>();
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
			toDelete.add(entry);
		}
		for(KeyKeyEntryTuple<XID,XID,XValue> entry : toDelete) {
			mapMapIndex.deIndex(entry.getKey1(), entry.getKey2());
		}
	}
	
	protected static <E> Set<E> toSet(Iterator<E> it) {
		Set<E> set = new HashSet<E>();
		while(it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}
	
	protected Set<XID> ids() {
		Set<XID> set = toSet(this.base.iterator());
		set.removeAll(toSet(this.removed.key1Iterator()));
		set.addAll(toSet(this.added.key1Iterator()));
		return set;
	}
	
	@Override
	public boolean isEmpty() {
		return this.ids().isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.ids().iterator();
	}
	
	@Override
	public long getRevisionNumber() {
		throw new UnsupportedOperationException("not implementable for DiffWritableModel");
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
	
	private class WrappedObject implements XWritableObject {
		
		private XID objectId;
		
		public WrappedObject(XID baseObjectId) {
			this.objectId = baseObjectId;
		}
		
		@Override
		public long getRevisionNumber() {
			throw new UnsupportedOperationException("not implementable for DiffWritableModel");
		}
		
		@Override
		public boolean hasField(XID fieldId) {
			return DiffWritableModel.this.objectHasField(this.objectId, fieldId);
		}
		
		@Override
		public boolean isEmpty() {
			return DiffWritableModel.this.objectIsEmpty(this.objectId);
		}
		
		@Override
		public Iterator<XID> iterator() {
			return DiffWritableModel.this.objectIterator(this.objectId);
		}
		
		@Override
		public XAddress getAddress() {
			return XX.resolveObject(DiffWritableModel.this.getAddress(), this.objectId);
		}
		
		@Override
		public XID getID() {
			return this.objectId;
		}
		
		@Override
		public XWritableField createField(XID fieldId) {
			return DiffWritableModel.this.objectCreateField(this.objectId, fieldId);
		}
		
		@Override
		public XWritableField getField(XID fieldId) {
			return DiffWritableModel.this.objectGetField(this.objectId, fieldId);
		}
		
		@Override
		public boolean removeField(XID fieldId) {
			return DiffWritableModel.this.objectRemoveField(this.objectId, fieldId);
		}
		
		@Override
		public XType getType() {
			return XType.XOBJECT;
		}
		
	}
	
	protected boolean objectHasField(XID objectId, XID fieldId) {
		if(this.added.tupleIterator(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(fieldId)).hasNext()) {
			return true;
		} else if(this.removed.tupleIterator(new EqualsConstraint<XID>(objectId),
		        new EqualsConstraint<XID>(fieldId)).hasNext()) {
			return false;
		} else {
			return this.base.hasObject(objectId) && this.base.getObject(objectId).hasField(fieldId);
		}
	}
	
	protected boolean objectRemoveField(XID objectId, XID fieldId) {
		boolean b = objectHasField(objectId, fieldId);
		this.added.deIndex(objectId, fieldId);
		this.removed.index(objectId, fieldId, NOVALUE);
		return b;
	}
	
	protected XWritableField objectGetField(XID objectId, XID fieldId) {
		if(objectHasField(objectId, fieldId)) {
			return new WrappedField(objectId, fieldId);
		} else {
			return null;
		}
	}
	
	protected XWritableField objectCreateField(XID objectId, XID fieldId) {
		assert this.hasObject(objectId);
		if(!objectHasField(objectId, fieldId)) {
			this.added.index(objectId, fieldId, NOVALUE);
			this.removed.deIndex(objectId, fieldId);
		}
		return new WrappedField(objectId, fieldId);
	}
	
	protected Set<XID> objectIds(XID objectId) {
		Set<XID> set = new HashSet<XID>();
		
		if(this.base.hasObject(objectId)) {
			set.addAll(toSet(this.base.getObject(objectId).iterator()));
		}
		// adjust
		
		Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = this.removed.tupleIterator(
		        new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
			set.remove(entry.getKey2());
		}
		it = this.added.tupleIterator(new EqualsConstraint<XID>(objectId), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> entry = it.next();
			set.add(entry.getKey2());
		}
		
		return set;
	}
	
	protected Iterator<XID> objectIterator(XID objectId) {
		return objectIds(objectId).iterator();
	}
	
	protected boolean objectIsEmpty(XID objectId) {
		return objectIds(objectId).isEmpty();
	}
	
	private class WrappedField implements XWritableField {
		
		private XID objectId;
		private XID fieldId;
		
		public WrappedField(XID objectId, XID fieldId) {
			this.objectId = objectId;
			this.fieldId = fieldId;
		}
		
		@Override
		public long getRevisionNumber() {
			throw new UnsupportedOperationException("not implementable for DiffWritableModel");
		}
		
		@Override
		public XValue getValue() {
			return DiffWritableModel.this.fieldGetValue(this.objectId, this.fieldId);
		}
		
		@Override
		public boolean isEmpty() {
			return DiffWritableModel.this.fieldIsEmpty(this.objectId, this.fieldId);
		}
		
		@Override
		public XAddress getAddress() {
			return XX.toAddress(DiffWritableModel.this.base.getAddress().getRepository(),
			        DiffWritableModel.this.base.getAddress().getModel(), this.objectId,
			        this.fieldId);
		}
		
		@Override
		public XID getID() {
			return this.fieldId;
		}
		
		@Override
		public boolean setValue(XValue value) {
			return DiffWritableModel.this.fieldSetValue(this.objectId, this.fieldId, value);
		}
		
		@Override
		public XType getType() {
			return XType.XFIELD;
		}
		
	}
	
	protected boolean fieldIsEmpty(XID objectId, XID fieldId) {
		assert hasObject(objectId);
		assert getObject(objectId).hasField(fieldId);
		
		return getObject(objectId).getField(fieldId).isEmpty();
	}
	
	protected boolean fieldSetValue(XID objectId, XID fieldId, XValue value) {
		assert hasObject(objectId);
		assert getObject(objectId).hasField(fieldId);
		
		XValue v = fieldGetValue(objectId, fieldId);
		if(v.equals(value)) {
			return false;
		}
		
		this.added.index(objectId, fieldId, value);
		this.removed.deIndex(objectId, fieldId);
		return true;
	}
	
	protected XValue fieldGetValue(XID objectId, XID fieldId) {
		assert hasObject(objectId) : "model." + objectId + " not found when looking for field "
		        + fieldId;
		assert getObject(objectId).hasField(fieldId);
		
		XValue value = this.added.lookup(objectId, fieldId);
		if(value != null) {
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
	
	public List<XAtomicCommand> toCommandList() {
		List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();
		
		// remove
		Iterator<KeyKeyEntryTuple<XID,XID,XValue>> it = this.removed.tupleIterator(
		        new Wildcard<XID>(), new Wildcard<XID>());
		while(it.hasNext()) {
			KeyKeyEntryTuple<XID,XID,XValue> e = it.next();
			if(e.getKey2().equals(NONE)) {
				// remove object
				list.add(X.getCommandFactory().createForcedRemoveObjectCommand(
				        resolveObject(e.getKey1())));
			} else if(e.getEntry().equals(NOVALUE)) {
				// remove field
				list.add(X.getCommandFactory().createForcedRemoveFieldCommand(
				        resolveField(e.getKey1(), e.getKey2())));
			} else {
				// remove value
				// TODO ???
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
				// add field
				list.add(X.getCommandFactory().createForcedAddFieldCommand(
				        resolveObject(e.getKey1()), e.getKey2()));
			} else {
				// value
				XValue currentValue = baseGetValue(e.getKey1(), e.getKey2());
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
		
		Collections.sort(list, new Comparator<XAtomicCommand>() {
			
			@Override
			public int compare(XAtomicCommand a, XAtomicCommand b) {
				return b.getChangedEntity().getAddressedType()
				        .compareTo(a.getChangedEntity().getAddressedType());
			}
		});
		
		return list;
	}
	
	private XAddress resolveObject(XID objectId) {
		return XX.toAddress(this.base.getAddress().getRepository(), this.base.getID(), objectId,
		        null);
	}
	
	private XAddress resolveField(XID objectId, XID fieldId) {
		return XX.toAddress(this.base.getAddress().getRepository(), this.base.getID(), objectId,
		        fieldId);
	}
	
	private XValue baseGetValue(XID objectId, XID fieldId) {
		XWritableObject o = this.base.getObject(objectId);
		if(o == null)
			return null;
		XWritableField f = o.getField(fieldId);
		if(f == null)
			return null;
		return f.getValue();
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
	
	@Override
	public XType getType() {
		return XType.XMODEL;
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
	
}
