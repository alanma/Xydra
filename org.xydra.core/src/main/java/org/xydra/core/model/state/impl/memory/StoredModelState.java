package org.xydra.core.model.state.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.XX;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XStateTransaction;


/**
 * A lazy implementation of {@link XModelState}. Only the {@link XID XIDs} of
 * the child-{@link XObjectState XObjectStates} are locally stored during
 * runtime. Child-{@link XObjectState XObjectStates} are retrieved on demand and
 * only loaded if they need to be loaded.
 * 
 */
public class StoredModelState extends AbstractModelState {
	
	private static final long serialVersionUID = -509532259783788492L;
	
	private final Set<XID> objectStateIDs = new HashSet<XID>();
	private final MemoryStateStore store;
	private final XChangeLogState changeLogState;
	
	/**
	 * @param modelStateID
	 * @param memoryStateStore where persistence happens on
	 *            {@link StoredModelState#save()} and
	 *            {@link StoredModelState#delete()}.
	 */
	public StoredModelState(XAddress modelAddr, MemoryStateStore store) {
		super(modelAddr);
		this.store = store;
		this.changeLogState = null;
	}
	
	public StoredModelState(XAddress modelAddr, MemoryStateStore store,
	        XChangeLogState changeLogState) {
		super(modelAddr);
		this.store = store;
		this.changeLogState = changeLogState;
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
	
	public void delete(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		this.store.deleteModelState(getAddress());
	}
	
	public void save(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		this.store.save(this);
	}
	
	public XObjectState createObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return this.store.createObjectState(objectAddr);
	}
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
}
