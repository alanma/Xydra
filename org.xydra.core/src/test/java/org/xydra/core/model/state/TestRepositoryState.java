package org.xydra.core.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;


public class TestRepositoryState extends TestState implements XRepositoryState {
	
	private static final long serialVersionUID = 8197909976435793933L;
	
	public TestRepositoryState(TestStateStore store, XAddress address) {
		super(store, address);
	}
	
	public void addModelState(XModelState modelState) {
		add(modelState.getID());
	}
	
	public XModelState createModelState(XID id) {
		return this.store.createModelState(XX.resolveModel(this.address, id));
	}
	
	@Override
	public XID getID() {
		return getAddress().getRepository();
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
