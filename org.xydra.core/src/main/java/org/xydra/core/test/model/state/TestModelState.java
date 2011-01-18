package org.xydra.core.test.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.XX;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;


public class TestModelState extends TestState implements XModelState {
	
	private static final long serialVersionUID = -3093619678678194720L;
	
	XChangeLogState log;
	
	public TestModelState(TestStateStore store, XAddress addr, XChangeLogState log) {
		super(store, addr);
		this.log = log;
	}
	
	@Override
	public XID getID() {
		return getAddress().getModel();
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
