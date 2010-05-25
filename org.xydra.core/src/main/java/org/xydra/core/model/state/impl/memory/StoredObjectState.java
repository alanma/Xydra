package org.xydra.core.model.state.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XFieldState;



/**
 * A lazy implementation. Children are only stored as their IDs and are
 * retrieved on-demand.
 * 
 * @author voelkel
 * 
 */
public class StoredObjectState extends AbstractObjectState {
	
	private Set<XID> fieldStateIDs;
	private MemoryStateStore store;
	
	public StoredObjectState(XAddress objectAddr, MemoryStateStore memoryStateStore) {
		super(objectAddr);
		this.store = memoryStateStore;
		this.fieldStateIDs = new HashSet<XID>();
	}
	
	public void addFieldState(XFieldState fieldState) {
		checkFieldState(fieldState);
		this.fieldStateIDs.add(fieldState.getID());
	}
	
	public boolean hasFieldState(XID fieldStateID) {
		boolean result = this.fieldStateIDs.contains(fieldStateID);
		return result;
	}
	
	public boolean isEmpty() {
		return this.fieldStateIDs.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.fieldStateIDs.iterator();
	}
	
	public void removeFieldState(XID fieldId) {
		this.fieldStateIDs.remove(fieldId);
	}
	
	protected void setChildrenIDs(Iterator<XID> childrenIDs) {
		synchronized(this.fieldStateIDs) {
			this.fieldStateIDs.clear();
			while(childrenIDs.hasNext()) {
				XID xid = childrenIDs.next();
				this.fieldStateIDs.add(xid);
			}
		}
	}
	
	public void delete() {
		this.store.deleteObjectState(getAddress());
	}
	
	public void save() {
		this.store.save(this);
	}
	
	public XFieldState createFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return this.store.createFieldState(fieldAddr);
	}
	
	public XFieldState getFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return this.store.loadFieldState(fieldAddr);
	}
	
}
