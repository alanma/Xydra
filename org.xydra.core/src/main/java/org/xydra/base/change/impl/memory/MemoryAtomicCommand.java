package org.xydra.base.change.impl.memory;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


abstract public class MemoryAtomicCommand implements XAtomicCommand, Serializable {
	
	private static final long serialVersionUID = -4547419646736034654L;
	private final ChangeType changeType;
	private final long revision;
	private XAddress target;
	
	protected MemoryAtomicCommand(XAddress target, ChangeType changeType, long revision) {
		
		if(target == null)
			throw new NullPointerException("target must not be null");
		
		if(revision < 0 && revision != XCommand.SAFE && revision != XCommand.FORCED)
			throw new RuntimeException("invalid revison: " + revision);
		
		this.target = target;
		this.changeType = changeType;
		this.revision = revision;
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
	
	public ChangeType getChangeType() {
		return this.changeType;
	}
	
	public XID getFieldId() {
		return this.target.getField();
	}
	
	/**
	 * @return the {@link XID} of the {@link XModel} holding the entity this
	 *         command will change (may be null)
	 */
	public XID getModelId() {
		return this.target.getModel();
	}
	
	/**
	 * @return the {@link XID} of the {@link XObject} holding the entity this
	 *         command will change (may be null)
	 */
	public XID getObjectId() {
		return this.target.getObject();
	}
	
	/**
	 * @return the {@link XID} of the {@link XRepository} holding the entity
	 *         this command will change (may be null)
	 */
	public XID getRepositoryId() {
		return this.target.getRepository();
	}
	
	public long getRevisionNumber() {
		return this.revision;
	}
	
	public XAddress getTarget() {
		return this.target;
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
	
	public boolean isForced() {
		return this.revision == XCommand.FORCED;
	}
	
}
