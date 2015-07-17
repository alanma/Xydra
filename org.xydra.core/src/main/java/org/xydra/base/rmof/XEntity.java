package org.xydra.base.rmof;

import java.io.Serializable;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXId;
import org.xydra.base.XType;


/**
 * Basic entity for all entities in Xydra.
 *
 * All implementations should have correct {@link #hashCode()} and
 * {@link #equals(Object)} methods.
 *
 * @author kaidel
 *
 */

public interface XEntity extends IHasXAddress, IHasXId, Serializable {

	public XType getType();

}
