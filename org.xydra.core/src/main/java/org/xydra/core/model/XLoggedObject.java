package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A logged {@link XObject} that at least supports read operations and sends
 * {@link XEvent XEvents} if something is changed.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedObject extends XBaseObject, XSendsObjectEvents, XSendsFieldEvents,
        XSendsTransactionEvents {
	
	/**
	 * Returns the {@link XLoggedField} contained in this object with the given
	 * {@link XID}.
	 * 
	 * @param fieldID The {@link XID} of the {@link XLoggedField} which is to be
	 *            returned
	 * @return The {@link XLoggedField} with the given {@link XID} or null, if
	 *         no corresponding {@link XBaseField} exists
	 */
	@ReadOperation
	XLoggedField getField(XID fieldId);
	
}
