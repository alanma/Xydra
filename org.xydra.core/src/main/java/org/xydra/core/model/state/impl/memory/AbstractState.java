package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;


public abstract class AbstractState implements IHasXID {
	
	private final XAddress address;
	
	public AbstractState(XAddress address) {
		this.address = address;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
}
