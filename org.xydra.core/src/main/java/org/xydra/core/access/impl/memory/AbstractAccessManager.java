package org.xydra.core.access.impl.memory;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.store.access.XA;


public abstract class AbstractAccessManager implements XAccessManager {
	
	private static final long serialVersionUID = 8774282865481424604L;
	
	public boolean canExecute(XID actor, XCommand command) {
		if(command instanceof XAtomicCommand) {
			return canExecute(actor, (XAtomicCommand)command);
		} else if(command instanceof XTransaction) {
			return canExecute(actor, (XTransaction)command);
		}
		throw new IllegalArgumentException("unknown non-atomic command class: " + command);
	}
	
	public boolean canExecute(XID actor, XAtomicCommand command) {
		if(command instanceof XFieldCommand) {
			return canExecute(actor, (XFieldCommand)command);
		} else if(command instanceof XObjectCommand) {
			return canExecute(actor, (XObjectCommand)command);
		} else if(command instanceof XModelCommand) {
			return canExecute(actor, (XModelCommand)command);
		} else if(command instanceof XRepositoryCommand) {
			return canExecute(actor, (XRepositoryCommand)command);
		}
		throw new IllegalArgumentException("unknown atomic command class: " + command);
	}
	
	public boolean canExecute(XID actor, XFieldCommand command) {
		return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canExecute(XID actor, XObjectCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveField(actor, command.getTarget(), command.getFieldID());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XID actor, XModelCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveObject(actor, command.getTarget(), command.getObjectID());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XID actor, XRepositoryCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveModel(actor, command.getTarget(), command.getModelID());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XID actor, XTransaction trans) {
		
		for(XAtomicCommand command : trans) {
			if(!canExecute(actor, command)) {
				return false;
			}
		}
		
		// all commands are allowed
		return true;
	}
	
	public boolean canRemoveModel(XID actor, XAddress repoAddr, XID modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		return hasAccessToSubtree(actor, modelAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, repoAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canRemoveObject(XID actor, XAddress modelAddr, XID objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return hasAccessToSubtree(actor, objectAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, modelAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canRemoveField(XID actor, XAddress objectAddr, XID fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return hasAccess(actor, fieldAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, objectAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canRead(XID actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_READ).isAllowed();
	}
	
	public boolean canWrite(XID actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canKnowAboutField(XID actor, XAddress objectAddr, XID fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return hasAccess(actor, objectAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccess(actor, fieldAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccess(actor, fieldAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canKnowAboutObject(XID actor, XAddress modelAddr, XID objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return hasAccess(actor, modelAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, objectAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, objectAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canKnowAboutModel(XID actor, XAddress repoAddr, XID modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		return hasAccess(actor, repoAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, modelAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, modelAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
}
