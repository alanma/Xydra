package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


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
	 * @return the {@link XID} of the changed {@link XField}
	 */
	XID getFieldId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XModel} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getModelId();
	
	/**
	 * HOW has the {@link XValue} of the {@link XField} been changed?
	 * 
	 * To get the old value, use {@link XModel#getChangeLog()} and get the event
	 * that has the revision number of {@link #getOldFieldRevision()}.
	 * 
	 * @return the new {@link XValue}. null indicates a remove event.
	 */
	XValue getNewValue();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XObject} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getObjectId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XField} where the change happened. It may be null.
	 */
	XID getRepositoryId();
	
}
