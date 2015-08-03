package org.xydra.store.impl.gae.changes;

import org.xydra.base.XId;
import org.xydra.store.RequestException;

public class XIdLengthException extends RequestException {

	private static final long serialVersionUID = -6686217386195204050L;

	public XIdLengthException(final XId id) {
		super("Too long XId for the GAE backend: " + id);
	}

}
