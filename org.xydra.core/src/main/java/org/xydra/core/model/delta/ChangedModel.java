package org.xydra.core.model.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
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
public class ChangedModel implements DeltaModel {
	
	private final Set<XID> removed = new HashSet<XID>();
	private final Map<XID,NewObject> added = new HashMap<XID,NewObject>();
	private final Map<XID,ChangedObject> changed = new HashMap<XID,ChangedObject>();
	
	private final XBaseModel base;
	
	public ChangedModel(XBaseModel base) {
		this.base = base;
	}
	
	public void createObject(XID objectId) {
		if(!hasObject(objectId)) {
			XAddress objectAddr = XX.resolveObject(getAddress(), objectId);
			this.added.put(objectId, new NewObject(objectAddr));
		}
	}
	
	/**
	 * @return the {@link XID}s of the objects that existed in the original
	 *         model but have been removed
	 */
	public Iterable<XID> getRemovedObjects() {
		return this.removed;
	}
	
	/**
	 * @return the new objects that have been added
	 */
	public Iterable<NewObject> getNewObjects() {
		return this.added.values();
	}
	
	/**
	 * @return the objects that existed in the original but have been changed.
	 *         note that their current state might be the same of the original
	 *         one
	 */
	public Iterable<ChangedObject> getChangedObjects() {
		return this.changed.values();
	}
	
	/**
	 * Count the minimal number of {@link XCommand}s that would be needed to
	 * transform the original model to the the state represented by this changed
	 * model.
	 */
	public int countChanges(int max) {
		int n = this.removed.size();
		if(n < max) {
			for(NewObject object : this.added.values()) {
				n += object.countChanges(max - n);
				if(n >= max) {
					break;
				}
			}
			if(n < max) {
				for(ChangedObject object : this.changed.values()) {
					n += object.countChanges(max - n);
					if(n >= max) {
						break;
					}
				}
			}
		}
		return n;
	}
	
	public DeltaObject getObject(XID objectId) {
		
		NewObject newObject = this.added.get(objectId);
		if(newObject != null) {
			return newObject;
		}
		
		ChangedObject changedObject = this.changed.get(objectId);
		if(changedObject != null) {
			return changedObject;
		}
		
		if(this.removed.contains(objectId)) {
			return null;
		}
		
		XBaseObject object = this.base.getObject(objectId);
		if(object == null) {
			return null;
		}
		
		changedObject = new ChangedObject(object);
		this.changed.put(objectId, changedObject);
		
		return changedObject;
	}
	
	public void removeObject(XID objectId) {
		if(hasObject(objectId)) {
			if(!this.added.containsKey(objectId)) {
				this.removed.add(objectId);
			} else {
				this.added.remove(objectId);
			}
			this.changed.remove(objectId);
		}
	}
	
	/**
	 * Get the revision number of the original model.
	 */
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	public boolean hasObject(XID objectId) {
		return this.added.containsKey(objectId)
		        || (!this.removed.contains(objectId) && this.base.hasObject(objectId));
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public Iterator<XID> iterator() {
		
		Iterator<XID> filtered = new AbstractFilteringIterator<XID>(this.base.iterator()) {
			@Override
			protected boolean matchesFilter(XID entry) {
				return !ChangedModel.this.removed.contains(entry);
			}
		};
		
		return new BagUnionIterator<XID>(filtered, this.added.keySet().iterator());
	}
	
	/**
	 * Get the object with the given {@link XID} as it exists in the old model.
	 */
	public XBaseObject getOldObject(XID objectId) {
		return this.base.getObject(objectId);
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
		
		for(XID objectId : this.base) {
			if(!this.removed.contains(objectId)) {
				return false;
			}
		}
		
		return true;
	}
	
}
