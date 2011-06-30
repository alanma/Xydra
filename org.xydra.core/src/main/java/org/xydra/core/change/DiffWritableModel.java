package org.xydra.core.change;

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
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XValue;


/**
 * A helper class to minimise the number and size of persistence accesses.
 * 
 * An implementation of {@link XWritableModel} that works as a diff on top of a
 * base {@link XWritableModel}. Via {@link #toCommandList()} a minimal list of
 * commands that changes the base model into the current state can be created.
 * The base model is changed at no times.
 * 
 * @author xamde
 * @deprecated trying to find all references...
 */
@Deprecated
public class DiffWritableModel implements XWritableModel {
	
	/* representing added stuff */
	private XWritableModel added;
	/*
	 * representing removed stuff. Empty objects denote a deletion of the
	 * complete object. Objects that have fields denote only deletion of these
	 * fields. Individual removal of values but keeping of the fields is not
	 * supported in this implementation.
	 */
	private XWritableModel removed;
	private final XWritableModel base;
	
	public DiffWritableModel(final XWritableModel base) {
		this.base = base;
		this.added = new SimpleModel(base.getAddress());
		this.removed = new SimpleModel(base.getAddress());
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	public XWritableObject createObject(XID objectId) {
		XWritableObject o = this.getObject(objectId);
		if(o == null) {
			o = this.added.createObject(objectId);
			this.removed.removeObject(objectId);
		}
		return new WrappedObject(o.getID());
	}
	
	public XWritableObject getObject(XID objectId) {
		XWritableObject o = getObjectInternal(objectId);
		if(o == null) {
			return null;
		}
		return new WrappedObject(o.getID());
	}
	
	private XWritableObject getObjectInternal(XID objectId) {
		XWritableObject o = this.added.getObject(objectId);
		if(o != null) {
			// fine
			assert !this.removed.hasObject(objectId);
		} else {
			if(this.removed.hasObject(objectId)) {
				assert !this.added.hasObject(objectId);
				return null;
			} else {
				o = this.base.getObject(objectId);
				if(o == null) {
					return null;
				}
			}
		}
		return o;
	}
	
	public boolean removeObject(XID objectId) {
		if(this.added.hasObject(objectId)) {
			this.added.removeObject(objectId);
			return true;
		}
		if(this.removed.hasObject(objectId)) {
			// make sure its emtpy to denote the complete deletion
			XWritableObject o = this.removed.getObject(objectId);
			if(!o.isEmpty()) {
				Set<XID> toDelete = toSet(o.iterator());
				for(XID fId : toDelete) {
					o.removeField(fId);
				}
				return true;
			} else {
				return false;
			}
		}
		if(this.base.hasObject(objectId)) {
			this.removed.createObject(objectId);
			return true;
		}
		
		return false;
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
		set.removeAll(toSet(this.removed.iterator()));
		set.addAll(toSet(this.added.iterator()));
		return set;
	}
	
	public boolean isEmpty() {
		return this.ids().isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.ids().iterator();
	}
	
	@Override
	public long getRevisionNumber() {
		throw new UnsupportedOperationException("not implementable for DiffWritableModel");
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		if(this.added.hasObject(objectId)) {
			return true;
		} else if(this.removed.hasObject(objectId)) {
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
		if(hasObject(objectId)) {
			return getObjectInternal(objectId).hasField(fieldId);
		}
		return false;
	}
	
	protected boolean objectRemoveField(XID objectId, XID fieldId) {
		// if added, add less
		if(this.added.hasObject(objectId)) {
			return this.added.getObject(objectId).removeField(fieldId);
		}
		// if removed ...
		if(this.removed.hasObject(objectId)) {
			XWritableObject o = this.removed.getObject(objectId);
			if(o.isEmpty()) {
				// object will be removed completely
				return this.base.getObject(objectId).hasField(fieldId);
			} else {
				// some fields will be removed, maybe one more
				if(o.hasField(fieldId)) {
					// already scheduled for deletion
					return false;
				} else {
					o.createField(fieldId);
					return true;
				}
			}
		} else {
			this.removed.createObject(objectId).createField(fieldId);
			return true;
		}
	}
	
	protected XWritableField objectGetField(XID objectId, XID fieldId) {
		XWritableObject o = this.added.getObject(objectId);
		if(o != null) {
			XWritableField f = o.getField(fieldId);
			if(f != null) {
				return new WrappedField(objectId, fieldId);
			}
		}
		
		o = this.removed.getObject(objectId);
		if(o != null) {
			if(o.isEmpty()) {
				// no fields will be left
				return null;
			} else {
				if(o.hasField(fieldId)) {
					return null;
				}
			}
		}
		
		o = this.base.getObject(objectId);
		if(o != null) {
			XWritableField f = o.getField(fieldId);
			if(f == null) {
				return null;
			} else {
				return new WrappedField(objectId, fieldId);
			}
		}
		
		return null;
	}
	
	protected XWritableField objectCreateField(XID objectId, XID fieldId) {
		XWritableObject o = this.added.getObject(objectId);
		if(o != null) {
			// fine
		} else {
			assert !this.removed.hasObject(objectId) : "accesed via a WrappedObject";
			assert this.base.hasObject(objectId);
			o = this.added.createObject(objectId);
		}
		XWritableField f = o.createField(fieldId);
		return new WrappedField(objectId, f.getID());
	}
	
	protected Set<XID> objectIds(XID objectId) {
		Set<XID> set = new HashSet<XID>();
		
		if(this.base.hasObject(objectId)) {
			set.addAll(toSet(this.base.getObject(objectId).iterator()));
		}
		if(this.removed.hasObject(objectId)) {
			set.removeAll(toSet(this.removed.getObject(objectId).iterator()));
		}
		if(this.added.hasObject(objectId)) {
			set.addAll(toSet(this.added.getObject(objectId).iterator()));
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
		
		public WrappedField(XID obejctId, XID fieldId) {
			this.objectId = obejctId;
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
		
		XWritableField f;
		if(this.added.hasObject(objectId)) {
			f = this.added.getObject(objectId).getField(fieldId);
		} else {
			f = this.added.createObject(objectId).createField(fieldId);
		}
		return f.setValue(value);
	}
	
	protected XValue fieldGetValue(XID objectId, XID fieldId) {
		assert hasObject(objectId);
		assert getObject(objectId).hasField(fieldId);
		
		XWritableField f;
		if(this.added.hasObject(objectId)) {
			f = this.added.getObject(objectId).getField(fieldId);
		} else {
			f = this.base.getObject(objectId).getField(fieldId);
		}
		return f.getValue();
	}
	
	public List<XAtomicCommand> toCommandList() {
		List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();
		
		// remove
		for(XID oId : this.removed) {
			XWritableObject o = this.removed.getObject(oId);
			if(o.isEmpty()) {
				// remove object
				list.add(X.getCommandFactory().createForcedRemoveObjectCommand(o.getAddress()));
			} else {
				for(XID fId : o) {
					XWritableField f = o.getField(fId);
					// remove field
					list.add(X.getCommandFactory().createForcedRemoveFieldCommand(f.getAddress()));
				}
			}
		}
		
		// add
		for(XID oId : this.added) {
			// add object
			list.add(X.getCommandFactory().createForcedAddObjectCommand(getAddress(), oId));
			XWritableObject o = this.added.getObject(oId);
			for(XID fId : o) {
				XWritableField f = o.getField(fId);
				// add field
				list.add(X.getCommandFactory().createForcedAddFieldCommand(o.getAddress(), fId));
				// set value
				if(!f.isEmpty()) {
					XValue currentValue = baseGetValue(oId, fId);
					if(currentValue == null) {
						// add value
						list.add(X.getCommandFactory().createForcedAddValueCommand(f.getAddress(),
						        f.getValue()));
					} else {
						// change value
						list.add(X.getCommandFactory().createForcedChangeValueCommand(
						        f.getAddress(), f.getValue()));
					}
				}
			}
		}
		return list;
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
	
	public XTransaction toTransaction() {
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		List<XAtomicCommand> list = toCommandList();
		for(XAtomicCommand command : list) {
			builder.addCommand(command);
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
