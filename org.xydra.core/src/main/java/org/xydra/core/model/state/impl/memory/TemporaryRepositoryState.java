package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XModelState;


public class TemporaryRepositoryState extends AbstractRepositoryState {
	
	private final Map<XID,XModelState> modelStates = new HashMap<XID,XModelState>();
	
	public TemporaryRepositoryState(XAddress repoAddr) {
		super(repoAddr);
	}
	
	public void addModelState(XModelState modelState) {
		checkModelState(modelState);
		this.modelStates.put(modelState.getID(), modelState);
	}
	
	public boolean hasModelState(XID modelStateID) {
		return this.modelStates.containsKey(modelStateID);
	}
	
	public boolean isEmpty() {
		return this.modelStates.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		return this.modelStates.keySet().iterator();
	}
	
	public void removeModelState(XID modelId) {
		this.modelStates.remove(modelId);
	}
	
	public void save(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to save
	}
	
	public void delete(Object transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to do here
	}
	
	public XModelState createModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return new TemporaryModelState(modelAddr, new MemoryChangeLogState(modelAddr, 0L));
	}
	
	public XModelState getModelState(XID id) {
		return this.modelStates.get(id);
	}
}
