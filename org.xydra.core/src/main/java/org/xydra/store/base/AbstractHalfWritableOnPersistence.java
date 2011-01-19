package org.xydra.store.base;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.store.impl.delegate.XydraPersistence;


public abstract class AbstractHalfWritableOnPersistence {
	
	protected transient XAddress address;
	
	protected XID executingActorId;
	
	protected XydraPersistence persistence;
	
	public AbstractHalfWritableOnPersistence(XydraPersistence persistence, XID executingActorId) {
		this.persistence = persistence;
		this.executingActorId = executingActorId;
	}
}
