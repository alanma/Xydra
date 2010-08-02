package org.xydra.core.model.state;

import org.xydra.core.model.XAddress;


/**
 * Responsible for creating and loading state-objects (like {@link XModelState
 * XModelStates}).
 * 
 * Different persistence implementations will implement this interface
 * differently.
 * 
 * @author voelkel
 */
public interface XStateStore {
	
	/**
	 * Create a new {@link XFieldState} in the persistence layer represented by
	 * this XStateStore. The created {@link XFieldState} is not persisted until
	 * it is saved. Also, this doesn't check if there already exists an
	 * {@link XFieldState} with the given {@link XAddress}. Use
	 * {@link #loadFieldState(XAddress)} to load an existing {@link XFieldState
	 * XFieldStates}.
	 * 
	 * @param fieldStateID the {@link XAddress} for the new {@link XFieldState}
	 * @return a new, not yet persisted {@link XFieldState}, linked to this
	 *         {@link XStateStore}.
	 */
	XFieldState createFieldState(XAddress fieldStateAddress);
	
	/**
	 * Create a new {@link XObjectState} in the persistence layer represented by
	 * this XStateStore. The created {@link XObjectState} is not persisted until
	 * it is saved. Also, this doesn't check if there already exists an
	 * {@link XObjectState} with the given {@link XAddress}. Use
	 * {@link #loadObjectState(XAddress)} to load an existing
	 * {@link XObjectState} s.
	 * 
	 * @param objectStateID the {@link XAddress} for the new
	 *            {@link XObjectState}
	 * @return a new, not yet persisted {@link XObjectState}, linked to this
	 *         {@link XStateStore}.
	 */
	XObjectState createObjectState(XAddress objectStateAddress);
	
	/**
	 * Create a new {@link XModelState} in the persistence layer represented by
	 * this XStateStore. The created {@link XModelState} is not persisted until
	 * it is saved. Also, this doesn't check if there already exists an
	 * {@link XModelState} with the given {@link XAddress}. Use
	 * {@link #loadModelState(XAddress)} to load an existing {@link XModelState}
	 * s.
	 * 
	 * @param modelStateID the {@link XAddress} for the new {@link XModelState}
	 * @return a new, not yet persisted {@link XModelState}, linked to this
	 *         {@link XStateStore}.
	 */
	XModelState createModelState(XAddress modelStateAddress);
	
	/**
	 * Create a new {@link XRepositoryState} in the persistence layer
	 * represented by this XStateStore. The created {@link XRepositoryState} is
	 * not persisted until it is saved. Also, this doesn't check if there
	 * already exists an {@link XRepositoryState} with the given
	 * {@link XAddress}. Use {@link #loadRepositoryState(XAddress)} to load an
	 * existing {@link XRepositoryState}.
	 * 
	 * @param repositoryStateID the {@link XAddress} for the new
	 *            {@link XRepositoryState}
	 * @return a new, not yet persisted {@link XRepositoryState}, linked to this
	 *         {@link XStateStore}.
	 */
	XRepositoryState createRepositoryState(XAddress repositoryStateAddress);
	
	/**
	 * Checks whether an already persisted {@link XFieldState} with the given
	 * {@link XAddress} exists and returns it if this is the case.
	 * 
	 * @param fieldAddress The {@link XAddress} of the {@link XFieldState} which
	 *            is to be returned (must not be null)
	 * @return a previously persisted {@link XFieldState} or null, if no
	 *         {@link XFieldState} with the given {@link XAddress} exists.
	 * @throws IllegalArgumentException if the given {@link XAddress} equals
	 *             null.
	 */
	XFieldState loadFieldState(XAddress fieldStateAddress);
	
	/**
	 * Checks whether an already persisted {@link XObjectState} with the given
	 * {@link XAddress} exists and returns it if this is the case.
	 * 
	 * @param fieldAddress The {@link XAddress} of the {@link XObjectState}
	 *            which is to be returned (must not be null)
	 * @return a previously persisted {@link XObjectState} or null, if no
	 *         {@link ObjectState} with the given {@link XAddress} exists.
	 * @throws IllegalArgumentException if the given {@link XAddress} equals
	 *             null.
	 */
	XObjectState loadObjectState(XAddress objectStateAddress);
	
	/**
	 * Checks whether an already persisted {@link XModelState} with the given
	 * {@link XAddress} exists and returns it if this is the case.
	 * 
	 * @param fieldAddress The {@link XAddress} of the {@link XModelState} which
	 *            is to be returned (must not be null)
	 * @return a previously persisted {@link XModelState} or null, if no
	 *         {@link XModelState} with the given {@link XAddress} exists.
	 * @throws IllegalArgumentException if the given {@link XAddress} equals
	 *             null.
	 */
	XModelState loadModelState(XAddress modelStateAddress);
	
	/**
	 * Checks whether an already persisted {@link XRepositoryState} with the
	 * given {@link XAddress} exists and returns it if this is the case.
	 * 
	 * @param fieldAddress The {@link XAddress} of the {@link XRepositoryState}
	 *            which is to be returned must not be null)
	 * @return a previously persisted {@link XRepositoryState} or null, if no
	 *         {@link XRepositoryState} with the given {@link XAddress} exists.
	 * @throws IllegalArgumentException if the given {@link XAddress} equals
	 *             null.
	 */
	XRepositoryState loadRepositoryState(XAddress repositoryStateAddress);
	
}
