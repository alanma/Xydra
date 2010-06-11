/**
 * 
 */
package org.xydra.core.test;

import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEvent;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * A listener for events of type T that records in a boolean variable if any
 * events have been received.
 * 
 * @param <T>
 */
public class HasChanged implements XRepositoryEventListener, XModelEventListener,
        XObjectEventListener, XFieldEventListener, XTransactionEventListener {
	
	public void onChangeEvent(XRepositoryEvent event) {
		this.eventsReceived = true;
	}
	
	public void onChangeEvent(XModelEvent event) {
		this.eventsReceived = true;
	}
	
	public void onChangeEvent(XObjectEvent event) {
		this.eventsReceived = true;
	}
	
	public void onChangeEvent(XFieldEvent event) {
		this.eventsReceived = true;
	}
	
	public void onChangeEvent(XTransactionEvent event) {
		this.eventsReceived = true;
	}
	
	public boolean eventsReceived = false;
	
	/**
	 * Listen for any fired change events sent by any object or field in the
	 * model or the model itself.
	 */
	static public HasChanged listen(XModel model) {
		HasChanged hc = new HasChanged();
		
		model.addListenerForModelEvents(hc);
		model.addListenerForObjectEvents(hc);
		model.addListenerForFieldEvents(hc);
		model.addListenerForTransactionEvents(hc);
		for(XID objectId : model) {
			XObject object = model.getObject(objectId);
			object.addListenerForObjectEvents(hc);
			object.addListenerForFieldEvents(hc);
			object.addListenerForTransactionEvents(hc);
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				field.addListenerForFieldEvents(hc);
			}
		}
		
		return hc;
	}
	
}
