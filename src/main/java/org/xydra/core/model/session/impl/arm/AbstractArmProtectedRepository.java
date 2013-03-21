package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An abstract {@link XProtectedRepository} that wraps an {@link XRepository}
 * for a specific actor and checks all access against an
 * {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
abstract public class AbstractArmProtectedRepository implements XProtectedRepository {
	
	private final XId actor;
	private final XAuthorisationManager arm;
	private final XRepository repo;
	
	public AbstractArmProtectedRepository(XRepository repo, XAuthorisationManager arm, XId actor) {
		this.repo = repo;
		this.arm = arm;
		this.actor = actor;
		
		XyAssert.xyAssert(repo != null); assert repo != null;
		XyAssert.xyAssert(arm != null); assert arm != null;
	}
	
	@Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForFieldEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForModelEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForObjectEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForRepositoryEvents(changeListener);
	}
	
	@Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForTransactionEvents(changeListener);
	}
	
	private void checkReadAccess() throws AccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	@Override
    public XProtectedModel createModel(XId modelId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XModel model = this.repo.createModel(modelId);
		
		XyAssert.xyAssert(model != null); assert model != null;
		
		return new ArmProtectedModel(model, getArmForModel(modelId), this.actor);
	}
	
	@Override
    public long executeCommand(XCommand command) {
		
		if(command instanceof XRepositoryCommand) {
			return executeRepositoryCommand((XRepositoryCommand)command);
		}
		
		XyAssert.xyAssert(command.getTarget().getModel() != null); assert command.getTarget().getModel() != null;
		
		if(!getArmForModel(command.getTarget().getModel()).canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.repo.executeCommand(command);
	}
	
	@Override
    public long executeRepositoryCommand(XRepositoryCommand command) {
		
		if(!getArmForModel(command.getModelId()).canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.repo.executeRepositoryCommand(command);
	}
	
	@Override
    public XId getActor() {
		return this.actor;
	}
	
	@Override
    public XAddress getAddress() {
		return this.repo.getAddress();
	}
	
	protected XAuthorisationManager getArm() {
		return this.arm;
	}
	
	abstract protected XAuthorisationManager getArmForModel(XId modelId);
	
	@Override
    public XId getId() {
		return this.repo.getId();
	}
	
	@Override
    public XProtectedModel getModel(XId modelId) {
		
		XAuthorisationManager modelArm = getArmForModel(modelId);
		
		if(!modelArm.canKnowAboutModel(this.actor, getAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getAddress());
		}
		
		XModel model = this.repo.getModel(modelId);
		
		if(model == null) {
			return null;
		}
		
		return new ArmProtectedModel(model, modelArm, this.actor);
	}
	
	@Override
    public boolean hasModel(XId modelId) {
		
		checkReadAccess();
		
		return this.repo.hasModel(modelId);
	}
	
	@Override
    public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.repo.isEmpty();
	}
	
	@Override
    public Iterator<XId> iterator() {
		
		checkReadAccess();
		
		return this.repo.iterator();
	}
	
	@Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.repo.removeListenerForFieldEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.repo.removeListenerForModelEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.repo.removeListenerForObjectEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		return this.repo.removeListenerForRepositoryEvents(changeListener);
	}
	
	@Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.repo.removeListenerForTransactionEvents(changeListener);
	}
	
	@Override
    public boolean removeModel(XId modelId) {
		
		if(!getArmForModel(modelId).canRemoveModel(this.actor, getAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot remove " + modelId + " from "
			        + getAddress());
		}
		
		return this.repo.removeModel(modelId);
	}
	
}
