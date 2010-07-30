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
	 * Create a new {@link XFieldState} in the persistence layer represented by
	 * this state store. The created state is not persisted until it is saved.
	 * Also, this doesn't check if a state with this address already exists. Use
	 * {@link #loadFieldState(XAddress)} to load existing {@link XFieldState}s.
	 * 
	 * @param fieldStateID
	 * @return a new, not yet persisted {@link XFieldState}, linked to this
	 *         {@link XStateStore}.
	 */
	XFieldState createFieldState(XAddress fieldStateAddress);
	
	/**
	 * Create a new {@link XObjectState} in the persistence layer represented by
	 * this state store. The created state is not persisted until it is saved.
	 * Also, this doesn't check if a state with this address already exists. Use
	 * {@link #loadObjectState(XAddress)} to load existing {@link XObjectState}
	 * s.
	 * 
	 * @param objectStateID
	 * @return a new, not yet persisted {@link XObjectState}, linked to this
	 *         {@link XStateStore}.
	 */
	XObjectState createObjectState(XAddress objectStateAddress);
	
	/**
	 * Create a new {@link XModelState} in the persistence layer represented by
	 * this state store. The created state is not persisted until it is saved.
	 * Also, this doesn't check if a state with this address already exists. Use
	 * {@link #loadModelState(XAddress)} to load existing {@link XModelState}s.
	 * 
	 * @param modelStateID
	 * @return a new, not yet persisted {@link XModelState}, linked to this
	 *         {@link XStateStore}.
	 */
	XModelState createModelState(XAddress modelStateAddress);
	
	/**
	 * Create a new {@link XRepositoryState} in the persistence layer
	 * represented by this state store. The created state is not persisted until
	 * it is saved. Also, this doesn't check if a state with this address
	 * already exists. Use {@link #loadRepositoryState(XAddress)} to load
	 * existing {@link XRepositoryState}s.
	 * 
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
	 * @param objectAddress never null
	 * @return a previously persisted {@link XObjectState} or null.
	 */
	XObjectState loadObjectState(XAddress objectStateAddress);
	
	/**
	 * @param modelAddress never null
	 * @return a previously persisted {@link XModelState} or null.
	 */
	XModelState loadModelState(XAddress modelStateAddress);
	
	/**
	 * @param repositoryAddress never null
	 * @return a previously persisted {@link XRepositoryState} or null.
	 */
	XRepositoryState loadRepositoryState(XAddress repositoryStateAddress);
	
}
