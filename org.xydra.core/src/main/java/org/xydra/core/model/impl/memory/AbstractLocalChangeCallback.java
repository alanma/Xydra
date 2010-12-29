package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XLocalChangeCallback;


public abstract class AbstractLocalChangeCallback implements XLocalChangeCallback {
	
	public void applied(long revision) {
		// ignore
	}
	
	public void failed() {
		// ignore
	}
	
}
