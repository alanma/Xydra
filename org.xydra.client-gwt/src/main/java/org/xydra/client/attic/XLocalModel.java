package org.xydra.client.attic;

import org.xydra.annotations.ReadOperation;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XID;



public interface XLocalModel extends XBaseModel, XSendsModelEvent, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents {
	
	/**
	 * @param id The XID of the wanted {@link XLocalObject}
	 * @return The {@link XLocalObject} with the given XID or null, if no
	 *         corresponding {@link XLocalObject} exists
	 */
	@ReadOperation
	XLocalObject getObject(XID objectId);
	
}
