package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateStore;


/**
 * An {@link XStateStore} implementation that creates
 * {@link TemporaryRepositoryState}s, {@link TemporaryModelState}s,
 * {@link TemporaryObjectState}s and {@link TemporaryFieldState}s which will not
 * be persisted. Loading states is not supported.
 * 
 * @author dscharrer
 * 
 */
public class TemporaryStateStore implements XStateStore {
	
	public XFieldState createFieldState(XAddress fieldStateAddress) {
		return new TemporaryFieldState(fieldStateAddress);
	}
	
	public XModelState createModelState(XAddress modelStateAddress) {
		return new TemporaryModelState(modelStateAddress, new MemoryChangeLogState(
		        modelStateAddress));
	}
	
	public XObjectState createObjectState(XAddress objectStateAddress) {
		XChangeLogState log = objectStateAddress.getModel() != null ? null
		        : new MemoryChangeLogState(objectStateAddress);
		return new TemporaryObjectState(objectStateAddress, log);
	}
	
	public XRepositoryState createRepositoryState(XAddress repositoryStateAddress) {
		return new TemporaryRepositoryState(repositoryStateAddress);
	}
	
	public XFieldState loadFieldState(XAddress fieldStateAddress) {
		throw new UnsupportedOperationException();
	}
	
	public XModelState loadModelState(XAddress modelStateAddress) {
		throw new UnsupportedOperationException();
	}
	
	public XObjectState loadObjectState(XAddress objectStateAddress) {
		throw new UnsupportedOperationException();
	}
	
	public XRepositoryState loadRepositoryState(XAddress repositoryStateAddress) {
		throw new UnsupportedOperationException();
	}
	
}
