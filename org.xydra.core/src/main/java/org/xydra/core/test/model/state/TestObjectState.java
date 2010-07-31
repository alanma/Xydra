package org.xydra.core.test.model.state;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;


public class TestObjectState extends TestState implements XObjectState {
	
	XChangeLogState log;
	
	public TestObjectState(TestStateStore store, XAddress addr, XChangeLogState log) {
		super(store, addr);
		
		this.log = log;
	}
	
	public void addFieldState(XFieldState fieldState) {
		add(fieldState.getID());
	}
	
	public XFieldState createFieldState(XID id) {
		return this.store.createFieldState(XX.resolveField(this.address, id));
	}
	
	public XChangeLogState getChangeLogState() {
		return this.log;
	}
	
	public XFieldState getFieldState(XID id) {
		return this.store.loadFieldState(XX.resolveField(this.address, id));
	}
	
	public boolean hasFieldState(XID id) {
		return has(id);
	}
	
	public void removeFieldState(XID fieldStateId) {
		remove(fieldStateId);
	}
	
	@Override
	public XID getID() {
		return getAddress().getObject();
	}
	
}
