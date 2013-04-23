package org.xydra.store.access.impl;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.core.XX;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthorisationManager;


public abstract class AbstractAuthorisationManager implements XAuthorisationManager {
	
	public static final long serialVersionUID = 8774282865481424604L;
	
	public boolean canExecute(XId actor, XAtomicCommand command) {
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
	public boolean canExecute(XId actor, XCommand command) {
		if(command instanceof XAtomicCommand) {
			return canExecute(actor, (XAtomicCommand)command);
		} else if(command instanceof XTransaction) {
			return canExecute(actor, (XTransaction)command);
		}
		throw new IllegalArgumentException("unknown non-atomic command class: " + command);
	}
	
	public boolean canExecute(XId actor, XFieldCommand command) {
		return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
	}
	
	public boolean canExecute(XId actor, XModelCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveObject(actor, command.getTarget(), command.getObjectId());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XId actor, XObjectCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveField(actor, command.getTarget(), command.getFieldId());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XId actor, XRepositoryCommand command) {
		switch(command.getChangeType()) {
		case ADD:
			return hasAccess(actor, command.getTarget(), XA.ACCESS_WRITE).isAllowed();
		case REMOVE:
			return canRemoveModel(actor, command.getTarget(), command.getModelId());
		default:
			throw new AssertionError("unexpected command type" + command);
		}
	}
	
	public boolean canExecute(XId actor, XTransaction trans) {
		
		for(XAtomicCommand command : trans) {
			if(!canExecute(actor, command)) {
				return false;
			}
		}
		
		// all commands are allowed
		return true;
	}
	
	@Override
	public boolean canKnowAboutField(XId actor, XAddress objectAddr, XId fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return !isInternal(fieldId)
		        && (hasAccess(actor, objectAddr, XA.ACCESS_READ).isAllowed()
		                || hasAccess(actor, fieldAddr, XA.ACCESS_READ).isAllowed() || hasAccess(
		                    actor, fieldAddr, XA.ACCESS_WRITE).isAllowed());
	}
	
	@Override
	public boolean canKnowAboutModel(XId actor, XAddress repoAddr, XId modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		boolean result = !isInternal(modelId)
		        && (hasAccess(actor, repoAddr, XA.ACCESS_READ).isAllowed()
		                || hasAccessToSubresource(actor, modelAddr, XA.ACCESS_READ).isAllowed() || hasAccessToSubresource(
		                    actor, modelAddr, XA.ACCESS_WRITE).isAllowed());
		return result;
	}
	
	private static boolean isInternal(XId id) {
		boolean result = id.toString().startsWith("internal--");
		return result;
	}
	
	@Override
	public boolean canKnowAboutObject(XId actor, XAddress modelAddr, XId objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return !isInternal(objectId)
		        && (hasAccess(actor, modelAddr, XA.ACCESS_READ).isAllowed()
		                || hasAccessToSubresource(actor, objectAddr, XA.ACCESS_READ).isAllowed() || hasAccessToSubresource(
		                    actor, objectAddr, XA.ACCESS_WRITE).isAllowed());
	}
	
	@Override
	public boolean canRead(XId actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_READ).isAllowed();
	}
	
	@Override
	public boolean canRemoveField(XId actor, XAddress objectAddr, XId fieldId) {
		XAddress fieldAddr = XX.resolveField(objectAddr, fieldId);
		return hasAccess(actor, fieldAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, objectAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
	public boolean canRemoveModel(XId actor, XAddress repoAddr, XId modelId) {
		XAddress modelAddr = XX.resolveModel(repoAddr, modelId);
		return hasAccessToSubtree(actor, modelAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, repoAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
	public boolean canRemoveObject(XId actor, XAddress modelAddr, XId objectId) {
		XAddress objectAddr = XX.resolveObject(modelAddr, objectId);
		return hasAccessToSubtree(actor, objectAddr, XA.ACCESS_WRITE).isAllowed()
		        && hasAccess(actor, modelAddr, XA.ACCESS_WRITE).isAllowed();
	}
	
	@Override
	public boolean canWrite(XId actor, XAddress resource) {
		return hasAccess(actor, resource, XA.ACCESS_WRITE).isAllowed();
	}
	
}