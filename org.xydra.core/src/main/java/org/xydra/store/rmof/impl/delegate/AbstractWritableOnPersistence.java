package org.xydra.store.rmof.impl.delegate;

import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.persistence.XydraPersistence;


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

	public XId getExecutingActorId() {
		return this.executingActorId;
	}

	public XydraPersistence getPersistence() {
		return this.persistence;
	}

	/**
	 * Set by sub-classes
	 */
	protected transient XAddress address;

	protected XId executingActorId;

	protected XydraPersistence persistence;

	public AbstractWritableOnPersistence(final XydraPersistence persistence, final XId executingActorId) {
		this.persistence = persistence;
		this.executingActorId = executingActorId;
	}

}
