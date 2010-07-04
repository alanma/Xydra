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
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;


/**
 * An {@link XBaseObject}/{@link DeltaObject} that represents changes to an
 * {@link XBaseObject}.
 * 
 * An {@link XBaseObject} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XBaseObject} and
 * allow changes on its set of {@link XBaseField XBaseFields}. The changes do
 * not happen directly on the passed {@link XBaseField} but rather on a sort of
 * copy that emulates the passed {@link XBaseObject}. A ChangedObject provides
 * methods to compare the current state to the state the passed
 * {@link XBaseObject} was in at creation time.
 * 
 * @author dscharrer
 * 
 */
public class ChangedObject implements DeltaObject {
	
	private final Set<XID> removed = new HashSet<XID>();
	private final Map<XID,NewField> added = new HashMap<XID,NewField>();
	private final Map<XID,ChangedField> changed = new HashMap<XID,ChangedField>();
	
	private final XBaseObject base;
	
	/**
	 * @param base The {@link XBaseObject} this ChangedObject will encapsulate
	 *            and represent
	 */
	/*
	 * TODO Woudln't it be better to actually copy the given base entitiy?
	 * (think about synchronization problems - somebody might change the base
	 * entity while this "changed" entity is being used, which may result in
	 * complete confusion (?))
	 */
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
	 * @return the {@link XID XIDs} of the {@link XBaseField XBaseFields} that
	 *         existed in the original {@link XBaseObject} but have been removed
	 */
	public Iterable<XID> getRemovedFields() {
		return this.removed;
	}
	
	/**
	 * @return the {@link NewField NewFields} that have been added to this
	 *         ChangedObject and were not contained in the original
	 *         {@link XBaseObject}
	 */
	public Iterable<NewField> getNewFields() {
		return this.added.values();
	}
	
	/**
	 * @return an iterable of the fields that already existed in the original
	 *         {@link XBaseObject} but have been changed. Note: their current
	 *         state might be the same as the original one
	 */
	public Iterable<ChangedField> getChangedFields() {
		return this.changed.values();
	}
	
	/**
	 * Count the minimal number of {@link XCommand XCommands} that would be
	 * needed to transform the original {@link XBaseObject} to the current state
	 * which is represented by this ChangedObject.
	 * 
	 * @param max An upper bound for counting the amount of needed
	 *            {@link XCommands}. Note that setting this bound to little may
	 *            result in the return of an integer which does not actually
	 *            represent the minimal amount of needed {@link XCommand
	 *            XCommands} for the transformation
	 * @result the amount of needed {@link XCommand XCommands} for the
	 *         transformation
	 */
	// TODO I'm not sure if I got the purpose of "max" right
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
	 * @return the revision number of the original {@link XBaseObject}
	 */
	// TODO Maybe a method for returning the revision number this ChangedObject
	// would have if it would be a real object would be a good idea?
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
	 * @return the {@link XBaseField} with the given {@link XID} as it exists in
	 *         the original {@link XBaseField}.
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
