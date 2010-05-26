package org.xydra.core.model.state.impl.gae;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;



public class GaeRepositoryState extends AbstractGaeStateWithChildren implements XRepositoryState {
	
	private static final long serialVersionUID = 8492473097214011504L;
	
	public GaeRepositoryState(XAddress repoAddr) {
		super(repoAddr);
		if(MemoryAddress.getAddressedType(repoAddr) != XType.XREPOSITORY) {
			throw new RuntimeException("must be a repository address, was: " + repoAddr);
		}
	}
	
	public void addModelState(XModelState modelState) {
		loadIfNecessary();
		this.children.add(modelState.getID());
	}
	
	public boolean hasModelState(XID id) {
		loadIfNecessary();
		return this.children.contains(id);
	}
	
	public boolean isEmpty() {
		loadIfNecessary();
		return this.children.isEmpty();
	}
	
	public void removeModelState(XID modelId) {
		loadIfNecessary();
		this.children.remove(modelId);
	}
	
	public XID getID() {
		return getAddress().getRepository();
	}
	
	public Iterator<XID> iterator() {
		loadIfNecessary();
		return this.children.iterator();
	}
	
	@Override
	public int hashCode() {
		return this.getID().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && other instanceof XRepositoryState
		        && ((XRepositoryState)other).getID().equals(this.getID());
	}
	
	public XModelState createModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return new GaeModelState(modelAddr);
	}
	
	public XModelState getModelState(XID id) {
		XAddress modelAddr = XX.resolveModel(getAddress(), id);
		return GaeModelState.load(modelAddr);
	}
	
	public static XRepositoryState load(XAddress repositoryStateAddress) {
		Key key = GaeUtils.toGaeKey(repositoryStateAddress);
		Entity entity = GaeUtils.getEntity(key);
		if(entity == null) {
			return null;
		}
		GaeRepositoryState repositoryState = new GaeRepositoryState(repositoryStateAddress);
		repositoryState.loadFromEntity(entity);
		return repositoryState;
	}
	
}
