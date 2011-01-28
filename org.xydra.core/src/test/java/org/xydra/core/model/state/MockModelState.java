package org.xydra.core.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;


public class MockModelState extends MockState implements XModelState {
	
	private static final long serialVersionUID = -3093619678678194720L;
	
	XChangeLogState log;
	
	public MockModelState(MockStateStore store, XAddress addr, XChangeLogState log) {
		super(store, addr);
		this.log = log;
	}
	
	public void addObjectState(XObjectState objectState) {
		add(objectState.getID());
	}
	
	public XObjectState createObjectState(XID id) {
		return this.store.createObjectState(XX.resolveObject(this.address, id));
	}
	
	public XChangeLogState getChangeLogState() {
		return this.log;
	}
	
	@Override
	public XID getID() {
		return getAddress().getModel();
	}
	
	public XObjectState getObjectState(XID id) {
		return this.store.loadObjectState(XX.resolveObject(this.address, id));
	}
	
	public boolean hasObjectState(XID id) {
		return has(id);
	}
	
	public void removeObjectState(XID objectStateId) {
		remove(objectStateId);
	}
	
}
