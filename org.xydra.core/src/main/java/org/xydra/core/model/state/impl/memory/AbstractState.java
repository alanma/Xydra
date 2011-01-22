package org.xydra.core.model.state.impl.memory;

import java.io.Serializable;

import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.core.model.state.XStateTransaction;


/**
 * An abstract implementation of the methods which implementations are the same
 * for alle state types.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractState implements IHasXAddress, Serializable {
	
	private static final long serialVersionUID = -3431932927218751276L;
	
	private final XAddress address;
	
	public AbstractState(XAddress address) {
		this.address = address;
	}
	
	/*
	 * See {@link XModelState#beginTransaction()}, etc.
	 */
	public XStateTransaction beginTransaction() {
		// overwrite, if transactions are needed for the state backend
		return null;
	}
	
	/*
	 * See {@link XModelState#endTransaction()}, etc.
	 */
	public void endTransaction(XStateTransaction transaction) {
		// overwrite, if transactions are needed for the state backend
		assert transaction == null;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
}
