package org.xydra.client.gwt.client.editor;

import org.xydra.core.model.XID;


/**
 * Callback for when the editor requests the wrapped entity to be deleted.
 */
public interface DeleteCallback {
	
	void delete(XID entity);
	
}
