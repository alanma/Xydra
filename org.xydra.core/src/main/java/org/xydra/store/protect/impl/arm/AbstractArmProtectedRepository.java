package org.xydra.store.protect.impl.arm;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.core.AccessException;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.protect.XProtectedModel;
import org.xydra.store.protect.XProtectedRepository;



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

	public AbstractArmProtectedRepository(final XRepository repo, final XAuthorisationManager arm, final XId actor) {
		this.repo = repo;
		this.arm = arm;
		this.actor = actor;

		XyAssert.xyAssert(repo != null); assert repo != null;
		XyAssert.xyAssert(arm != null); assert arm != null;
	}

	@Override
    public boolean addListenerForFieldEvents(final XFieldEventListener changeListener) {

		checkReadAccess();

		return this.repo.addListenerForFieldEvents(changeListener);
	}

	@Override
    public boolean addListenerForModelEvents(final XModelEventListener changeListener) {

		checkReadAccess();

		return this.repo.addListenerForModelEvents(changeListener);
	}

	@Override
    public boolean addListenerForObjectEvents(final XObjectEventListener changeListener) {

		checkReadAccess();

		return this.repo.addListenerForObjectEvents(changeListener);
	}

	@Override
    public boolean addListenerForRepositoryEvents(final XRepositoryEventListener changeListener) {

		checkReadAccess();

		return this.repo.addListenerForRepositoryEvents(changeListener);
	}

	@Override
    public boolean addListenerForTransactionEvents(final XTransactionEventListener changeListener) {

		checkReadAccess();

		return this.repo.addListenerForTransactionEvents(changeListener);
	}

	private void checkReadAccess() throws AccessException {
		if(!this.arm.canRead(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot read " + getAddress());
		}
	}

	@Override
    public XProtectedModel createModel(final XId modelId) {

		if(!this.arm.canWrite(this.actor, getAddress())) {
			throw new AccessException(this.actor + " cannot write to " + getAddress());
		}

		final XModel model = this.repo.createModel(modelId);

		XyAssert.xyAssert(model != null); assert model != null;

		return new ArmProtectedModel(model, getArmForModel(modelId), this.actor);
	}

	@Override
    public long executeCommand(final XCommand command) {

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
    public long executeRepositoryCommand(final XRepositoryCommand command) {

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
    public XProtectedModel getModel(final XId modelId) {

		final XAuthorisationManager modelArm = getArmForModel(modelId);

		if(!modelArm.canKnowAboutModel(this.actor, getAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot read modelId " + modelId + " in "
			        + getAddress());
		}

		final XModel model = this.repo.getModel(modelId);

		if(model == null) {
			return null;
		}

		return new ArmProtectedModel(model, modelArm, this.actor);
	}

	@Override
    public boolean hasModel(final XId modelId) {

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
    public boolean removeListenerForFieldEvents(final XFieldEventListener changeListener) {
		return this.repo.removeListenerForFieldEvents(changeListener);
	}

	@Override
    public boolean removeListenerForModelEvents(final XModelEventListener changeListener) {
		return this.repo.removeListenerForModelEvents(changeListener);
	}

	@Override
    public boolean removeListenerForObjectEvents(final XObjectEventListener changeListener) {
		return this.repo.removeListenerForObjectEvents(changeListener);
	}

	@Override
    public boolean removeListenerForRepositoryEvents(final XRepositoryEventListener changeListener) {
		return this.repo.removeListenerForRepositoryEvents(changeListener);
	}

	@Override
    public boolean removeListenerForTransactionEvents(final XTransactionEventListener changeListener) {
		return this.repo.removeListenerForTransactionEvents(changeListener);
	}

	@Override
    public boolean removeModel(final XId modelId) {

		if(!getArmForModel(modelId).canRemoveModel(this.actor, getAddress(), modelId)) {
			throw new AccessException(this.actor + " cannot remove " + modelId + " from "
			        + getAddress());
		}

		return this.repo.removeModel(modelId);
	}

}
