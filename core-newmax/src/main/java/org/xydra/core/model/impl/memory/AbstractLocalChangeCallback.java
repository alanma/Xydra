package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XLocalChangeCallback;


public abstract class AbstractLocalChangeCallback implements XLocalChangeCallback {
	
	@Override
    public void onFailure() {
		// ignore
	}
	
	@Override
    public void onSuccess(long revision) {
		// ignore
	}
	
}
