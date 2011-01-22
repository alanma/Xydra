package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An {@link XEvent} representing changes of {@link XObject XObjects}
 * 
 * 
 * @author voelkel
 * 
 */
public interface XObjectEvent extends XAtomicEvent {
	
	/**
	 * WHAT was changed?
	 * 
	 * @return the {@link XID} of the added/removed {@link XField}
	 */
	XID getFieldId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XModel} of the
	 *         {@link XObject} where the change happened. It may be null.
	 */
	XID getModelId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the changed {@link XObject}
	 */
	XID getObjectId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XObject} where the change happened. It may be null.
	 */
	XID getRepositoryId();
}
