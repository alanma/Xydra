package org.xydra.core.change.impl.memory;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;


/**
 * An implementation of {@link XModelCommand}
 * 
 */

public class MemoryModelCommand extends MemoryAtomicCommand implements XModelCommand {
	
	// ID of the object being added or removed
	private final XID objectId;
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XModelCommand))
			return false;
		XModelCommand command = (XModelCommand)object;
		
		if(!this.objectId.equals(command.getObjectID()))
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.objectId.hashCode();
		
		return result;
	}
	
	private MemoryModelCommand(XAddress target, ChangeType changeType, long objectRevision,
	        XID objectId) {
		super(target, changeType, objectRevision);
		
		if(target.getModel() == null)
			throw new IllegalArgumentException("target must specify a model, was:" + target);
		
		if(target.getObject() != null)
			throw new IllegalArgumentException("target must not specify an object, was:" + target);
		
		if(target.getField() != null)
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		
		if(objectId == null)
			throw new IllegalArgumentException("the object id must not be null");
		
		this.objectId = objectId;
		
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type. Will add a new
	 * {@link XObject} with the specified {@link XID} to the {@link XModel} this
	 * event applies to, if possible.
	 * 
	 * @param target The target of this command - the model {@link XID} must not
	 *            be null, object & field {@link XID} must be null
	 * @param isForced determines whether this command will be a forced or a
	 *            safe command.
	 * @param objectId The {@link XID} for the {@link XObject} which is to be
	 *            added
	 * @return A new {@link XModelCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XModel} or if the given objectID is null
	 */
	public static XModelCommand createAddCommand(XAddress target, boolean isForced, XID objectID) {
		if(isForced) {
			return createAddCommand(target, XCommand.FORCED, objectID);
		} else {
			return createAddCommand(target, XCommand.SAFE, objectID);
		}
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the add-type. Will add a new
	 * {@link XObject} with the specified {@link XID} to the {@link XModel} this
	 * event applies to, if possible.
	 * 
	 * @param target The target of this command - the model {@link XID} must not
	 *            be null, object & field {@link XID} must be null
	 * @param objectRevision Must be {@link XCommand.FORCED} or
	 *            {@link XCommand.SAFE} to determine the behavior of this
	 *            command.
	 * @param objectId The {@link XID} for the {@link XObject} which is to be
	 *            added
	 * @return A new {@link XModelCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XModel}, if the given objectRevison is
	 *             neither {@link XCommand#FORCED} or {@link XCommand#SAFE} or
	 *             if the given objectID is null
	 */
	public static XModelCommand createAddCommand(XAddress target, long objectRevision, XID objectId) {
		
		if(objectRevision != XCommand.FORCED && objectRevision != XCommand.SAFE)
			throw new IllegalArgumentException(
			        "invalid revision for an XObjectCommand of type ADD: " + objectRevision);
		
		return new MemoryModelCommand(target, ChangeType.ADD, objectRevision, objectId);
	}
	
	/**
	 * Creates a new {@link XModelCommand} of the remove-type. Will remove the
	 * specified {@link XObject} from the {@link XModel} this event applies to.
	 * 
	 * @param target The target of this command - the model {@link XID} must not
	 *            be null, object & field {@link XID} must be null
	 * @param objectRevision The current revision number of the {@link XObject}
	 *            which is to be removed
	 * @param objectId The {@link XID} of the {@link XObject} which is to be
	 *            removed
	 * @return A new {@link XModelCommand} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} doesn't
	 *             refer to an {@link XModel} or if the given objectID is null
	 */
	public static XModelCommand createRemoveCommand(XAddress target, long objectRevision,
	        XID objectId) {
		return new MemoryModelCommand(target, ChangeType.REMOVE, objectRevision, objectId);
	}
	
	@Override
	public XID getObjectID() {
		return this.objectId;
	}
	
	@Override
	public String toString() {
		String str = "ModelCommand: " + getChangeType() + " " + this.objectId;
		if(isForced())
			str += " (forced)";
		else if(getChangeType() == ChangeType.ADD)
			str += " (safe)";
		else
			str += " r" + getRevisionNumber();
		str += " @" + getTarget();
		return str;
	}
	
}
