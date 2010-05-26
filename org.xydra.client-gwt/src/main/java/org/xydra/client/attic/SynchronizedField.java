package org.xydra.client.attic;

import java.util.HashSet;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;



public class SynchronizedField implements XLocalField, XFieldEventListener {
	
	private XValue value;
	private XField base;
	private long revision;
	private int nchanges = 0;
	private final Set<XFieldEventListener> fieldListeners = new HashSet<XFieldEventListener>();
	
	public SynchronizedField(SynchronizedModel model, XField base) {
		this.base = base;
		if(base != null) {
			base.addListenerForFieldEvents(this);
		}
	}
	
	protected boolean isChanged() {
		return this.nchanges > 0;
	}
	
	public void detachBase() {
		assert isChanged();
		this.base.removeListenerForFieldEvents(this);
		this.base = null;
	}
	
	public void attachBase(XField base) {
		this.base = base;
		base.addListenerForFieldEvents(this);
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	/**
	 * The current revision number of the original field.
	 */
	public long getRevisionNumber() {
		if(isChanged()) {
			return this.revision;
		}
		return this.base.getRevisionNumber();
	}
	
	public XValue getValue() {
		return this.nchanges > 0 ? this.value : this.base.getValue();
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public boolean isEmpty() {
		return this.nchanges > 0 ? this.value == null : this.base.isEmpty();
	}
	
	public void onChangeEvent(XFieldEvent event) {
		if(!isChanged()) {
			for(XFieldEventListener listener : this.fieldListeners) {
				listener.onChangeEvent(event);
			}
		}
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldListeners.add(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.fieldListeners.remove(changeListener);
	}
	
	protected void setValue(XID actor, XValue newValue) {
		assert !XX.equals(getValue(), newValue);
		this.value = newValue;
		if(!isChanged()) {
			// TODO register @ model
		}
		this.nchanges++;
		// TODO fixRev
		// TODO send Event
	}
	
	public void revertFirstChange() {
		// TODO more to do here...
		assert isChanged();
		this.nchanges--;
		if(this.nchanges == 0) {
			// TODO unregister @ model
			if(!XX.equals(this.value, this.base.getValue())) {
				// TODO send Event
			}
			// cleanup
			this.value = null;
			this.revision = 0;
		}
		// TODO fix revision?
	}
	
}
