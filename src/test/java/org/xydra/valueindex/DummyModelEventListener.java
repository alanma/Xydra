package org.xydra.valueindex;

import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;


public class DummyModelEventListener implements XModelEventListener {
	XEvent event;
	
	@Override
	public void onChangeEvent(XModelEvent event) {
		this.event = event;
	}
	
}
