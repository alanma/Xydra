package org.xydra.core.model.state.impl.gae;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateStore;

import com.google.appengine.api.datastore.DatastoreService;


/**
 * An implementation of {@link XStateStore} that persists to the Google App
 * Engine {@link DatastoreService}.
 */
public class GaeStateStore implements XStateStore {
	
	public GaeFieldState createFieldState(XAddress fieldAddr) {
		return new GaeFieldState(fieldAddr);
	}
	
	public GaeModelState createModelState(XAddress modelAddr) {
		return new GaeModelState(modelAddr);
	}
	
	public GaeObjectState createObjectState(XAddress objectAddr) {
		return new GaeObjectState(objectAddr);
	}
	
	public GaeRepositoryState createRepositoryState(XAddress repositoryAddr) {
		return new GaeRepositoryState(repositoryAddr);
	}
	
	public XFieldState loadFieldState(XAddress fieldStateAddress) {
		return GaeFieldState.load(fieldStateAddress);
	}
	
	public XModelState loadModelState(XAddress modelStateAddress) {
		return GaeModelState.load(modelStateAddress);
	}
	
	public XObjectState loadObjectState(XAddress objectStateAddress) {
		return GaeObjectState.load(objectStateAddress);
	}
	
	public XRepositoryState loadRepositoryState(XAddress repositoryStateAddress) {
		return GaeRepositoryState.load(repositoryStateAddress);
	}
	
}
