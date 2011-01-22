package org.xydra.core.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;


public class TestObjectState extends TestState implements XObjectState {
	
	private static final long serialVersionUID = -6505722468701048503L;
	
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
	
	@Override
	public XID getID() {
		return getAddress().getObject();
	}
	
	public boolean hasFieldState(XID id) {
		return has(id);
	}
	
	public void removeFieldState(XID fieldStateId) {
		remove(fieldStateId);
	}
	
}
