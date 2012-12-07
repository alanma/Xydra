package org.xydra.core.index;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableObject;


@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public interface IIndexFactory {
	
	/**
	 * @param fieldId the fieldId to index XObjects by.
	 * @param indexObject common practice is to use one with ID
	 *            {objectId}"-index-"{fieldId}
	 * @return the given indexObject wrapped as an {@link IObjectIndex}. Index
	 *         entries are stored as XFields.
	 */
	IObjectIndex createObjectIndex(XID fieldId, XWritableObject indexObject);
	
	/**
	 * @param fieldId the fieldId to index XObjects by.
	 * @param indexObject common practice is to use one with ID
	 *            {objectId}"-index-"{fieldId}
	 * @return the given indexObject wrapped as an {@link IObjectIndex}. Index
	 *         entries are stored as XFields.
	 */
	IUniqueObjectIndex createUniqueObjectIndex(XID fieldId, XWritableObject indexObject);
}
