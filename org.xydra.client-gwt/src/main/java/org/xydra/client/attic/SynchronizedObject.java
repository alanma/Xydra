package org.xydra.client.attic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;



public class SynchronizedObject implements XLocalObject, XObjectEventListener,
        XTransactionEventListener, XFieldEventListener {
	
	private final SynchronizedModel model;
	private final Set<XID> removed = new HashSet<XID>();
	private final Set<XID> added = new HashSet<XID>();
	private final Map<XID,SynchronizedField> changed = new HashMap<XID,SynchronizedField>();
	private XObject base;
	private int nchanges = 0;
	private final Set<XFieldEventListener> fieldListeners = new HashSet<XFieldEventListener>();
	private final Set<XObjectEventListener> objectListeners = new HashSet<XObjectEventListener>();
	private final Set<XTransactionEventListener> transListeners = new HashSet<XTransactionEventListener>();
	
	protected boolean isChanged() {
		return this.nchanges > 0;
	}
	
	public SynchronizedObject(SynchronizedModel model, XObject base) {
		this.model = model;
		this.base = base;
		if(base != null) {
			base.addListenerForFieldEvents(this);
			base.addListenerForObjectEvents(this);
			base.addListenerForTransactionEvents(this);
		}
	}
	
	public void detachBase() {
		assert this.nchanges > 0;
		this.base.removeListenerForFieldEvents(this);
		this.base.removeListenerForObjectEvents(this);
		this.base.removeListenerForTransactionEvents(this);
		this.base = null;
	}
	
	public void attachBase(XObject base) {
		this.base = base;
		base.addListenerForFieldEvents(this);
		base.addListenerForObjectEvents(this);
		base.addListenerForTransactionEvents(this);
	}
	
	public SynchronizedField getField(XID fieldId) {
		
		SynchronizedField changedField = this.changed.get(fieldId);
		if(changedField != null) {
			return changedField;
		}
		
		if(this.removed.contains(fieldId)) {
			return null;
		}
		
		XField field = this.base.getField(fieldId);
		if(field == null) {
			return null;
		}
		
		changedField = new SynchronizedField(this.model, field);
		this.changed.put(fieldId, changedField);
		
		return changedField;
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
		return this.added.contains(fieldId)
		        || (!this.removed.contains(fieldId) && this.base.hasField(fieldId));
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public Iterator<XID> iterator() {
		
		Iterator<XID> filtered = new AbstractFilteringIterator<XID>(this.base.iterator()) {
			@Override
			protected boolean matchesFilter(XID entry) {
				return !SynchronizedObject.this.removed.contains(entry);
			}
		};
		
		return new BagUnionIterator<XID>(filtered, this.added.iterator());
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
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectListeners.add(changeListener);
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.objectListeners.remove(changeListener);
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldListeners.add(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldListeners.remove(changeListener);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.transListeners.add(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.transListeners.remove(changeListener);
	}
	
	public void onChangeEvent(XObjectEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public void onChangeEvent(XTransactionEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public void onChangeEvent(XFieldEvent event) {
		if(!isFieldChange(event.getFieldID())) {
			for(XFieldEventListener listener : this.fieldListeners) {
				listener.onChangeEvent(event);
			}
		}
	}
	
	private boolean isFieldChange(XID fieldID) {
		SynchronizedField field = this.changed.get(fieldID);
		return field != null && field.isChanged();
	}
	
}
