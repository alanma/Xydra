package org.xydra.client.attic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;



public class SynchronizedModel implements XLocalModel {
	
	private final Set<XID> removed = new HashSet<XID>();
	private final Set<XID> added = new HashSet<XID>();
	private final Map<XID,SynchronizedObject> changed = new HashMap<XID,SynchronizedObject>();
	
	private final XModel base;
	
	public SynchronizedModel(XModel base) {
		this.base = base;
	}
	
	public SynchronizedObject getObject(XID objectId) {
		
		SynchronizedObject changedObject = this.changed.get(objectId);
		if(changedObject != null) {
			return changedObject;
		}
		
		if(this.removed.contains(objectId)) {
			return null;
		}
		
		XObject object = this.base.getObject(objectId);
		if(object == null) {
			return null;
		}
		
		changedObject = new SynchronizedObject(this, object);
		this.changed.put(objectId, changedObject);
		
		return changedObject;
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
		return this.added.contains(objectId)
		        || (!this.removed.contains(objectId) && this.base.hasObject(objectId));
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public Iterator<XID> iterator() {
		
		Iterator<XID> filtered = new AbstractFilteringIterator<XID>(this.base.iterator()) {
			@Override
			protected boolean matchesFilter(XID entry) {
				return !SynchronizedModel.this.removed.contains(entry);
			}
		};
		
		return new BagUnionIterator<XID>(filtered, this.added.iterator());
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
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
