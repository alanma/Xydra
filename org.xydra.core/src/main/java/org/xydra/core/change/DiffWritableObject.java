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
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XValue;


/**
 * A helper class to minimise the number and size of persistence accesses.
 * 
 * An implementation of {@link XWritableObject} that works as a diff on top of a
 * base {@link XWritableObject}. Via {@link #toCommandList(boolean)} a minimal
 * list of commands that changes the base model into the current state can be
 * created. The base model is changed at no times.
 * 
 * @author xamde
 */
public class DiffWritableObject implements XWritableObject {
	
	private class WrappedField implements XWritableField {
		
		private XID fieldId;
		
		public WrappedField(XID fieldId) {
			this.fieldId = fieldId;
		}
		
		@Override
		public XAddress getAddress() {
			XAddress parent = DiffWritableObject.this.base.getAddress();
			return XX.resolveField(parent, this.fieldId);
		}
		
		@Override
		public XID getID() {
			return this.fieldId;
		}
		
		@Override
		public long getRevisionNumber() {
			throw new UnsupportedOperationException("not implementable for DiffWritableObject");
		}
		
		@Override
		public XValue getValue() {
			return DiffWritableObject.this.fieldGetValue(this.fieldId);
		}
		
		@Override
		public boolean isEmpty() {
			XValue value = getValue();
			return value == null;
		}
		
		@Override
		public boolean setValue(XValue value) {
			return DiffWritableObject.this.fieldSetValue(this.fieldId, value);
		}
		
		@Override
		public XType getType() {
			return XType.XFIELD;
		}
		
	}
	
