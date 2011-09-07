package org.xydra.store.access.impl;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthorisationManager;


public abstract class AbstractAuthorisationManager implements XAuthorisationManager {
	
	private static final long serialVersionUID = 8774282865481424604L;
	
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
	
	@Override
    public boolean canExecute(XID actor, XCommand command) {
		if(command instanceof XAtomicCommand) {
			return canExecute(actor, (XAtomicCommand)command);
		} else if(command instanceof XTransaction) {
			return canExecute(actor, (XTransaction)command);
		}
		throw new IllegalArgumentException("unknown non-atomic command class: " + command);
	}
	
	public boolean canExecute(XID actor, XFieldCommand command) {
		return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canExecute(XID actor, XModelCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveObject(actor, command.getTarget(), command.getObjectId());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XID actor, XObjectCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveField(actor, command.getTarget(), command.getFieldId());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XID actor, XRepositoryCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveModel(actor, command.getTarget(), command.getModelId());
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
	
	@Override
    public boolean canKnowAboutField(XID actor, XAddress objectAddr, XID fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return hasAccess(actor, objectAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccess(actor, fieldAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccess(actor, fieldAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canKnowAboutModel(XID actor, XAddress repoAddr, XID modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		return hasAccess(actor, repoAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, modelAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, modelAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canKnowAboutObject(XID actor, XAddress modelAddr, XID objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return hasAccess(actor, modelAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, objectAddr, XA.ACCESS_READ).isAllowed()
		        || hasAccessToSubresource(actor, objectAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canRead(XID actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_READ).isAllowed();
	}
	
	@Override
    public boolean canRemoveField(XID actor, XAddress objectAddr, XID fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return hasAccess(actor, fieldAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, objectAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canRemoveModel(XID actor, XAddress repoAddr, XID modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		return hasAccessToSubtree(actor, modelAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, repoAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canRemoveObject(XID actor, XAddress modelAddr, XID objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return hasAccessToSubtree(actor, objectAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, modelAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
    public boolean canWrite(XID actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_WRITE).isAllowed();
	}
	
}
