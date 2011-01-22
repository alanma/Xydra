package org.xydra.base.rmof.impl.delegate;

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
	
	protected transient XAddress address;
	
	protected XID executingActorId;
	
	protected XydraPersistence persistence;
	
	public AbstractWritableOnPersistence(XydraPersistence persistence, XID executingActorId) {
		this.persistence = persistence;
		this.executingActorId = executingActorId;
	}
	
}
