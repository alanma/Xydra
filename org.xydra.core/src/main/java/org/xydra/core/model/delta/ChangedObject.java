package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;


/**
 * An {@link XBaseModel} that represents changes to a model.
 * 
 * @author dscharrer
 * 
 */
public class ChangedObject implements DeltaObject {
	
	private final Set<XID> removed = new HashSet<XID>();
	private final Map<XID,NewField> added = new HashMap<XID,NewField>();
	private final Map<XID,ChangedField> changed = new HashMap<XID,ChangedField>();
	
	private final XBaseObject base;
	
	public ChangedObject(XBaseObject base) {
		this.base = base;
	}
	
	public void createField(XID fieldId) {
		if(!hasField(fieldId)) {
			XAddress fieldAddr = XX.resolveField(getAddress(), fieldId);
			this.added.put(fieldId, new NewField(fieldAddr));
		}
	}
	
	/**
	 * @return the {@link XID}s of the fields that existed in the original
	 *         object but have been removed
	 */
	public Iterable<XID> getRemovedFields() {
		return this.removed;
	}
	
	/**
	 * @return the new fields that have been added
	 */
	public Iterable<NewField> getNewFields() {
		return this.added.values();
	}
	
	/**
	 * @return the fields that existed in the original but have been changed.
	 *         note that their current state might be the same of the original
	 *         one
	 */
	public Iterable<ChangedField> getChangedFields() {
		return this.changed.values();
	}
	
	/**
	 * Count the minimal number of {@link XCommand}s that would be needed to
	 * transform the original object to the the state represented by this
	 * changed object.
	 */
	public int countChanges(int max) {
		int n = this.removed.size();
		if(n < max) {
			for(NewField field : this.added.values()) {
				n += field.countChanges();
				if(n >= max) {
					break;
				}
			}
			if(n < max) {
				for(ChangedField field : this.changed.values()) {
					n += field.countChanges(max - n);
					if(n >= max) {
						break;
					}
				}
			}
		}
		return n;
	}
	
	public DeltaField getField(XID fieldId) {
		
		NewField newField = this.added.get(fieldId);
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
		
		XBaseField field = this.base.getField(fieldId);
		if(field == null) {
			return null;
		}
		
		changedField = new ChangedField(field);
		this.changed.put(fieldId, changedField);
		
		return changedField;
	}
	
	public void removeField(XID fieldId) {
		if(hasField(fieldId)) {
			if(!this.added.containsKey(fieldId)) {
				this.removed.add(fieldId);
			} else {
				this.added.remove(fieldId);
			}
			this.changed.remove(fieldId);
		}
	}
	
	/**
	 * Get the revision number of the original object.
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	public boolean hasField(XID fieldId) {
		return this.added.containsKey(fieldId)
		        || (!this.removed.contains(fieldId) && this.base.hasField(fieldId));
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
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
	
	/**
	 * Get the field with the given {@link XID} as it exists in the old object.
	 */
	public XBaseField getOldField(XID fieldId) {
		return this.base.getField(fieldId);
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
	
}
