package org.xydra.core.model.state.impl.memory;

import java.util.HashMap;
import java.util.Map;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateStore;


/**
 * A factory and store for State entities that persist only in-memory.
 * 
 * @author voelkel
 */
public class MemoryStateStore implements XStateStore {
	
	private Map<XAddress,XFieldState> fields;
	private Map<XAddress,XObjectState> objects;
	private Map<XAddress,XModelState> models;
	private Map<XAddress,XRepositoryState> repositories;
	
	public MemoryStateStore() {
		this.fields = new HashMap<XAddress,XFieldState>();
		this.objects = new HashMap<XAddress,XObjectState>();
		this.models = new HashMap<XAddress,XModelState>();
		this.repositories = new HashMap<XAddress,XRepositoryState>();
	}
	
	/*
	 * FIXME create....State methods -> what happens if given address is already
	 * taken? Currently, a new state object is created, which will probably
	 * result in overwritting the old state object. Why not return the old and
	 * already existing state object instead, like in the create... Methods from
	 * XObject, XModel and XRepository?
	 */
	public XFieldState createFieldState(XAddress fieldAddr) {
		return new StoredFieldState(fieldAddr, this);
	}
	
	public XModelState createModelState(XAddress modelAddr) {
		return new StoredModelState(modelAddr, this, new MemoryChangeLogState(modelAddr, 0L));
	}
	
	public XObjectState createObjectState(XAddress objectAddr) {
		XChangeLogState log = objectAddr.getModel() != null ? null : new MemoryChangeLogState(
		        objectAddr, 0L);
		return new StoredObjectState(objectAddr, this, log);
	}
	
	public XRepositoryState createRepositoryState(XAddress repoAddr) {
		return new StoredRepositoryState(repoAddr, this);
	}
	
	public XFieldState loadFieldState(XAddress fieldStateAddress) {
		if(fieldStateAddress == null) {
			throw new IllegalArgumentException("fieldStateAddress may not be null");
		}
		return this.fields.get(fieldStateAddress);
	}
	
	public XModelState loadModelState(XAddress modelStateAddress) {
		if(modelStateAddress == null) {
			throw new IllegalArgumentException("modelStateAddress may not be null");
		}
		return this.models.get(modelStateAddress);
	}
	
	public XObjectState loadObjectState(XAddress objectStateAddress) {
		if(objectStateAddress == null) {
			throw new IllegalArgumentException("objectStateAddress may not be null");
		}
		return this.objects.get(objectStateAddress);
	}
	
	public XRepositoryState loadRepositoryState(XAddress repositoryStateAddress) {
		if(repositoryStateAddress == null) {
			throw new IllegalArgumentException("repositoryStateAddress may not be null");
		}
		return this.repositories.get(repositoryStateAddress);
	}
	
	public void deleteFieldState(XAddress fieldAddress) {
		this.fields.remove(fieldAddress);
	}
	
	public void deleteModelState(XAddress modelAddress) {
		this.models.remove(modelAddress);
	}
	
	public void deleteObjectState(XAddress objectAddress) {
		this.objects.remove(objectAddress);
	}
	
	public void deleteRepositoryState(XAddress repositoryAddress) {
		this.repositories.remove(repositoryAddress);
	}
	
	public void save(StoredFieldState fieldState) {
		if(fieldState == null) {
			throw new IllegalArgumentException("fieldState may not be null");
		}
		this.fields.put(fieldState.getAddress(), fieldState);
	}
	
	public void save(AbstractModelState modelState) {
		if(modelState == null) {
			throw new IllegalArgumentException("modelState may not be null");
		}
		this.models.put(modelState.getAddress(), modelState);
	}
	
	public void save(AbstractObjectState objectState) {
		if(objectState == null) {
			throw new IllegalArgumentException("objectState may not be null");
		}
		this.objects.put(objectState.getAddress(), objectState);
	}
	
	public void save(AbstractRepositoryState repositoryState) {
		if(repositoryState == null) {
			throw new IllegalArgumentException("repositoryState may not be null");
		}
		this.repositories.put(X.getIDProvider().fromComponents(repositoryState.getID(), null, null,
		        null), repositoryState);
	}
	
}
