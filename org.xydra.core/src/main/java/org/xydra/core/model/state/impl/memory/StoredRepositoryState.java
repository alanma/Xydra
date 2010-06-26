package org.xydra.core.model.state.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XModelState;


public class StoredRepositoryState extends AbstractRepositoryState {
	
	private static final long serialVersionUID = 4103085757625902603L;
	
	private final Set<XID> modelStateIDs = new HashSet<XID>();
	private final MemoryStateStore store;
	
	public StoredRepositoryState(XAddress modelAddr, MemoryStateStore store) {
		super(modelAddr);
		this.store = store;
	}
	
	public void addModelState(XModelState modelState) {
		checkModelState(modelState);
		this.modelStateIDs.add(modelState.getID());
	}
	
	/* can be answered by using solely local data */
	public boolean hasModelState(XID modelStateID) {
		boolean result = this.modelStateIDs.contains(modelStateID);
		return result;
	}
	
	/* can be answered by using solely local data */
	public boolean isEmpty() {
		return this.modelStateIDs.isEmpty();
	}
	
	/* can be answered by using solely local data */
	public Iterator<XID> iterator() {
		return this.modelStateIDs.iterator();
	}
	
	public void removeModelState(XID modelId) {
		this.modelStateIDs.remove(modelId);
	}
	
	protected void setChildrenIDs(Iterator<XID> childrenIDs) {
		synchronized(this.modelStateIDs) {
			this.modelStateIDs.clear();
			while(childrenIDs.hasNext()) {
				XID xid = childrenIDs.next();
				this.modelStateIDs.add(xid);
			}
		}
	}
	
	public void save() {
		this.store.save(this);
	}
	
	public void delete() {
		this.store.deleteRepositoryState(this.getAddress());
	}
	
	public XModelState createModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return this.store.createModelState(modelAddr);
	}
	
	public XModelState getModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return this.store.loadModelState(modelAddr);
	}
}
