package org.xydra.core.model.state.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;


/**
 * Management of children (especially 'iterator()') is implemented by
 * sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractModelState extends AbstractState implements XModelState {
	
	private static final long serialVersionUID = -7700593025637594717L;
	
	private long revisionNumber = 0L;
	
	public AbstractModelState(XAddress modelAddr) {
		super(modelAddr);
		if(MemoryAddress.getAddressedType(modelAddr) != XType.XMODEL) {
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
		        + Utils.toString(iterator(), ",");
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
		return Utils.equals(this.iterator(), otherModel.iterator());
	}
	
	public XID getID() {
		return getAddress().getModel();
	}
	
	protected void checkObjectState(XObjectState objectState) {
		if(objectState == null) {
			throw new IllegalArgumentException("objectState was null");
		}
		if(!XX.contains(getAddress(), objectState.getAddress())) {
			throw new IllegalArgumentException("cannot add object state "
			        + objectState.getAddress() + " to " + getAddress());
		}
	}
	
}
