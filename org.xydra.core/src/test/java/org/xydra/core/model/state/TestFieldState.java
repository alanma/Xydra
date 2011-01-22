package org.xydra.core.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.model.state.XFieldState;


public class TestFieldState extends TestState implements XFieldState {
	
	private static final long serialVersionUID = 5586237583268217971L;
	
	public TestFieldState(TestStateStore store, XAddress addr) {
		super(store, addr);
	}
	
	@Override
	public XID getID() {
		return getAddress().getField();
	}
	
}
