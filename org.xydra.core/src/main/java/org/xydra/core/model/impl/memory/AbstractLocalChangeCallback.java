package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XLocalChangeCallback;


public abstract class AbstractLocalChangeCallback implements XLocalChangeCallback {
	
	public void onFailure() {
		// ignore
	}
	
	public void onSuccess(long revision) {
		// ignore
	}
	
}
