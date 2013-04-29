package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.base.XAddress;


/**
 * Parent Class for SyncLogs
 * 
 * @author Andi K.
 */
public interface XLog extends Serializable {
	
	/**
	 * @return the {@link XAddress} of the {@link XModel} or {@link XObject}
	 *         this change log refers to. All contained events have been
	 *         produced by this entity or a descendant.
	 */
	XAddress getBaseAddress();
	
	/**
	 * @return the current revision number of the logged {@link XModel} as seen
	 *         from this log
	 */
	long getCurrentRevisionNumber();
	
}
