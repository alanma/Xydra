package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;


/**
 * An abstract and basic implementation of {@link XModelState}.
 * 
 * Management of child-{@link XObjectState XObjectStates} must be implemented by
 * sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractRepositoryState extends AbstractState implements XRepositoryState {
	
	private static final long serialVersionUID = 6618821523238475778L;
	
	public AbstractRepositoryState(XAddress repoAddr) {
		super(repoAddr);
		if(repoAddr.getAddressedType() != XType.XREPOSITORY) {
			throw new RuntimeException("must be a repository address, was: " + repoAddr);
		}
	}
	
	@Override
	public String toString() {
		return "xrepository" + getAddress().toString() + " = " + XStateUtils.toString(iterator(), ",");
	}
	
	/* Content of repository is ignored for hashCode */
	@Override
	public int hashCode() {
		return getAddress().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof XRepositoryState)) {
			return false;
		}
		XRepositoryState otherRepository = (XRepositoryState)other;
		if(!getAddress().equals(otherRepository.getAddress())) {
			return false;
		}
		// compare content
		return XStateUtils.equals(iterator(), otherRepository.iterator());
	}
	
	public XID getID() {
		return getAddress().getRepository();
	}
	
	/**
	 * Checks whether the given {@link XModelState} could be added as a child of
	 * this AbstractRepositoryState.
	 * 
	 * @param modelState The {@link XModelState} which is to be checked
	 * @throws IllegalArgumentException if the given {@link XModelState} was
	 *             null or cannot be added to this AbstractRepositoryState
	 */
	protected void checkModelState(XModelState modelState) {
		if(modelState == null) {
			throw new IllegalArgumentException("modelState was null");
		}
		if(!getAddress().contains(modelState.getAddress())) {
			throw new IllegalArgumentException("cannot add model state " + modelState.getAddress()
			        + " to " + getAddress());
		}
	}
	
}
