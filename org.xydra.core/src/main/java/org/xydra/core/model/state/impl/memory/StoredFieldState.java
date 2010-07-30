package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.XAddress;


/**
 * Here eager and lazy implementation are the same, as there are no children.
 * 
 * @author voelkel
 * 
 */
public class StoredFieldState extends AbstractFieldState {
	
	private static final long serialVersionUID = -4924597306047670433L;
	
	private MemoryStateStore store;
	
	public StoredFieldState(XAddress fieldAddr, MemoryStateStore store) {
		super(fieldAddr);
		this.store = store;
	}
	
	public void delete(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		this.store.deleteFieldState(getAddress());
	}
	
	public void save(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		this.store.save(this);
	}
	
}
