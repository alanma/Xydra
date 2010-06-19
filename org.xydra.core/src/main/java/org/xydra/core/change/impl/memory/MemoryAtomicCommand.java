package org.xydra.core.change.impl.memory;

import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;


abstract public class MemoryAtomicCommand implements XAtomicCommand {
	
	private XAddress target;
	private final ChangeType changeType;
	private final long revision;
	
	protected MemoryAtomicCommand(XAddress target, ChangeType changeType, long revision) {
		
		if(target == null)
			throw new NullPointerException("target must not be null");
		
		if(revision < 0 && revision != XCommand.SAFE && revision != XCommand.FORCED)
			throw new RuntimeException("invalid revison: " + revision);
		
		this.target = target;
		this.changeType = changeType;
		this.revision = revision;
	}
	
	/**
	 * @return the {@link XID} of the {@link XRepository} holding the entity
	 *         this command will change (may be null)
	 */
	public XID getRepositoryID() {
		return this.target.getRepository();
	}
	
	/**
	 * @return the {@link XID} of the {@link XModel} holding the entity this
	 *         command will change (may be null)
	 */
	public XID getModelID() {
		return this.target.getModel();
	}
	
	public XID getFieldID() {
		return this.target.getField();
	}
	
	/**
	 * @return the {@link XID} of the {@link XObject} holding the entity this
	 *         command will change (may be null)
	 */
	public XID getObjectID() {
		return this.target.getObject();
	}
	
	public long getRevisionNumber() {
		return this.revision;
	}
	
	public ChangeType getChangeType() {
		return this.changeType;
	}
	
	public XAddress getTarget() {
		return this.target;
	}
	
	public boolean isForced() {
		return this.revision == XCommand.FORCED;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if(object == null)
			return false;
		
		if(!(object instanceof XAtomicCommand))
			return false;
		XAtomicCommand command = (XAtomicCommand)object;
		
		return this.revision == command.getRevisionNumber()
		        && this.changeType == command.getChangeType()
		        && this.target.equals(command.getTarget());
	}
	
	@Override
	public int hashCode() {
		
		int result = 0;
		
		// changeType is never null
		result ^= this.changeType.hashCode();
		
		// revision
		result ^= this.revision;
		
		// target
		result ^= this.target.hashCode();
		
		return result;
	}
	
}
