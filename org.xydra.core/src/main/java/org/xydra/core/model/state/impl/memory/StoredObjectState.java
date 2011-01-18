package org.xydra.core.model.state.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.XX;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XStateTransaction;


/**
 * A lazy implementation of {@link XObjectState}. Only the {@link XID XIDs} of
 * the child-{@link XFieldState XFieldStates} are locally stored during runtime.
 * Child- {@link XFieldState XFieldStates} are retrieved on demand and only
 * loaded if they need to be loaded.
 * 
 * @author voelkel
 * 
 */
public class StoredObjectState extends AbstractObjectState {
	
	private static final long serialVersionUID = 144975610485120472L;
	
	private final Set<XID> fieldStateIDs = new HashSet<XID>();
	private final MemoryStateStore store;
	private final XChangeLogState changeLogState;
	
	public StoredObjectState(XAddress objectAddr, MemoryStateStore store) {
		super(objectAddr);
		this.store = store;
		this.changeLogState = null;
	}
	
	public StoredObjectState(XAddress objectAddr, MemoryStateStore store,
	        XChangeLogState changeLogState) {
		super(objectAddr);
		this.store = store;
		this.changeLogState = changeLogState;
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
	
	public void delete(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		this.store.deleteObjectState(getAddress());
	}
	
	public void save(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
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
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
}
