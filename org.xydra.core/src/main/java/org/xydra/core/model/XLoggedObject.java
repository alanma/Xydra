package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;



/**
 * A logged {@link XObject} that at least supports read operations and can send
 * events.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedObject extends XBaseObject, XSendsObjectEvents, XSendsFieldEvents,
        XSendsTransactionEvents {
	
	/**
	 * Returns the field corresponding to the given XID in this object.
	 * 
	 * @param fieldID The XID of the wanted {@link XField}
	 * @return The {@link XField} with the given XID or null, if no
	 *         corresponding {@link XField} exists
	 */
	@ReadOperation
	XLoggedField getField(XID fieldId);
	
}
