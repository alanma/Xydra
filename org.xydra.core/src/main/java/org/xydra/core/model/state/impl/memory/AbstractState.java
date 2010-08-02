package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XAddress;


/**
 * An abstract implementation of the methods which implementations are the same
 * for alle state types.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractState implements IHasXAddress {
	
	private final XAddress address;
	
	public AbstractState(XAddress address) {
		this.address = address;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	/*
	 * TODO What is the purpose of the beginTransaction() & endTransaction()
	 * method? Why should they be overwritten?
	 */
	public Object beginTransaction() {
		// overwrite, if transactions are needed for the state backend
		return null;
	}
	
	public void endTransaction(Object transaction) {
		// overwrite, if transactions are needed for the state backend
		assert transaction == null;
	}
	
}
