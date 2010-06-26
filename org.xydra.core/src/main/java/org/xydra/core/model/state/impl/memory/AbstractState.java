package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XAddress;


public abstract class AbstractState implements IHasXAddress {
	
	private final XAddress address;
	
	public AbstractState(XAddress address) {
		this.address = address;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
}
