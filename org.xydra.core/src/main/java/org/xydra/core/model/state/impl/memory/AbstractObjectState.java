package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;


/**
 * An abstract and basic implementation of {@link XObjectState}.
 * 
 * Management of child-{@link XModelState XModelStates} must be implemented by
 * sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractObjectState extends AbstractState implements XObjectState {
	
	private static final long serialVersionUID = -5023507446999380441L;
	
	private long revisionNumber;
	
	public AbstractObjectState(XAddress objectAddr) {
		super(objectAddr);
		if(objectAddr.getAddressedType() != XType.XOBJECT) {
			throw new RuntimeException("must be an object address, was: " + objectAddr);
		}
	}
	
	/**
	 * Checks whether the given {@link XFieldState} could be added as a child of
	 * this AbstractObjectState.
	 * 
	 * @param fieldState The {@link XFieldState} which is to be checked
	 * @throws IllegalArgumentException if the given {@link XFieldState} was
	 *             null or cannot be added to this AbstractObjectState
	 */
	protected void checkFieldState(XFieldState fieldState) {
		if(fieldState == null) {
			throw new IllegalArgumentException("fieldState was null");
		}
		if(!getAddress().contains(fieldState.getAddress())) {
			throw new IllegalArgumentException("cannot add field state " + fieldState.getAddress()
			        + " to " + getAddress());
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof XObjectState)) {
			return false;
		}
		XObjectState otherObject = (XObjectState)other;
		if(!this.getAddress().equals(otherObject.getAddress())
		        && this.getRevisionNumber() == otherObject.getRevisionNumber()) {
			return false;
		}
		// compare content
		return XStateUtils.equals(this.iterator(), otherObject.iterator());
	}
	
	public XID getID() {
		return getAddress().getObject();
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	/* Content of object is ignored for hashCode */
	@Override
	public int hashCode() {
		return (int)(getAddress().hashCode() + getRevisionNumber());
	}
	
	public void setRevisionNumber(long revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
	
	@Override
	public String toString() {
		return "xobject " + getAddress() + " [" + getRevisionNumber() + "]" + " = "
		        + XStateUtils.toString(iterator(), ",");
	}
	
}
