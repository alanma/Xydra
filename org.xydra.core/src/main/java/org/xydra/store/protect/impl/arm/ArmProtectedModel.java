package org.xydra.store.protect.impl.arm;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.core.AccessException;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.protect.XProtectedModel;
import org.xydra.store.protect.XProtectedObject;



/**
 * An {@link XProtectedModel} that wraps an {@link XModel} for a specific actor
 * and checks all access against an {@link XAuthorisationManager}.
 *
 * @author dscharrer
 *
 */
public class ArmProtectedModel extends ArmProtectedBaseModel implements XProtectedModel,
        IHasChangeLog {

    private final XModel model;

    public ArmProtectedModel(final XModel model, final XAuthorisationManager arm, final XId actor) {
        super(model, arm, actor);
        this.model = model;
    }

    @Override
    public boolean addListenerForFieldEvents(final XFieldEventListener changeListener) {

        checkReadAccess();

        return this.model.addListenerForFieldEvents(changeListener);
    }

    @Override
    public boolean addListenerForModelEvents(final XModelEventListener changeListener) {

        checkReadAccess();

        return this.model.addListenerForModelEvents(changeListener);
    }

    @Override
    public boolean addListenerForObjectEvents(final XObjectEventListener changeListener) {

        checkReadAccess();

        return this.model.addListenerForObjectEvents(changeListener);
    }

    @Override
    public boolean addListenerForTransactionEvents(final XTransactionEventListener changeListener) {

        checkReadAccess();

        return this.model.addListenerForTransactionEvents(changeListener);
    }

    @Override
    public XProtectedObject createObject(@NeverNull final XId objectId) {

        if(!this.arm.canWrite(this.actor, getAddress())) {
            throw new AccessException(this.actor + " cannot write to " + getAddress());
        }

        final XObject object = this.model.createObject(objectId);

        XyAssert.xyAssert(object != null);
        assert object != null;

        return new ArmProtectedObject(object, this.arm, this.actor);
    }

    @Override
    public long executeCommand(final XCommand command) {

        if(!this.arm.canExecute(this.actor, command)) {
            throw new AccessException(this.actor + " cannot execute " + command);
        }

        return this.model.executeCommand(command);
    }

    @Override
    public long executeModelCommand(final XModelCommand command) {

        if(!this.arm.canExecute(this.actor, command)) {
            throw new AccessException(this.actor + " cannot execute " + command);
        }

        return this.model.executeModelCommand(command);
    }

    @Override
    public XChangeLog getChangeLog() {
        return new ArmProtectedChangeLog(this.model.getChangeLog(), this.arm, this.actor);
    }

    @Override
    public XProtectedObject getObject(@NeverNull final XId objectId) {

        checkCanKnowAboutObject(objectId);

        final XObject object = this.model.getObject(objectId);

        if(object == null) {
            return null;
        }

        return new ArmProtectedObject(object, this.arm, this.actor);
    }

    @Override
    public boolean removeListenerForFieldEvents(final XFieldEventListener changeListener) {
        return this.model.removeListenerForFieldEvents(changeListener);
    }

    @Override
    public boolean removeListenerForModelEvents(final XModelEventListener changeListener) {
        return this.model.removeListenerForModelEvents(changeListener);
    }

    @Override
    public boolean removeListenerForObjectEvents(final XObjectEventListener changeListener) {
        return this.model.removeListenerForObjectEvents(changeListener);
    }

    @Override
    public boolean removeListenerForTransactionEvents(final XTransactionEventListener changeListener) {
        return this.model.removeListenerForTransactionEvents(changeListener);
    }

    @Override
    public boolean removeObject(@NeverNull final XId objectId) {

        if(!this.arm.canRemoveObject(this.actor, getAddress(), objectId)) {
            throw new AccessException(this.actor + " cannot remove " + objectId + " from "
                    + getAddress());
        }

        return this.model.removeObject(objectId);
    }

}
