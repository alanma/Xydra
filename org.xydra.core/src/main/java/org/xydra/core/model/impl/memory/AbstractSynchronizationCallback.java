package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XSynchronizationCallback;


public abstract class AbstractSynchronizationCallback implements XSynchronizationCallback {
	
	public void applied(long revision) {
		// ignore
	}
	
	public void failed() {
		// ignore
	}
	
}
