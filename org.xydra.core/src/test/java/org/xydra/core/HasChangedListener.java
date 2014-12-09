/**
 * 
 */
package org.xydra.core;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;

/**
 * Test utility: A listener for events of type T that records in a boolean
 * variable if any events have been received.
 * 
 * @author dscharrer
 */
public class HasChangedListener implements XRepositoryEventListener, XModelEventListener,
		XObjectEventListener, XFieldEventListener, XTransactionEventListener {

	/**
	 * @param model
	 * @return a new {@link HasChangedListener} instanced that listens to any
	 *         fired change events sent by any object or field in the model or
	 *         the model itself.
	 */
	static public HasChangedListener listen(XModel model) {
		HasChangedListener hc = new HasChangedListener();

		model.addListenerForModelEvents(hc);
		model.addListenerForObjectEvents(hc);
		model.addListenerForFieldEvents(hc);
		model.addListenerForTransactionEvents(hc);
		for (XId objectId : model) {
			XObject object = model.getObject(objectId);
			object.addListenerForObjectEvents(hc);
			object.addListenerForFieldEvents(hc);
			object.addListenerForTransactionEvents(hc);
			for (XId fieldId : object) {
				XField field = object.getField(fieldId);
				field.addListenerForFieldEvents(hc);
			}
		}

		return hc;
	}

	List<XEvent> events = new ArrayList<XEvent>();

	private void handle(XEvent event) {
		this.events.add(event);
	}

	@Override
	public void onChangeEvent(XFieldEvent event) {
		handle(event);
	}

	@Override
	public void onChangeEvent(XModelEvent event) {
		handle(event);
	}

	@Override
	public void onChangeEvent(XObjectEvent event) {
		handle(event);
	}

	@Override
	public void onChangeEvent(XRepositoryEvent event) {
		handle(event);
	}

	@Override
	public void onChangeEvent(XTransactionEvent event) {
		handle(event);
	}

	public boolean hasEventsReceived() {
		return !this.events.isEmpty();
	}

	public void reset() {
		this.events.clear();
	}

	public void dump() {
		System.out.println("Got these events:");
		for (XEvent event : this.events) {
			System.out.println(event);
		}
	}

}
