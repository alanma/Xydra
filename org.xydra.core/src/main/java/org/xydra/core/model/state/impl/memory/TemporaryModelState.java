package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XObjectState;


public class TemporaryModelState extends AbstractModelState {
	
	private static final long serialVersionUID = 5621101945176337135L;
	
	private final Map<XID,XObjectState> objectStates = new HashMap<XID,XObjectState>();
	private final XChangeLogState changeLogState;
	
	public TemporaryModelState(XAddress modelAddr) {
		super(modelAddr);
		this.changeLogState = null;
	}
	
	public TemporaryModelState(XAddress modelAddr, XChangeLogState changeLogState) {
		super(modelAddr);
		this.changeLogState = changeLogState;
	}
	
	public void addObjectState(XObjectState objectState) {
		checkObjectState(objectState);
		this.objectStates.put(objectState.getID(), objectState);
	}
	
	public XObjectState getObjectState(XID objectStateID) {
		return this.objectStates.get(objectStateID);
	}
	
	public boolean hasObjectState(XID objectStateID) {
		return this.objectStates.containsKey(objectStateID);
	}
	
	public boolean isEmpty() {
		return this.objectStates.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.objectStates.keySet().iterator();
	}
	
	public void removeObjectState(XID objectId) {
		this.objectStates.remove(objectId);
	}
	
	public void delete(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to do here
	}
	
	public void save(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to save
	}
	
	public XObjectState createObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return new TemporaryObjectState(objectAddr);
	}
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
}
