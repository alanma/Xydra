package org.xydra.core.model.state.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.core.model.state.XFieldState;


/**
 * An abstract and basic implementation of {@link XFieldState}.
 * 
 * Here eager and lazy implementation are the same, as there are no children.
 * 
 * @author voelkel
 * 
 */
public abstract class AbstractFieldState extends AbstractState implements XFieldState {
	
	private static final long serialVersionUID = -4924597306047670433L;
	
	protected long revisionNumber = 0L;
	
	/** may be null */
	protected XValue value;
	
	public AbstractFieldState(XAddress fieldAddr) {
		super(fieldAddr);
		if(fieldAddr.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("must be a field address, was: " + fieldAddr);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof XFieldState)) {
			return false;
		}
		XFieldState otherField = (XFieldState)other;
		
		if(!getAddress().equals(otherField.getAddress())) {
			return false;
		}
		if(this.getRevisionNumber() != otherField.getRevisionNumber()) {
			return false;
		}
		
		if(this.getValue() == null) {
			if(otherField.getValue() != null) {
				return false;
			}
		} else {
			if(otherField.getValue() == null) {
				return false;
			}
			if(!this.getValue().equals(otherField.getValue())) {
				return false;
			}
		}
		
		return true;
	}
	
	public XID getID() {
		return getAddress().getField();
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	@Override
	public int hashCode() {
		return (int)(getAddress().hashCode() + getRevisionNumber() + (getValue() == null ? 0
		        : getValue().hashCode()));
	}
	
	public void setRevisionNumber(long revisionNumber) {
		this.revisionNumber = revisionNumber;
	}
	
	public void setValue(XValue value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "xfield " + getAddress() + " [" + getRevisionNumber() + "]" + " = " + getValue();
	}
	
}
