package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;



/**
 * A logged {@link XModel} that at least supports read operations and can send
 * events.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedModel extends XBaseModel, XSendsModelEvent, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents {
	
	/**
	 * @param id The XID of the wanted {@link XObject}
	 * @return The {@link XObject} with the given XID or null, if no
	 *         corresponding {@link XObject} exists
	 */
	@ReadOperation
	XLoggedObject getObject(XID objectId);
	
	/**
	 * @return the change log for this model
	 */
	XChangeLog getChangeLog();
	
}
