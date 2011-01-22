package org.xydra.core.model.session.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
	
	private final XID actor;
	private final XAuthorisationManager arm;
	private final XRepository repo;
	
	public AbstractArmProtectedRepository(XRepository repo, XAuthorisationManager arm, XID actor) {
		this.repo = repo;
		this.arm = arm;
		this.actor = actor;
		
		assert repo != null;
		assert arm != null;
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForFieldEvents(changeListener);
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForModelEvents(changeListener);
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForObjectEvents(changeListener);
	}
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForRepositoryEvents(changeListener);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		
		checkReadAccess();
		
		return this.repo.addListenerForTransactionEvents(changeListener);
	}
	
	private void checkReadAccess() throws AccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}
	
	public XProtectedModel createModel(XID modelId) {
		
		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}
		
		XModel model = this.repo.createModel(modelId);
		
		assert model != null;
		
		return new ArmProtectedModel(model, getArmForModel(modelId), this.actor);
	}
	
	public long executeCommand(XCommand command) {
		
		if(command instanceof XRepositoryCommand) {
			return executeRepositoryCommand((XRepositoryCommand)command);
		}
		
		assert command.getTarget().getModel() != null;
		
		if(!getArmForModel(command.getTarget().getModel()).canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.repo.executeCommand(command);
	}
	
	public long executeRepositoryCommand(XRepositoryCommand command) {
		
		if(!getArmForModel(command.getModelId()).canExecute(this.actor, command)) {
			throw new AccessException(this.actor + " cannot execute " + command);
		}
		
		return this.repo.executeRepositoryCommand(command);
	}
	
	public XID getActor() {
		return this.actor;
	}
	
	public XAddress getAddress() {
		return this.repo.getAddress();
	}
	
	protected XAuthorisationManager getArm() {
		return this.arm;
	}
	
	abstract protected XAuthorisationManager getArmForModel(XID modelId);
	
	public XID getID() {
		return this.repo.getID();
	}
	
	public XProtectedModel getModel(XID modelId) {
		
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
	
	public boolean hasModel(XID modelId) {
		
		checkReadAccess();
		
		return this.repo.hasModel(modelId);
	}
	
	public boolean isEmpty() {
		
		checkReadAccess();
		
		return this.repo.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		
		checkReadAccess();
		
		return this.repo.iterator();
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.repo.removeListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.repo.removeListenerForModelEvents(changeListener);
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.repo.removeListenerForObjectEvents(changeListener);
	}
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		return this.repo.removeListenerForRepositoryEvents(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.repo.removeListenerForTransactionEvents(changeListener);
	}
	
	public boolean removeModel(XID modelId) {
		
		if(!getArmForModel(modelId).canRemoveModel(this.actor, getAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot remove " + modelId + " from "
			        + getAddress());
		}
		
		return this.repo.removeModel(modelId);
	}
	
}