	protected static <E> Set<E> toSet(Iterator<E> it) {
		Set<E> set = new HashSet<E>();
		while(it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}
	
	/* representing added stuff (also changed values) */
	private XWritableObject added;
	
	private final XWritableObject base;
	
	/*
	 * representing removed stuff. Empty objects denote a deletion of the
	 * complete object. Objects that have fields denote only deletion of these
	 * fields. Individual removal of values but keeping of the fields is not
	 * supported in this implementation.
	 */
	private XWritableObject removed;
	
	public DiffWritableObject(final XWritableObject base) {
		this.base = base;
		this.added = new SimpleObject(base.getAddress());
		this.removed = new SimpleObject(base.getAddress());
	}
	
	private XValue baseGetValue(XID fieldId) {
		XWritableField f = this.base.getField(fieldId);
		if(f == null)
			return null;
		return f.getValue();
	}
	
	@Override
	public XWritableField createField(XID fieldId) {
		XWritableField f = this.getField(fieldId);
		if(f == null) {
			f = this.added.createField(fieldId);
			this.removed.removeField(fieldId);
		}
		return new WrappedField(f.getID());
		
	}
	
	protected XValue fieldGetValue(XID fieldId) {
		XWritableField f;
		if(this.added.hasField(fieldId)) {
			f = this.added.getField(fieldId);
		} else {
			f = this.base.getField(fieldId);
		}
		if(f == null) {
			return null;
		}
		return f.getValue();
	}
	
	protected boolean fieldSetValue(XID fieldId, XValue value) {
		assert hasField(fieldId);
		
		XWritableField f;
		if(this.added.hasField(fieldId)) {
			f = this.added.getField(fieldId);
		} else {
			f = this.added.createField(fieldId);
		}
		return f.setValue(value);
	}
	
	@Override
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	@Override
	public XWritableField getField(XID fieldId) {
		XWritableField f = getFieldInternal(fieldId);
		if(f == null) {
			return null;
		}
		return new WrappedField(f.getID());
	}
	
	private XWritableField getFieldInternal(XID fieldId) {
		XWritableField f = this.added.getField(fieldId);
		if(f != null) {
			// fine
			assert !this.removed.hasField(fieldId);
		} else {
			if(this.removed.hasField(fieldId)) {
				assert !this.added.hasField(fieldId);
				return null;
			} else {
				f = this.base.getField(fieldId);
				if(f == null) {
					return null;
				}
			}
		}
		return f;
	}
	
	@Override
	public XID getID() {
		return this.base.getID();
	}
	
	@Override
	public long getRevisionNumber() {
		throw new UnsupportedOperationException("not implementable for DiffWritableObject");
	}
	
	@Override
	public boolean hasField(XID fieldId) {
		if(this.added.hasField(fieldId)) {
			return true;
		} else if(this.removed.hasField(fieldId)) {
			return false;
		} else {
			return this.base.hasField(fieldId);
		}
	}
	
	protected Set<XID> ids() {
		Set<XID> set = toSet(this.base.iterator());
		set.removeAll(toSet(this.removed.iterator()));
		set.addAll(toSet(this.added.iterator()));
		return set;
	}
	
	@Override
	public boolean isEmpty() {
		return this.ids().isEmpty();
	}
	
	protected boolean isEmpty(XID objectId) {
		return fieldIds().isEmpty();
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.ids().iterator();
	}
	
	protected Iterator<XID> iterator(XID objectId) {
		return fieldIds().iterator();
	}
	
	protected Set<XID> fieldIds() {
		Set<XID> set = new HashSet<XID>();
		
		set.addAll(toSet(this.base.iterator()));
		set.removeAll(toSet(this.removed.iterator()));
		set.addAll(toSet(this.added.iterator()));
		return set;
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		// if added, add less
		if(this.added.hasField(fieldId)) {
			this.added.removeField(fieldId);
			return true;
		}
		// if removed ...
		if(this.removed.hasField(fieldId)) {
			// already scheduled for deletion
			return false;
		} else {
			this.removed.createField(fieldId);
			return true;
		}
	}
	
	/**
	 * @param forced if true, creates forced commands, otherwise safe commands
	 *            are used.
	 * @return this diff as a list of commands that mimic the state change since
	 *         the creation of this diff object
	 */
	public List<XAtomicCommand> toCommandList(boolean forced) {
		List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();
		
		// remove
		for(XID fId : this.removed) {
			XAddress fAddress = XX.resolveField(getAddress(), fId);
			// remove field
			if(forced) {
				list.add(X.getCommandFactory().createForcedRemoveFieldCommand(fAddress));
			} else {
				list.add(X.getCommandFactory().createSafeRemoveFieldCommand(fAddress,
				        this.base.getField(fId).getRevisionNumber()));
			}
		}
		
		// add
		for(XID fId : this.added) {
			XWritableField f = getField(fId);
			assert f != null;
			// add field
			// FIXME !!! handle 'forced'
			list.add(X.getCommandFactory().createForcedAddFieldCommand(getAddress(), fId));
			// set value
			if(!f.isEmpty()) {
				XValue currentValue = baseGetValue(fId);
				if(currentValue == null) {
					// add value
					// FIXME !!! handle 'forced'
					list.add(X.getCommandFactory().createForcedAddValueCommand(f.getAddress(),
					        f.getValue()));
				} else {
					// change value
					if(forced) {
						list.add(X.getCommandFactory().createForcedChangeValueCommand(
						        f.getAddress(), f.getValue()));
					} else {
						list.add(X.getCommandFactory().createSafeChangeValueCommand(f.getAddress(),
						        this.base.getField(fId).getRevisionNumber(), f.getValue()));
					}
					
				}
			}
		}
		
		return list;
	}
	
	/**
	 * @param forced if true, creates forced commands, otherwise safe commands
	 *            are used.
	 * @return this diffObject as a transaction. This object is in no way reset
	 *         afterwards.
	 */
	public XTransaction toTransaction(boolean forced) {
		XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
		List<XAtomicCommand> list = toCommandList(forced);
		for(XAtomicCommand command : list) {
			builder.addCommand(command);
		}
		return builder.build();
	}
	
	@Override
	public XType getType() {
		return XType.XOBJECT;
	}
	
	/**
	 * Allows to end a transaction and go back to using the base object.
	 * 
	 * @return the base object that has been used to created this wrapped
	 *         {@link DiffWritableObject}.
	 */
	public XWritableObject getBase() {
		return this.base;
	}
	
	@Override
	public String toString() {
		return "base: " + this.base.toString()

		+ "\n added: " + this.added.toString()

		+ "\n removed: " + this.removed.toString();
	}
	
}
