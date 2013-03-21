package org.xydra.base.change;

import org.xydra.base.XId;
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
	 * @return the {@link XId} of the added/removed {@link XField}
	 */
	XId getFieldId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XId} of the Parent-{@link XModel} of the
	 *         {@link XObject} where the change happened. It may be null.
	 */
	XId getModelId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XId} of the changed {@link XObject}
	 */
	XId getObjectId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XId} of the Parent-{@link XRepository} of the
	 *         {@link XObject} where the change happened. It may be null.
	 */
	XId getRepositoryId();
}
