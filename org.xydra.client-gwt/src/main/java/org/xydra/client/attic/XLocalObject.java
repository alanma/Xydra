package org.xydra.client.attic;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;



public interface XLocalObject extends XBaseObject, XSendsObjectEvents, XSendsFieldEvents,
        XSendsTransactionEvents {
	
	/**
	 * Returns the field corresponding to the given XID in this object.
	 * 
	 * @param fieldID The XID of the wanted {@link XLocalField}
	 * @return The {@link XLocalField} with the given XID or null, if no
	 *         corresponding {@link XLocalField} exists
	 */
	@ReadOperation
	XLocalField getField(XID fieldId);
	
}
