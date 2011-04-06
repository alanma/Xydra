package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;


/**
 * An {@link XWritableObject} that represents changes to an
 * {@link XReadableObject}.
 * 
 * An {@link XReadableObject} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XReadableObject}
 * and allow changes on its set of {@link XReadableField XBaseFields}. The
 * changes do not happen directly on the passed {@link XReadableField} but
 * rather on a sort of copy that emulates the passed {@link XReadableObject}. A
 * ChangedObject provides methods to compare the current state to the state the
 * passed {@link XReadableObject} was in at creation time.
 * 
 * @author dscharrer
 * 
 */
public class ChangedObject implements XWritableObject {
	
	// Fields that are not in base and have been added.
	// Contains no XIDs that are in removed or changed.
	private final Map<XID,SimpleField> added = new HashMap<XID,SimpleField>();
	
	private final XReadableObject base;
	
	// Fields that are in base and have not been removed.
	// While they were changed once, those changes might have been reverted.
	// Contains no XIDs that are in added or removed.
	private final Map<XID,ChangedField> changed = new HashMap<XID,ChangedField>();
	
	// Fields that are in base but have been removed.
	// Contains no XIDs that are in added or changed.
	private final Set<XID> removed = new HashSet<XID>();
	
	/**
	 * Wrap an {@link XReadableObject} to record a set of changes made. Multiple
	 * changes will be combined as much as possible such that a minimal set of
	 * changes remains.
	 * 
	 * Note that this is a very lightweight wrapper intended for a short
	 * lifetime. As a consequence, the wrapped {@link XReadableObject} is not
	 * copied and changes to it or any contained fields (as opposed to this
	 * {@link ChangedObject}) may result in undefined behavior of the
	 * {@link ChangedObject}.
	 * 
	 * @param base The {@link XReadableObject} this ChangedObject will
	 *            encapsulate and represent
	 */
	public ChangedObject(XReadableObject base) {
		assert base != null;
		this.base = base;
	}
	
	private boolean checkSetInvariants() {
		
		for(XID id : this.removed) {
			assert !this.added.containsKey(id) && !this.changed.containsKey(id);
			assert this.base.hasField(id);
		}
		
		for(XID id : this.added.keySet()) {
			assert !this.removed.contains(id) && !this.changed.containsKey(id);
			assert !this.base.hasField(id);
			assert id.equals(this.added.get(id).getID());
		}
		
		for(XID id : this.changed.keySet()) {
			assert !this.removed.contains(id) && !this.added.containsKey(id);
			assert this.base.hasField(id);
			assert id.equals(this.changed.get(id).getID());
		}
		
		return true;
	}
	
	/**
	 * Remove all fields.
	 */
	public void clear() {
		
		this.added.clear();
		this.changed.clear();
		for(XID id : this.base) {
			// IMPROVE maybe add a "cleared" flag to remove all fields more
			// efficiently?
			this.removed.add(id);
		}
		
		assert checkSetInvariants();
	}
	
	/**
	 * Count the minimal number of {@link XCommand XCommands} that would be
	 * needed to transform the original {@link XReadableObject} to the current
	 * state which is represented by this ChangedObject.
	 * 
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XCommand XCommands}. Note that setting this bound to
	 *            little may result in the return of an integer which does not
	 *            actually represent the minimal amount of needed
	 *            {@link XCommand XCommands} for the transformation.
	 * @result the amount of needed {@link XCommand XCommands} for the
	 *         transformation
	 */
	public int countCommandsNeeded(int max) {
		int n = this.removed.size() + this.added.size();
		if(n < max) {
			for(XReadableField field : this.added.values()) {
				if(!field.isEmpty()) {
					n++;
					if(n >= max) {
						return n;
					}
				}
			}
			for(ChangedField field : this.changed.values()) {
				n += field.isChanged() ? 1 : 0;
				if(n >= max) {
					return n;
				}
			}
		}
		return n;
	}
	
	/**
	 * Count the number of {@link XEvent XEvents} that would be needed to log
	 * the transformation of the original {@link XReadableModel} to the current
	 * state which is represented by this ChangedModel.
	 * 
	 * This is different to {@link #countCommandsNeeded} in that a removed
	 * object or field may cause several events while only needing one command.
	 * 
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XEvent XEvents}. Note that setting this bound to little
	 *            may result in the return of an integer which does not actually
	 *            represent the minimal amount of needed {@link XEvent XEvents}
	 *            for the transformation.
	 * @result the amount of needed {@link XEvent XEvents} for the
	 *         transformation
	 */
	public int countEventsNeeded(int max) {
		int n = this.removed.size() + this.added.size();
		if(n < max) {
			for(XID fieldId : this.removed) {
				// removing field itself already counted
				XReadableField oldField = getOldField(fieldId);
				if(!oldField.isEmpty()) {
					n++; // removing the value
					if(n >= max) {
						return n;
					}
				}
			}
			for(XReadableField field : this.added.values()) {
				if(!field.isEmpty()) {
					n++;
					if(n >= max) {
						return n;
					}
				}
			}
			for(ChangedField field : this.changed.values()) {
				n += field.isChanged() ? 1 : 0;
				if(n >= max) {
					return n;
				}
			}
		}
		return n;
	}
	
