package org.xydra.core.model;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;


/**
 * An {@link XLogState} represents the inner state of an {@link XLog} for
 * example for persistence/serialisation purposes.
 * 
 * @author Andi K.
 */
public interface XLogState extends Serializable {
	
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
	
	/**
	 * Removes all {@link XEvent XEvents} from this XChangeLogState that
	 * occurred after the given revision number.
	 * 
	 * @param revisionNumber the revision number from which on the entries are
	 *            to be removed
	 * @return true, if truncating was successful, false otherwise (may happen
	 *         if the given revision number was bigger than the current revision
	 *         number of this change log state or smaller than the revision
	 *         number when this change log began logging)
	 */
	
	boolean truncateToRevision(long revisionNumber);
	
}
