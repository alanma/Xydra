package org.xydra.core.model.session.impl.arm;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AccessException;
import org.xydra.store.access.XAuthorisationManager;


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
    
    public ArmProtectedModel(XModel model, XAuthorisationManager arm, XId actor) {
        super(model, arm, actor);
        this.model = model;
    }
    
    @Override
    public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
        
        checkReadAccess();
        
        return this.model.addListenerForFieldEvents(changeListener);
    }
    
    @Override
    public boolean addListenerForModelEvents(XModelEventListener changeListener) {
        
        checkReadAccess();
        
        return this.model.addListenerForModelEvents(changeListener);
    }
    
    @Override
    public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
        
        checkReadAccess();
        
        return this.model.addListenerForObjectEvents(changeListener);
    }
    
    @Override
    public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
        
        checkReadAccess();
        
        return this.model.addListenerForTransactionEvents(changeListener);
    }
    
    @Override
    public XProtectedObject createObject(@NeverNull XId objectId) {
        
        if(!this.arm.canWrite(this.actor, getAddress())) {
            throw new AccessException(this.actor + " cannot write to " + getAddress());
        }
        
        XObject object = this.model.createObject(objectId);
        
        XyAssert.xyAssert(object != null);
        assert object != null;
        
        return new ArmProtectedObject(object, this.arm, this.actor);
    }
    
    @Override
    public long executeCommand(XCommand command) {
        
        if(!this.arm.canExecute(this.actor, command)) {
            throw new AccessException(this.actor + " cannot execute " + command);
        }
        
        return this.model.executeCommand(command);
    }
    
    @Override
    public long executeModelCommand(XModelCommand command) {
        
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
    public XProtectedObject getObject(@NeverNull XId objectId) {
        
        checkCanKnowAboutObject(objectId);
        
        XObject object = this.model.getObject(objectId);
        
        if(object == null) {
            return null;
        }
        
        return new ArmProtectedObject(object, this.arm, this.actor);
    }
    
    @Override
    public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
        return this.model.removeListenerForFieldEvents(changeListener);
    }
    
    @Override
    public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
        return this.model.removeListenerForModelEvents(changeListener);
    }
    
    @Override
    public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
        return this.model.removeListenerForObjectEvents(changeListener);
    }
    
    @Override
    public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
        return this.model.removeListenerForTransactionEvents(changeListener);
    }
    
    @Override
    public boolean removeObject(@NeverNull XId objectId) {
        
        if(!this.arm.canRemoveObject(this.actor, getAddress(), objectId)) {
            throw new AccessException(this.actor + " cannot remove " + objectId + " from "
                    + getAddress());
        }
        
        return this.model.removeObject(objectId);
    }
    
}
