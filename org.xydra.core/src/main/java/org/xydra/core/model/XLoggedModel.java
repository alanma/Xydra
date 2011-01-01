package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A logged model that at least supports read operations and sends
 * {@link XEvent XEvents} if something is changed.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedModel extends XBaseModel, XSendsModelEvent, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, IHasChangeLog {
	
	/**
	 * Returns the {@link XLoggedObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XLoggedObject} which is to be
	 *            returned
	 * @return The {@link XLoggedObject} with the given {@link XID} or null, if
	 *         no corresponding {@link XLoggedObject} exists
	 */
	@ReadOperation
	XLoggedObject getObject(XID objectId);
	
}
