package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.value.XValue;


public class MemoryFieldCommand extends MemoryAtomicCommand implements XFieldCommand {
	
	private final XValue newValue;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XFieldCommand))
			return false;
		XFieldCommand command = (XFieldCommand)object;
		
		if(!XX.equals(this.newValue, command.getValue()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= (this.newValue == null ? 0 : this.newValue.hashCode());
		
		return result;
	}
	
	private MemoryFieldCommand(XAddress target, ChangeType changeType, long fieldRevision,
	        XValue newValue) {
		super(target, changeType, fieldRevision);
		
		if(target.getField() == null)
			throw new IllegalArgumentException(
			        "targets of XFieldCommands must specify a field, was:" + target);
		
		this.newValue = newValue;
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the add-type.
	 * 
	 * @param target The {@link XAddress} of the {@link XField} which value is
	 *            to be set.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which value is to be set. The field ID must not be null
	 * @param newValue The new {@link XValue} for the specified {@link XField} -
	 *            must not be null
	 * @return Creates a new {@link XFieldCommand} of the add-type for the
	 *         specified target.
	 */
	public static XFieldCommand createAddCommand(XAddress target, long fieldRevision,
	        XValue newValue) {
		
		if(newValue == null)
			throw new NullPointerException("an ADD command must have a non-null value");
		
		return new MemoryFieldCommand(target, ChangeType.ADD, fieldRevision, newValue);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the remove-type.
	 * 
	 * @param target The {@link XAddress} of the {@link XField} which value is
	 *            to be set.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which value is to be set. The field ID must not be null
	 * @return Creates a new {@link XFieldCommand} of the remove-type for the
	 *         specified target.
	 */
	public static XFieldCommand createRemoveCommand(XAddress target, long fieldRevision) {
		return new MemoryFieldCommand(target, ChangeType.REMOVE, fieldRevision, null);
	}
	
	/**
	 * Creates a new {@link XFieldCommand} of the change-type.
	 * 
	 * @param target The {@link XAddress} of the {@link XField} which value is
	 *            to be set.
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which value is to be set. The field ID must not be null
	 * @param newValue The new {@link XValue} for the specified {@link XField} -
	 *            must not be null
	 * @return Creates a new {@link XFieldCommand} of the change-type for the
	 *         specified target.
	 */
	public static XFieldCommand createChangeCommand(XAddress target, long fieldRevision,
	        XValue newValue) {
		
		if(newValue == null)
			throw new NullPointerException("a CHANGE command must have a non-null value");
		
		return new MemoryFieldCommand(target, ChangeType.CHANGE, fieldRevision, newValue);
	}
	
	public XValue getValue() {
		return this.newValue;
	}
	
	@Override
	public String toString() {
		String suffix = " @" + getTarget();
		if(isForced())
			suffix += " (forced)";
		else
			suffix += " r" + getRevisionNumber();
		switch(getChangeType()) {
		case ADD:
			return "FieldCommand: ADD " + this.newValue + suffix;
		case REMOVE:
			return "FieldCommand: REMOVE value " + suffix;
		case CHANGE:
			return "FieldCommand: CHANGE value to " + this.newValue + suffix;
		default:
			throw new RuntimeException("this field event should have never been created");
		}
	}
}
