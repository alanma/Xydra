package org.xydra.core.model.state.impl.gae;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;



public class GaeObjectState extends AbstractGaeStateWithChildren implements XObjectState {
	
	private static final long serialVersionUID = 8492473097214011504L;
	
	public GaeObjectState(XAddress objectAddr) {
		super(objectAddr);
		if(MemoryAddress.getAddressedType(objectAddr) != XType.XOBJECT) {
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
		Key key = GaeUtils.toGaeKey(objectStateAddress);
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
	
}
