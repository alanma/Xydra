package org.xydra.core.model.state.impl.gae;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.server.impl.gae.OldGaeUtils;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * An implementation of {@link XModelState} that persists to the Google App
 * Engine {@link DatastoreService}.
 */
public class GaeModelState extends AbstractGaeStateWithChildren implements XModelState {
	
	private static final long serialVersionUID = 8492473097214011504L;
	
	public GaeModelState(XAddress modelAddr) {
		super(modelAddr);
		if(modelAddr.getAddressedType() != XType.XMODEL) {
			throw new RuntimeException("must be a model address, was: " + modelAddr);
		}
	}
	
	public void addObjectState(XObjectState objectState) {
		loadIfNecessary();
		this.children.add(objectState.getID());
	}
	
	public boolean hasObjectState(XID id) {
		loadIfNecessary();
		return this.children.contains(id);
	}
	
	public boolean isEmpty() {
		loadIfNecessary();
		return this.children.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		loadIfNecessary();
		return this.children.iterator();
	}
	
	public void removeObjectState(XID objectId) {
		loadIfNecessary();
		this.children.remove(objectId);
	}
	
	public XObjectState createObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return new GaeObjectState(objectAddr);
	}
	
	public XObjectState getObjectState(XID id) {
		XAddress objectAddr = XX.resolveObject(getAddress(), id);
		return GaeObjectState.load(objectAddr);
	}
	
	public static XModelState load(XAddress modelStateAddress) {
		Key key = OldGaeUtils.keyForEntity(modelStateAddress);
		Entity entity = GaeUtils.getEntity(key);
		if(entity == null) {
			return null;
		}
		GaeModelState modelState = new GaeModelState(modelStateAddress);
		modelState.loadFromEntity(entity);
		return modelState;
	}
	
	public XID getID() {
		return getAddress().getModel();
	}
	
	XChangeLogState log = null;
	
	public XChangeLogState getChangeLogState() {
		if(this.log == null) {
			this.log = new GaeChangeLogState(getAddress(), getRevisionNumber());
		}
		return this.log;
	}
	
}
