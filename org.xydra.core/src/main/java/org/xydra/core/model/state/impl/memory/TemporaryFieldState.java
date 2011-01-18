package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XStateTransaction;


/**
 * An implementation of {@link XFieldState} that only exists in memory and
 * cannot be stored.
 * 
 * @author dscharrer
 * 
 */
public class TemporaryFieldState extends AbstractFieldState {
	
	private static final long serialVersionUID = 2383163381618680601L;
	
	public TemporaryFieldState(XAddress fieldAddr) {
		super(fieldAddr);
	}
	
	public void delete(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to do here
	}
	
	public void save(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to save
	}
	
}
