package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;


/**
 * An abstract and basic implementation of {@link XModelState}.
 * 
 * Management of child-{@link XObjectState XObjectStates} must be implemented by
 * sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractModelState extends AbstractState implements XModelState {
	
	private static final long serialVersionUID = -7700593025637594717L;
	
	private long revisionNumber = 0L;
	
	public AbstractModelState(XAddress modelAddr) {
		super(modelAddr);
		if(modelAddr.getAddressedType() != XType.XMODEL) {
			throw new RuntimeException("must be a model address, was: " + modelAddr);
		}
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public void setRevisionNumber(long revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
	
	@Override
	public String toString() {
		return "xmodel" + getAddress() + " [" + getRevisionNumber() + "]" + " = "
		        + XStateUtils.toString(iterator(), ",");
	}
	
	/* Content of model is ignored for hashCode */
	@Override
	public int hashCode() {
		return (int)(getAddress().hashCode() + getRevisionNumber());
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof XModelState)) {
			return false;
		}
		XModelState otherModel = (XModelState)other;
		if(!this.getAddress().equals(otherModel.getAddress())
		        && this.getRevisionNumber() == otherModel.getRevisionNumber()) {
			return false;
		}
		// compare content
		return XStateUtils.equals(this.iterator(), otherModel.iterator());
	}
	
	public XID getID() {
		return getAddress().getModel();
	}
	
	/**
	 * Checks whether the given {@link XObjectState} could be added as a child
	 * of this AbstractModelState.
	 * 
	 * @param objectState The {@link XObjectState} which is to be checked
	 * @throws IllegalArgumentException if the given {@link XObjectState} was
	 *             null or cannot be added to this AbstractModelState
	 */
	protected void checkObjectState(XObjectState objectState) {
		if(objectState == null) {
			throw new IllegalArgumentException("objectState was null");
		}
		if(!getAddress().contains(objectState.getAddress())) {
			throw new IllegalArgumentException("cannot add object state "
			        + objectState.getAddress() + " to " + getAddress());
		}
	}
	
}
