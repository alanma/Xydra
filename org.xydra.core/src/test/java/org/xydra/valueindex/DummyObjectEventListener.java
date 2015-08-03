package org.xydra.valueindex;

import org.xydra.base.change.XEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;


public class DummyObjectEventListener implements XObjectEventListener {
	XEvent event;

	@Override
	public void onChangeEvent(final XObjectEvent event) {
		this.event = event;
	}

}
