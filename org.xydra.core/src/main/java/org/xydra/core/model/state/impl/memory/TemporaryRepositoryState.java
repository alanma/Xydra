package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateTransaction;


/**
 * An implementation of {@link XRepositoryState} that only exists in memory and
 * cannot be stored.
 * 
 */
public class TemporaryRepositoryState extends AbstractRepositoryState {
	
	private static final long serialVersionUID = 3209744380682066522L;
	
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
	
	public void save(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to save
	}
	
	public void delete(XStateTransaction transaction) {
		assert transaction == null : "no transactions needed/supported";
		// nothing to do here
	}
	
	public XModelState createModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return new TemporaryModelState(modelAddr, new MemoryChangeLogState(modelAddr));
	}
	
	public XModelState getModelState(XID id) {
		return this.modelStates.get(id);
	}
}
