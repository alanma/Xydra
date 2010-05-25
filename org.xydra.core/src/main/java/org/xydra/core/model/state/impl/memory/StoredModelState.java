package org.xydra.core.model.state.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XObjectState;



public class StoredModelState extends AbstractModelState {
	
	private static final long serialVersionUID = -509532259783788492L;
	
	private Set<XID> objectStateIDs;
	
	private MemoryStateStore store;
	
	private MemoryChangeLogState changeLogState;
	
	/**
	 * @param modelStateID
	 * @param memoryStateStore where persistence happens on
	 *            {@link StoredModelState#save()} and
	 *            {@link StoredModelState#delete()}.
	 */
	public StoredModelState(XAddress modelAddr, MemoryStateStore memoryStateStore) {
		super(modelAddr);
		this.store = memoryStateStore;
		this.objectStateIDs = new HashSet<XID>();
	}
	
	public StoredModelState(XAddress modelAddr, MemoryStateStore memoryStateStore,
	        long revisionNumber) {
		super(modelAddr, revisionNumber);
		this.store = memoryStateStore;
		this.objectStateIDs = new HashSet<XID>();
	}
	
	public void addObjectState(XObjectState objectState) {
		checkObjectState(objectState);
		this.objectStateIDs.add(objectState.getID());
	}
	
	public XObjectState getObjectState(XID objectStateID) {
		XAddress address = XX.resolveObject(getAddress(), objectStateID);
		return this.store.loadObjectState(address);
	}
	
	public boolean hasObjectState(XID objectStateID) {
		boolean result = this.objectStateIDs.contains(objectStateID);
		return result;
	}
	
	public boolean isEmpty() {
		return this.objectStateIDs.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.objectStateIDs.iterator();
	}
	
	public void removeObjectState(XID objectId) {
		this.objectStateIDs.remove(objectId);
	}
	
	protected void setChildrenIDs(Iterator<XID> childrenIDs) {
		synchronized(this.objectStateIDs) {
			this.objectStateIDs.clear();
			while(childrenIDs.hasNext()) {
				XID xid = childrenIDs.next();
				this.objectStateIDs.add(xid);
			}
		}
	}
	
	public void delete() {
		this.store.deleteModelState(getAddress());
	}
	
	public void save() {
		this.store.save(this);
	}
	
	public XObjectState createObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return this.store.createObjectState(objectAddr);
	}
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
	@Override
	protected void initializeChangeLogState() {
		assert this.changeLogState == null;
		
		this.changeLogState = new MemoryChangeLogState(getAddress(), getRevisionNumber());
	}
	
}
