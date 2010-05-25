package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XFieldState;


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
	
	public void delete() {
		// nothing to do here
	}
	
	public void save() {
		// nothing to save
	}
	
}
