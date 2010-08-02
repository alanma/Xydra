package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;


/**
 * An implementation of {@link XObjectState} that only exists in memory and
 * cannot be stored.
 * 
 * @author dscharrer
 * 
 */
public class TemporaryObjectState extends AbstractObjectState {
	
	private final Map<XID,XFieldState> fieldStates = new HashMap<XID,XFieldState>();
	private final XChangeLogState changeLogState;
	
	public TemporaryObjectState(XAddress objectAddr) {
		super(objectAddr);
		this.changeLogState = null;
	}
	
	public TemporaryObjectState(XAddress objectAddr, XChangeLogState changeLogState) {
		super(objectAddr);
		this.changeLogState = changeLogState;
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
	
	public void delete(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to do here
	}
	
	public void save(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to save
	}
	
	public XFieldState createFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return new TemporaryFieldState(fieldAddr);
	}
	
	public XFieldState getFieldState(XID id) {
		return this.fieldStates.get(id);
	}
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
}
