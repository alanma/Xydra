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
	
	private Map<XID,XObjectState> objectStates;
	
	private MemoryChangeLogState changeLogState;
	
	public TemporaryModelState(XAddress modelAddr) {
		super(modelAddr);
		this.objectStates = new HashMap<XID,XObjectState>();
	}
	
	public TemporaryModelState(XAddress modelAddr, long revisionNumber) {
		super(modelAddr, revisionNumber);
		this.objectStates = new HashMap<XID,XObjectState>();
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
	
	public void delete() {
		// nothing to do here
	}
	
	public void save() {
		// nothing to save
	}
	
	public XObjectState createObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return new TemporaryObjectState(objectAddr);
	}
	
	@Override
	void initializeChangeLogState() {
		assert this.changeLogState == null;
		
		this.changeLogState = new MemoryChangeLogState(getAddress(), getRevisionNumber());
	}
	
	public XChangeLogState getChangeLogState() {
		return this.changeLogState;
	}
	
}
