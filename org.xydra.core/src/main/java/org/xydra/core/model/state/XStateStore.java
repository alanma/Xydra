package org.xydra.core.model.state;

import org.xydra.core.model.XAddress;


/**
 * Responsible for creating and loading State-things.
 * 
 * Different persistence implementations will implement this differently.
 * 
 * @author voelkel
 */
public interface XStateStore {
	
	/**
	 * @param fieldStateID
	 * @return a new, not yet persisted {@link XFieldState}, linked to this
	 *         {@link XStateStore}.
	 */
	XFieldState createFieldState(XAddress fieldStateAddress);
	
	/**
	 * @param modelStateID
	 * @return a new, not yet persisted {@link XModelState}, linked to this
	 *         {@link XStateStore}.
	 */
	XModelState createModelState(XAddress modelStateAddress);
	
	/**
	 * @param objectStateID
	 * @return a new, not yet persisted {@link XObjectState}, linked to this
	 *         {@link XStateStore}.
	 */
	XObjectState createObjectState(XAddress objectStateAddress);
	
	/**
	 * @param repositoryStateID
	 * @return a new, not yet persisted {@link XRepositoryState}, linked to this
	 *         {@link XStateStore}.
	 */
	XRepositoryState createRepositoryState(XAddress repositoryStateAddress);
	
	/**
	 * @param fieldAddress never null
	 * @return a previously persisted {@link XFieldState} or null.
	 */
	XFieldState loadFieldState(XAddress fieldStateAddress);
	
	/**
	 * @param modelAddress never null
	 * @return a previously persisted {@link XModelState} or null.
	 */
	XModelState loadModelState(XAddress modelStateAddress);
	
	/**
	 * @param objectAddress never null
	 * @return a previously persisted {@link XObjectState} or null.
	 */
	XObjectState loadObjectState(XAddress objectStateAddress);
	
	/**
	 * @param repositoryAddress never null
	 * @return a previously persisted {@link XRepositoryState} or null.
	 */
	XRepositoryState loadRepositoryState(XAddress repositoryStateAddress);
	
}
