package org.xydra.core.model.state;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


public class MockFieldState extends MockState implements XFieldState {
	
	private static final long serialVersionUID = 5586237583268217971L;
	
	public MockFieldState(MockStateStore store, XAddress addr) {
		super(store, addr);
	}
	
	@Override
	public XID getID() {
		return getAddress().getField();
	}
	
}
