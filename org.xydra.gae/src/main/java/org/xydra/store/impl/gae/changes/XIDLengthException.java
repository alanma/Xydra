package org.xydra.store.impl.gae.changes;

import org.xydra.base.XID;
import org.xydra.store.RequestException;


public class XIDLengthException extends RequestException {
	
	private static final long serialVersionUID = -6686217386195204050L;
	
	public XIDLengthException(XID id) {
		super("Too long XID for the GAE backend: " + id);
	}
	
}
