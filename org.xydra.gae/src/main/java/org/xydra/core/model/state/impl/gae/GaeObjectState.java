package org.xydra.core.model.state.impl.gae;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.server.impl.gae.OldGaeUtils;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * An implementation of {@link XObjectState} that persists to the Google App
 * Engine {@link DatastoreService}.
 */
public class GaeObjectState extends AbstractGaeStateWithChildren implements XObjectState {
	
	private static final long serialVersionUID = 8492473097214011504L;
	
	public GaeObjectState(XAddress objectAddr) {
		super(objectAddr);
		if(objectAddr.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("must be an object address, was: " + objectAddr);
		}
	}
	
	public void addFieldState(XFieldState fieldState) {
		loadIfNecessary();
		this.children.add(fieldState.getID());
	}
	
	public boolean hasFieldState(XID id) {
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
	
	public void removeFieldState(XID fieldId) {
		loadIfNecessary();
		this.children.remove(fieldId);
	}
	
	public XFieldState createFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return new GaeFieldState(fieldAddr);
	}
	
	public XFieldState getFieldState(XID id) {
		XAddress fieldAddr = XX.resolveField(getAddress(), id);
		return GaeFieldState.load(fieldAddr);
	}
	
	public static XObjectState load(XAddress objectStateAddress) {
		Key key = OldGaeUtils.keyForEntity(objectStateAddress);
		Entity entity = GaeUtils.getEntity(key);
		if(entity == null) {
			return null;
		}
		GaeObjectState objectState = new GaeObjectState(objectStateAddress);
		objectState.loadFromEntity(entity);
		return objectState;
	}
	
	public XID getID() {
		return getAddress().getObject();
	}
	
	XChangeLogState log = null;
	
	public XChangeLogState getChangeLogState() {
		if(getAddress().getModel() != null) {
			return null;
		}
		if(this.log == null) {
			this.log = new GaeChangeLogState(getAddress(), getRevisionNumber());
		}
		return this.log;
	}
	
}
