package org.xydra.core.model.state.impl.gae;

import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.impl.memory.AbstractState;
import org.xydra.server.gae.GaeSchema;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public abstract class AbstractGaeState extends AbstractState implements IHasXID {
	
	private boolean loaded;
	
	private long revisionNumber;
	
	public AbstractGaeState(XAddress address) {
		super(address);
		this.loaded = false;
	}
	
	public void delete() {
		Key key = GaeUtils.toGaeKey(getAddress());
		GaeUtils.deleteEntity(key);
	}
	
	public long getRevisionNumber() {
		loadIfNecessary();
		assert this.revisionNumber >= 0;
		return this.revisionNumber;
	}
	
	protected void loadIfNecessary() {
		if(!this.loaded) {
			Key key = GaeUtils.toGaeKey(getAddress());
			Entity e = GaeUtils.getEntity(key);
			if(e != null) {
				loadFromEntity(e);
			}
			this.loaded = true;
		}
	}
	
	/**
	 * Save revisionNumber, parentAddress
	 */
	protected void storeInEntity(Entity e) {
		
		// revision Number
		e.setUnindexedProperty(GaeSchema.PROP_REVISION_NUMBER, this.revisionNumber);
		// parent
		// TODO is this needed?
		// e.setUnindexedProperty(GaeSchema.PROP_PARENT_ADDRESS,
		// this.getParentAddress().toString());
	}
	
	/**
	 * Load revisionNumber, parentAddress
	 */
	protected void loadFromEntity(Entity e) {
		// revision Number
		Long longObject = (Long)e.getProperty(GaeSchema.PROP_REVISION_NUMBER);
		long revNr;
		if(longObject == null) {
			revNr = 0;
		} else if(longObject == -1) {
			revNr = 0;
		} else {
			revNr = longObject;
		}
		this.revisionNumber = revNr;
		assert this.revisionNumber >= 0;
		// parent
		/*
		 * TODO is this needed? String addressString =
		 * (String)e.getProperty(GaeSchema.PROP_PARENT_ADDRESS);
		 * if(addressString != null) {
		 * setParentAddress(X.getIDProvider().fromAddress(addressString)); }
		 */
	}
	
	public void save() {
		
		Key key = GaeUtils.toGaeKey(getAddress());
		Entity e = new Entity(key);
		storeInEntity(e);
		GaeUtils.putEntity(e);
	}
	
	public void setRevisionNumber(long revisionNumber) {
		loadIfNecessary();
		this.revisionNumber = revisionNumber;
	}
	
	@Override
	public int hashCode() {
		return this.getID().hashCode();
	}
	
	/**
	 * Value is not compared. Objects with same ID, parentAddress and
	 * revisionNumber are considered equal.
	 */
	@Override
	public boolean equals(Object other) {
		return other != null && other instanceof AbstractGaeState
		        && ((AbstractGaeState)other).getAddress().equals(getAddress())

		        && ((AbstractGaeState)other).getRevisionNumber() == this.getRevisionNumber();
	}
	
}
