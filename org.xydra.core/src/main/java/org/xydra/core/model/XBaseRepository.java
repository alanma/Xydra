package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;


/**
 * A basic {@link XModel} that at least supports read operations.
 * 
 * @author dscharrer
 * 
 */
public interface XBaseRepository extends IHasXAddress, IHasXID, Iterable<XID> {
	
	/**
	 * @param id
	 * @return the XModel with the given id or null if no such model exists in
	 *         this repository.
	 */
	@ReadOperation
	XBaseModel getModel(XID id);
	
	/**
	 * Checks whether this XRepository contains an XModel with the given XID.
	 * 
	 * @param id The XID which is to be checked
	 * @return true, if this XRepository contains an XModel with the given XID,
	 *         false otherwise
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
	
}
