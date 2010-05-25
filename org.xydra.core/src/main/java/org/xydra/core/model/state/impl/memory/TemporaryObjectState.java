package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;



/**
 * An implementation of {@link XObjectState} that exists only in memory.
 * 
 * @author dscharrer
 * 
 */
public class TemporaryObjectState extends AbstractObjectState {
	
	private Map<XID,XFieldState> fieldStates;
	
	public TemporaryObjectState(XAddress objectAddr) {
		super(objectAddr);
		this.fieldStates = new HashMap<XID,XFieldState>();
	}
	
	public void addFieldState(XFieldState fieldState) {
		checkFieldState(fieldState);
		this.fieldStates.put(fieldState.getID(), fieldState);
	}
	
	public boolean hasFieldState(XID fieldStateID) {
		return this.fieldStates.containsKey(fieldStateID);
	}
	
	public boolean isEmpty() {
		return this.fieldStates.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.fieldStates.keySet().iterator();
	}
	
	public void removeFieldState(XID fieldId) {
		this.fieldStates.remove(fieldId);
	}
	
	public void delete() {
		// nothing to do here
	}
	
	public void save() {
		// nothing to save
	}
	
	public XFieldState createFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return new TemporaryFieldState(fieldAddr);
	}
	
	public XFieldState getFieldState(XID id) {
		return this.fieldStates.get(id);
	}
	
}
