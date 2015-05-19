package org.xydra.base.rmof.impl.memory;

import java.io.Serializable;

import org.xydra.base.rmof.XEntity;


public abstract class SimpleEntity implements Serializable, XEntity {

	private static final long serialVersionUID = -8935900909094851790L;

	/* Just for GWT */
	protected SimpleEntity() {
	}

	@Override
	public int hashCode() {
		return (int) (this.getAddress().hashCode() + this.getRevisionNumber());
	}

	public abstract long getRevisionNumber();

	private boolean entityExists = true;

	/**
	 * @return at runtime if the entity currently exists or not. This flag is
	 *         not persisted.
	 */
	public boolean exists() {
		return this.entityExists;
	}

	/**
	 * Set if this entity exists or not. This flag is not persisted.
	 * 
	 * @param b
	 */
	public void setExists(boolean b) {
		this.entityExists = b;
	}

}
