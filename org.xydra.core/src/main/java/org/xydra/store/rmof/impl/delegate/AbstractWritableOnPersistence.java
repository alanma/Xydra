package org.xydra.store.rmof.impl.delegate;

import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Super-class that just holds the {@link XydraPersistence} and an
 * executingActorId.
 * 
 * @author xamde
 */
public abstract class AbstractWritableOnPersistence implements IHasXAddress {
	
	@Override
    public XAddress getAddress() {
		return this.address;
	}
	
	public XID getExecutingActorId() {
		return this.executingActorId;
	}
	
	public XydraPersistence getPersistence() {
		return this.persistence;
	}
	
	/**
	 * Set by sub-classes
	 */
	protected transient XAddress address;
	
	protected XID executingActorId;
	
	protected XydraPersistence persistence;
	
	public AbstractWritableOnPersistence(XydraPersistence persistence, XID executingActorId) {
		this.persistence = persistence;
		this.executingActorId = executingActorId;
	}
	
}
