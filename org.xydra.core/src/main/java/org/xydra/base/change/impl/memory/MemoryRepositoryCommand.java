package org.xydra.base.change.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


/**
 * An implementation of {@link XRepositoryCommand}.
 * 
 */

public class MemoryRepositoryCommand extends MemoryAtomicCommand implements XRepositoryCommand {
	
	private static final long serialVersionUID = -6723215151804666417L;
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the add-type. Will add a new
	 * {@link XModel} with the specified {@link XID} to the {@link XRepository}
	 * this event applies to, if possible.
	 * 
	 * @param target The {@link XAddress} of the {@link XRepository} this
	 *            command applies to - repository {@link XID} must not be null,
	 *            model, object & field {@link XID} must be null
	 * @param isForced determines whether this command will be a forced or a
	 *            safe command.
	 * @param modelId The {@link XID} for the {@link XModel} which is to be
	 *            added
	 * @return A new {@link XRepositoryCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             refer to an {@link XRepository} or if the given modelId is
	 *             null
	 */
	public static XRepositoryCommand createAddCommand(XAddress target, boolean isForced, XID modelId) {
		if(isForced) {
			return createAddCommand(target, XCommand.FORCED, modelId);
		} else {
			return createAddCommand(target, XCommand.SAFE, modelId);
		}
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the add-type. Will add a new
	 * {@link XModel} with the specified {@link XID} to the {@link XRepository}
	 * this event applies to, if possible.
	 * 
	 * @param target The {@link XAddress} of the {@link XRepository} this
	 *            command applies to - repository ID must not be null, model,
	 *            object & field ID must be null
	 * @param modelRevision Must be {@link XCommand#FORCED} or
	 *            {@link XCommand#SAFE} to determine the behavior of this
	 *            command.
	 * @param modelId The {@link XID} for the {@link XModel} which is to be
	 *            added
	 * @return A new {@link XRepositoryCommand} of the add-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             refer to an {@link XRepository} or if the given modelId is
	 *             null
	 */
	public static XRepositoryCommand createAddCommand(XAddress target, long modelRevision,
	        XID modelId) {
		
		if(modelRevision != XCommand.FORCED && modelRevision != XCommand.SAFE)
			throw new RuntimeException("invalid revision for an XObjectCommand of type ADD: "
			        + modelRevision);
		
		return new MemoryRepositoryCommand(target, ChangeType.ADD, modelRevision, modelId);
	}
	
	/**
	 * Creates a new {@link XRepositoryCommand} of the remove-type. Will remove
	 * the {@link XModel} with the specified {@link XID} from the
	 * {@link XRepository} this event applies to, if possible.
	 * 
	 * @param target The {@link XAddress} of the {@link XRepository} this
	 *            command applies to - repository {@link XID} must not be null,
	 *            model, object & field {@link XID} must be null
	 * @param modelRevision Must be {@link XCommand#FORCED} or
	 *            {@link XCommand#SAFE} to determine the behavior of this
	 *            command.
	 * @param modelId The {@link XID} of the {@link XModel} which is to be
	 *            removed
	 * @return A new {@link XRepositoryCommand} of the remove-type
	 * @throws IllegalArgumentException if the given {@link XAddress} does not
	 *             refer to an {@link XRepository} or if the given modelId is
	 *             null
	 */
	public static XRepositoryCommand createRemoveCommand(XAddress target, long modelRevision,
	        XID modelId) {
		return new MemoryRepositoryCommand(target, ChangeType.REMOVE, modelRevision, modelId);
	}
	
	// ID of the model to be added or removed
	private final XID modelId;
	
	private MemoryRepositoryCommand(XAddress target, ChangeType changeType, long modelRevision,
	        XID modelId) {
		super(target, changeType, modelRevision);
		
		if(target.getRepository() == null)
			throw new IllegalArgumentException("target must specify a repository, was:" + target);
		
		if(target.getModel() != null)
			throw new IllegalArgumentException("target must not specify a model, was:" + target);
		
		if(target.getObject() != null)
			throw new IllegalArgumentException("target must not specify an object, was:" + target);
		
		if(target.getField() != null)
			throw new IllegalArgumentException("target must not specify a field, was:" + target);
		
		if(modelId == null)
			throw new IllegalArgumentException("the model id must not be null");
		
		this.modelId = modelId;
		
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(!super.equals(object))
			return false;
		
		if(!(object instanceof XRepositoryCommand))
			return false;
		XRepositoryCommand command = (XRepositoryCommand)object;
		
		if(!this.modelId.equals(command.getModelId()))
			return false;
		
		return true;
	}
	
	public XAddress getChangedEntity() {
		return XX.resolveModel(getTarget(), getModelId());
	}
	
	@Override
	public XID getModelId() {
		return this.modelId;
	}
	
	@Override
	public int hashCode() {
		
		int result = super.hashCode();
		
		// newValue
		result ^= this.modelId.hashCode();
		
		return result;
	}
	
	@Override
	public String toString() {
		String str = "RepositoryCommand: " + getChangeType() + " " + this.modelId;
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