	public XWritableField createField(XID fieldId) {
		
		XWritableField oldField = getField(fieldId);
		if(oldField != null) {
			return oldField;
		}
		
		XReadableField field = this.base.getField(fieldId);
		if(field != null) {
			
			// If the field previously existed it must have been removed
			// previously and we can merge the remove and add changes.
			assert this.removed.contains(fieldId);
			assert !this.changed.containsKey(fieldId);
			this.removed.remove(fieldId);
			ChangedField newField = new ChangedField(field);
			newField.setValue(null);
			this.changed.put(fieldId, newField);
			
			assert checkSetInvariants();
			
			return newField;
			
		} else {
			
			// Otherwise, the field is completely new.
			XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
			SimpleField newField = new SimpleField(fieldAddr);
			this.added.put(fieldId, newField);
			
			assert checkSetInvariants();
			
			return newField;
		}
		
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	/**
	 * @return an {@link Iterable} of the fields that already existed in the
	 *         original {@link XReadableObject} but have been changed. Note:
	 *         their current state might be the same as the original one. Use
	 *         {@link ChangedField#isChanged()} to check if they are actually
	 *         different form the original field.
	 */
	public Iterable<ChangedField> getChangedFields() {
		return this.changed.values();
	}
	
	public XWritableField getField(XID fieldId) {
		assert fieldId != null;
		assert this.base != null;
		
		SimpleField newField = this.added.get(fieldId);
		if(newField != null) {
			return newField;
		}
		
		ChangedField changedField = this.changed.get(fieldId);
		if(changedField != null) {
			return changedField;
		}
		
		if(this.removed.contains(fieldId)) {
			return null;
		}
		
		XReadableField field = this.base.getField(fieldId);
		if(field == null) {
			return null;
		}
		
		changedField = new ChangedField(field);
		this.changed.put(fieldId, changedField);
		
		assert checkSetInvariants();
		
		return changedField;
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	/**
	 * @return the {@link SimpleField SimpleFields} that have been added to this
	 *         ChangedObject and were not contained in the original
	 *         {@link XReadableObject}
	 */
	public Iterable<SimpleField> getNewFields() {
		return this.added.values();
	}
	
	/**
	 * @return the {@link XReadableField} with the given {@link XID} as it
	 *         exists in the original {@link XReadableField}.
	 */
	public XReadableField getOldField(XID fieldId) {
		return this.base.getField(fieldId);
	}
	
	/**
	 * @return the {@link XID XIDs} of the {@link XReadableField XBaseFields}
	 *         that existed in the original {@link XReadableObject} but have
	 *         been removed
	 */
	public Iterable<XID> getRemovedFields() {
		return this.removed;
	}
	
	/**
	 * Return the revision number of the wrapped {@link XReadableObject}. The
	 * revision number does not increase with changes to this
	 * {@link ChangedObject}.
	 * 
	 * @return the revision number of the original {@link XReadableObject}
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public boolean hasField(XID fieldId) {
		return this.added.containsKey(fieldId)
		        || (!this.removed.contains(fieldId) && this.base.hasField(fieldId));
	}
	
	public boolean isEmpty() {
		
		if(!this.added.isEmpty()) {
			return false;
		}
		
		if(this.removed.isEmpty()) {
			return this.base.isEmpty();
		}
		
		if(this.changed.size() > this.removed.size()) {
			return false;
		}
		
		for(XID fieldId : this.base) {
			if(!this.removed.contains(fieldId)) {
				return false;
			}
		}
		
		return true;
	}
	
	public Iterator<XID> iterator() {
		
		Iterator<XID> filtered = new AbstractFilteringIterator<XID>(this.base.iterator()) {
			@Override
			protected boolean matchesFilter(XID entry) {
				return !ChangedObject.this.removed.contains(entry);
			}
		};
		
		return new BagUnionIterator<XID>(filtered, this.added.keySet().iterator());
	}
	
	public boolean removeField(XID fieldId) {
		
		if(this.added.containsKey(fieldId)) {
			
			// Never existed in base, so removing from added is sufficient.
			assert !this.base.hasField(fieldId) && !this.changed.containsKey(fieldId);
			assert !this.removed.contains(fieldId);
			
			this.added.remove(fieldId);
			
			assert checkSetInvariants();
			
			return true;
			
		} else if(!this.removed.contains(fieldId) && this.base.hasField(fieldId)) {
			
			// Exists in base and not removed yet.
			assert !this.added.containsKey(fieldId);
			
			this.removed.add(fieldId);
			this.changed.remove(fieldId);
			
			assert checkSetInvariants();
			
			return true;
			
		}
		
		return false;
	}
	
	public boolean isChanged() {
		boolean changed = !this.added.isEmpty();
		changed |= !this.removed.isEmpty();
		for(ChangedField cf : this.changed.values()) {
			changed |= cf.isChanged();
		}
		return changed;
	}
}
