package org.xydra.core.model.state.impl.gae;

import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.core.model.state.impl.memory.AbstractState;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * An base for X*State implementations that persist to the Google App Engine
 * {@link DatastoreService}.
 */
public abstract class AbstractGaeState extends AbstractState implements IHasXID {
	
	private static final long serialVersionUID = 937578882150219792L;
	
	private boolean loaded;
	
	private long revisionNumber;
	
	public AbstractGaeState(XAddress address) {
		super(address);
		this.loaded = false;
	}
	
	public void delete(XStateTransaction trans) {
		Key key = GaeUtils.keyForEntity(getAddress());
		GaeUtils.deleteEntity(key, GaeStateTransaction.asTransaction(trans));
	}
	
	public long getRevisionNumber() {
		loadIfNecessary();
		assert this.revisionNumber >= 0;
		return this.revisionNumber;
	}
	
	protected void loadIfNecessary() {
		if(!this.loaded) {
			Key key = GaeUtils.keyForEntity(getAddress());
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
	}
	
	public void save(XStateTransaction trans) {
		
		Key key = GaeUtils.keyForEntity(getAddress());
		Entity e = new Entity(key);
		storeInEntity(e);
		GaeUtils.putEntity(e, GaeStateTransaction.asTransaction(trans));
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
