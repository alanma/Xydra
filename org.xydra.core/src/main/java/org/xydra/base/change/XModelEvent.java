package org.xydra.base.change;

import org.xydra.base.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


/**
 * An {@link XEvent} representing changes of {@link XModel XModels}.
 * 
 * @author Kaidel
 * 
 */
public interface XModelEvent extends XAtomicEvent {
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the changed {@link XModel}
	 */
	XID getModelId();
	
	/**
	 * WHAT has been changed?
	 * 
	 * @return the {@link XID} of the added/removed {@link XObject}.
	 */
	XID getObjectId();
	
	/**
	 * WHERE did the change happen?
	 * 
	 * @return the {@link XID} of the Parent-{@link XRepository} of the
	 *         {@link XModel} where the change happened. It may be null.
	 */
	XID getRepositoryId();
}