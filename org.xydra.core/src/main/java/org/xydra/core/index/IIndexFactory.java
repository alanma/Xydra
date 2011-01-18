package org.xydra.core.index;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XID;
import org.xydra.core.model.XObject;


@RunsInAppEngine
@RunsInGWT
@RunsInJava
public interface IIndexFactory {
	
	/**
	 * @param fieldID the fieldID to index XObjects by.
	 * @param indexObject common practice is to use one with ID
	 *            {objectID}"-index-"{fieldID}
	 * @return the given indexObject wrapped as an {@link IObjectIndex}. Index
	 *         entries are stored as XFields.
	 */
	IObjectIndex createObjectIndex(XID fieldID, XObject indexObject);
	
	/**
	 * @param fieldID the fieldID to index XObjects by.
	 * @param indexObject common practice is to use one with ID
	 *            {objectID}"-index-"{fieldID}
	 * @return the given indexObject wrapped as an {@link IObjectIndex}. Index
	 *         entries are stored as XFields.
	 */
	IUniqueObjectIndex createUniqueObjectIndex(XID fieldID, XObject indexObject);
}
