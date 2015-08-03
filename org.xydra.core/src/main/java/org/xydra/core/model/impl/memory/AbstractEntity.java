package org.xydra.core.model.impl.memory;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XEntity;

/**
 * Abstract super entity type for XModel, XObject and XField types containing
 * central implementations of methods, for example equals().
 *
 * An entity can be <em>stand-alone</em>, i.e. it has no father entity.
 *
 * Some entities can have children.
 *
 * All entities have an {@link XType}, an {@link XAddress}, an {@link XId} and a
 * revision number (see {@link #getRevisionNumber()}).
 *
 * @author kaidel
 */
public abstract class AbstractEntity implements XEntity, IMemoryEntity, Serializable {

	/**
	 * Compares address and revision number. Does not compare potential father
	 * objects. Thus equality works well only within the same entity.
	 *
	 * @see java.lang.Object#equals(java.lang.Object) for full contract.
	 */
	@Override
	public boolean equals(final Object object) {
		if (object instanceof AbstractEntity) {
			final AbstractEntity entity = (AbstractEntity) object;
			if (getAddress().equals(entity.getAddress())
					&& getRevisionNumber() == entity.getRevisionNumber()) {

				return true;
			}
		}
		return false;
	}

	/**
	 * @return the current revision number of this entity. Is 0 if the entity
	 *         has been created. Can be -1 if this instance represents an entity
	 *         that has not been created.
	 */
	@Override
	public abstract long getRevisionNumber();

	/**
	 * Looks into address and revision number. Does not use potential father
	 * objects. Thus equality works well only within the same entity.
	 *
	 * @see java.lang.Object#hashCode() for full contract.
	 */
	@Override
	public int hashCode() {
		final int hashCode = getId().hashCode() + (int) getRevisionNumber();
		return hashCode;
	}

}
