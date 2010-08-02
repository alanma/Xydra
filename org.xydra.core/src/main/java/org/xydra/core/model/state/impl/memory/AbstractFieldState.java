package org.xydra.core.model.state.impl.memory;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.value.XValue;


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
		if(MemoryAddress.getAddressedType(fieldAddr) != XType.XFIELD) {
			throw new RuntimeException("must be a field address, was: " + fieldAddr);
		}
	}
	
	public long getRevisionNumber() {
		return this.revisionNumber;
	}
	
	public XValue getValue() {
		return this.value;
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
	
	@Override
	public int hashCode() {
		return (int)(getAddress().hashCode() + getRevisionNumber() + (getValue() == null ? 0
		        : getValue().hashCode()));
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
	
}
