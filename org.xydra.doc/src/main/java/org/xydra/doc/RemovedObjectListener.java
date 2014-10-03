package org.xydra.doc;

import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;

public class RemovedObjectListener implements XModelEventListener {

	private int counter; // variable that will count how many XObjects are
							// removed from the XModel

	@Override
	public void onChangeEvent(XModelEvent event) {
		if (event.getChangeType() == ChangeType.REMOVE) {
			this.counter++; // only increment the counter if an XObject is
							// removed!
		}
	}

	// we'll need a method for getting the value of our counter variable
	public int getRemovedObjectsCounter() {
		return this.counter;
	}
}
