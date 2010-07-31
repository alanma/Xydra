package org.xydra.core.test.model.state;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;


public class TestRepositoryState extends TestState implements XRepositoryState {
	
	public TestRepositoryState(TestStateStore store, XAddress address) {
		super(store, address);
	}
	
	@Override
	public XID getID() {
		return getAddress().getRepository();
	}
	
	public void addModelState(XModelState modelState) {
		add(modelState.getID());
	}
	
	public XModelState createModelState(XID id) {
		return this.store.createModelState(XX.resolveModel(this.address, id));
	}
	
	public XModelState getModelState(XID id) {
		return this.store.loadModelState(XX.resolveModel(this.address, id));
	}
	
	public boolean hasModelState(XID id) {
		return has(id);
	}
	
	public void removeModelState(XID modelStateId) {
		remove(modelStateId);
	}
	
}
