package org.xydra.base.change;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;


public interface XSessionModel extends XWritableModel {

	/**
	 * Loads the object from underlying persistence or uses the cached data if
	 * it has been read in the current session already.
	 *
	 * @param objectId
	 * @return this instance for fluent API style
	 */
	XSessionModel loadObject(XId objectId);

	/**
	 * If not all objects are already known (i.e. from a previous call to this
	 * method), the underlying model is fetched and stored.
	 *
	 * @return this instance for fluent API style
	 */
	XSessionModel loadAllObjects();

}
