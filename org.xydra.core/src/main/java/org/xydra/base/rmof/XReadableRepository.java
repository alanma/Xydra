package org.xydra.base.rmof;

import java.util.Iterator;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;


/**
 * A basic repository that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XReadableRepository extends XEntity, Iterable<XID> {
	
	/**
	 * Returns the {@link XReadableModel} contained in this repository with the
	 * given {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XReadableModel} which is to be
	 *            returned
	 * @return the {@link XReadableModel} with the given {@link XID} or null if
	 *         no such {@link XReadableModel} exists in this repository.
	 */
	@ReadOperation
	XReadableModel getModel(XID id);
	
	/**
	 * Checks whether this repository contains an {@link XReadableModel} with
	 * the given {@link XID}.
	 * 
	 * @param id The {@link XID} which is to be checked
	 * @return true, if this repository contains an {@link XReadableModel} with
	 *         the given {@link XID}, false otherwise
	 */
	@ReadOperation
	boolean hasModel(XID id);
	
	/**
	 * Returns true, if this repository has no child-models
	 * 
	 * @return true, if this repository has no child-models
	 */
	@ReadOperation
	boolean isEmpty();
	
	/**
	 * @return an iterator over the {@link XID XIDs} of the child-models of this
	 *         XBaseRepository.
	 */
	@ReadOperation
	Iterator<XID> iterator();
	
}
