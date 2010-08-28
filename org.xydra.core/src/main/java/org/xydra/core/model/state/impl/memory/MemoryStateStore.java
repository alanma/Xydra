package org.xydra.core.model.state.impl.memory;

import java.io.Serializable;
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
 * An {@link XStateStore} implementation that creates states that are stored
 * only in-memory. As opposed to with {@link TemporaryStateStore}, saved states
 * can be loaded from the store at a later time.
 * 
 * @author voelkel
 */
public class MemoryStateStore implements XStateStore, Serializable {
	
	private static final long serialVersionUID = 4486561293132414895L;
	
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
	
	public XFieldState createFieldState(XAddress fieldAddr) {
		return new StoredFieldState(fieldAddr, this);
	}
	
	public XModelState createModelState(XAddress modelAddr) {
		return new StoredModelState(modelAddr, this, new MemoryChangeLogState(modelAddr));
	}
	
	public XObjectState createObjectState(XAddress objectAddr) {
		XChangeLogState log = objectAddr.getModel() != null ? null : new MemoryChangeLogState(
		        objectAddr);
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
	
	protected void deleteFieldState(XAddress fieldAddress) {
		this.fields.remove(fieldAddress);
	}
	
	protected void deleteModelState(XAddress modelAddress) {
		this.models.remove(modelAddress);
	}
	
	protected void deleteObjectState(XAddress objectAddress) {
		this.objects.remove(objectAddress);
	}
	
	protected void deleteRepositoryState(XAddress repositoryAddress) {
		this.repositories.remove(repositoryAddress);
	}
	
	protected void save(StoredFieldState fieldState) {
		if(fieldState == null) {
			throw new IllegalArgumentException("fieldState may not be null");
		}
		this.fields.put(fieldState.getAddress(), fieldState);
	}
	
	protected void save(AbstractModelState modelState) {
		if(modelState == null) {
			throw new IllegalArgumentException("modelState may not be null");
		}
		this.models.put(modelState.getAddress(), modelState);
	}
	
	protected void save(AbstractObjectState objectState) {
		if(objectState == null) {
			throw new IllegalArgumentException("objectState may not be null");
		}
		this.objects.put(objectState.getAddress(), objectState);
	}
	
	protected void save(AbstractRepositoryState repositoryState) {
		if(repositoryState == null) {
			throw new IllegalArgumentException("repositoryState may not be null");
		}
		this.repositories.put(X.getIDProvider().fromComponents(repositoryState.getID(), null, null,
		        null), repositoryState);
	}
	
}
