package org.xydra.core.change;

import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


/**
 * An {@link XEvent} representing changes of {@link XField XFields}
 * 
 * @author Kaidel
 * 
 */

public interface XFieldEvent extends XAtomicEvent {
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getRepositoryID();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XModel} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getModelID();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XObject} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getObjectID();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the changed {@link XField}
	 */
	XID getFieldID();
	
	/**
	 * HOW has the {@link XValue} of the {@link XField} been changed?
	 * 
	 * @return the new {@link XValue}. null indicates a remove event.
	 */
	XValue getNewValue();
	
	/**
	 * WHAT was changed?
	 * 
	 * @return the old {@link XValue} before the change happened. null indicates
	 *         an add event.
	 */
	XValue getOldValue();
	
}
