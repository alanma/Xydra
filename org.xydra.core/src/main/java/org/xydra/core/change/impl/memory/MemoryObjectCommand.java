package org.xydra.core.change.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;


/**
 * An implementation of {@link XObjectCommand}
 * 
 */

public class MemoryObjectCommand extends MemoryAtomicCommand implements XObjectCommand {
	
	// ID of the field being added or removed
	private final XID fieldId;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XObjectCommand))
			return false;
		XObjectCommand command = (XObjectCommand)object;
		
		if(!this.fieldId.equals(command.getFieldID()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.fieldId.hashCode();
		
		return result;
	}
	
	private MemoryObjectCommand(XAddress target, ChangeType changeType, long fieldRevision,
	        XID fieldId) {
		super(target, changeType, fieldRevision);
		
		if(target.getObject() == null)
			throw new IllegalArgumentException("target must specify an object, was:" + target);
		
		if(target.getField() != null)
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		
		if(fieldId == null)
			throw new IllegalArgumentException("the field id must not be null");
		
		this.fieldId = fieldId;
		
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type. Will add a new
	 * {@link XField} with the specified {@link XID} to the {@link XObject} this
	 * event applies to, if possible.
	 * 
	 * @param target The target of this command - object {@link XID} must not be
	 *            null, field {@link XID} has to be null
	 * @param isForced determines whether this command will be a forced or a
	 *            safe command.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            added
	 * @return A new {@link XObjectCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XField} or if the given fieldID is null
	 */
	public static XObjectCommand createAddCommand(XAddress target, boolean isForced, XID fieldID) {
		if(isForced) {
			return createAddCommand(target, XCommand.FORCED, fieldID);
		} else {
			return createAddCommand(target, XCommand.SAFE, fieldID);
		}
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the add-type. Will add a new
	 * {@link XField} with the specified {@link XID} to the {@link XObject} this
	 * event applies to, if possible.
	 * 
	 * @param target The target of this command - object {@link XID} must not be
	 *            null, field {@link XID} has to be null
	 * @param fieldRevision Must be {@link XCommand.FORCED} or
	 *            {@link XCommand.SAFE} to determine the behavior of this
	 *            command.
	 * @param fieldId The {@link XID} for the {@link XField} which is to be
	 *            added
	 * @return A new {@link XObjectCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XField} or if the given fieldID is null
	 */
	public static XObjectCommand createAddCommand(XAddress target, long fieldRevision, XID fieldId) {
		
		if(fieldRevision != XCommand.FORCED && fieldRevision != XCommand.SAFE)
			throw new RuntimeException("invalid revision for an XObjectCommand of type ADD: "
			        + fieldRevision);
		
		return new MemoryObjectCommand(target, ChangeType.ADD, fieldRevision, fieldId);
	}
	
	/**
	 * Creates a new {@link XObjectCommand} of the remove-type. Will remove the
	 * specified {@link XField} from the {@link XObject} this event applies to,
	 * if possible.
	 * 
	 * @param target The target of this command - object {@link XID} must not be
	 *            null, field {@link XID} has to be null
	 * @param fieldRevision The current revision number of the {@link XField}
	 *            which is to be removed
	 * @param fieldId The {@link XID} of the {@link XField} which is to be
	 *            removed
	 * @return A new {@link XObjectCommand} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XField} or if the given fieldID is null
	 */
	public static XObjectCommand createRemoveCommand(XAddress target, long fieldRevision,
	        XID fieldId) {
		return new MemoryObjectCommand(target, ChangeType.REMOVE, fieldRevision, fieldId);
	}
	
	@Override
	public XID getFieldID() {
		return this.fieldId;
	}
	
	@Override
	public String toString() {
		String str = "ObjectCommand: " + getChangeType() + " " + this.fieldId;
		if(isForced())
			str += " (forced)";
		else if(getChangeType() == ChangeType.ADD)
			str += " (safe)";
		else
			str += " r" + getRevisionNumber();
		str += " @" + getTarget();
		return str;
	}
	
	public XAddress getChangedEntity() {
		return XX.resolveField(getTarget(), getFieldID());
	}
	
}
