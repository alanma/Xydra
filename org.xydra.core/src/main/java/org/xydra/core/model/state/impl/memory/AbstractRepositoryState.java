package org.xydra.core.model.state.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XRepositoryState;


/**
 * Management of children (iterator()) is implemented by sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractRepositoryState extends AbstractState implements XRepositoryState {
	
	private static final long serialVersionUID = 6618821523238475778L;
	
	public AbstractRepositoryState(XAddress repoAddr) {
		super(repoAddr);
		if(MemoryAddress.getAddressedType(repoAddr) != XType.XREPOSITORY) {
			throw new RuntimeException("must be a repository address, was: " + repoAddr);
		}
	}
	
	@Override
	public String toString() {
		return "xrepository" + getAddress().toString() + " = " + Utils.toString(iterator(), ",");
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
		return Utils.equals(iterator(), otherRepository.iterator());
	}
	
	public XID getID() {
		return getAddress().getRepository();
	}

	protected void checkModelState(XModelState modelState) {
        if(modelState == null) {
    		throw new IllegalArgumentException("modelState was null");
    	}
    	if(!XX.contains(getAddress(), modelState.getAddress())) {
    		throw new IllegalArgumentException("cannot add model state " + modelState.getAddress()
    		        + " to " + getAddress());
    	}
    }
	
}
