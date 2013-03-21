/**
 * 
 */
package org.xydra.core;

import org.xydra.base.XId;
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
 * A listener for events of type T that records in a boolean variable if any
 * events have been received.
 * 
 * @author dscharrer
 */
public class HasChanged implements XRepositoryEventListener, XModelEventListener,
        XObjectEventListener, XFieldEventListener, XTransactionEventListener {
	
	/**
	 * @param model
	 * @return a new {@link HasChanged} instanced that listens to any fired
	 *         change events sent by any object or field in the model or the
	 *         model itself.
	 */
	static public HasChanged listen(XModel model) {
		HasChanged hc = new HasChanged();
		
		model.addListenerForModelEvents(hc);
		model.addListenerForObjectEvents(hc);
		model.addListenerForFieldEvents(hc);
		model.addListenerForTransactionEvents(hc);
		for(XId objectId : model) {
			XObject object = model.getObject(objectId);
			object.addListenerForObjectEvents(hc);
			object.addListenerForFieldEvents(hc);
			object.addListenerForTransactionEvents(hc);
			for(XId fieldId : object) {
				XField field = object.getField(fieldId);
				field.addListenerForFieldEvents(hc);
			}
		}
		
		return hc;
	}
	
	public boolean eventsReceived = false;
	
	@Override
	public void onChangeEvent(XFieldEvent event) {
		this.eventsReceived = true;
	}
	
	@Override
	public void onChangeEvent(XModelEvent event) {
		this.eventsReceived = true;
	}
	
	@Override
	public void onChangeEvent(XObjectEvent event) {
		this.eventsReceived = true;
	}
	
	@Override
	public void onChangeEvent(XRepositoryEvent event) {
		this.eventsReceived = true;
	}
	
	@Override
	public void onChangeEvent(XTransactionEvent event) {
		this.eventsReceived = true;
	}
	
}
