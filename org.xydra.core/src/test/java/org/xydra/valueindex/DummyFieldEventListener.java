package org.xydra.valueindex;

import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;


public class DummyFieldEventListener implements XFieldEventListener {
	XEvent event;

	@Override
	public void onChangeEvent(final XFieldEvent event) {
		this.event = event;
	}

}
