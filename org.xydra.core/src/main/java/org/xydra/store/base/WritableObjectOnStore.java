package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.X;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XReadableObject} snapshot.
 * 
 * This class is mostly useful for testing purposes. For production use it has
 * not been tested enough and performance is expected to be rather bad.
 * 
 * @author voelkel
 */
@RunsInGWT(false)
public class WritableObjectOnStore extends ReadableObjectOnStore implements XWritableObject,
        Serializable {
    
    private static final long serialVersionUID = -6112519567015753881L;
    
    public WritableObjectOnStore(Credentials credentials, XydraStore store, XAddress address) {
        super(credentials, store, address);
    }
    
    @Override
    public XWritableField createField(XId fieldId) {
        XCommand command = X.getCommandFactory().createAddFieldCommand(
                this.address.getRepository(), this.address.getModel(), this.address.getObject(),
                fieldId, true);
        executeCommand(command);
        return this.getField(fieldId);
    }
    
    private boolean executeCommand(XCommand command) throws AssertionError {
        long result = ExecuteCommandsUtils.executeCommand(this.credentials, this.store, command);
        if(result >= 0) {
            load();
            return true;
        } else {
            // something went wrong
            if(result == XCommand.FAILED) {
                throw new RuntimeException("Command failed");
            } else if(result == XCommand.NOCHANGE) {
                return false;
            } else {
                throw new AssertionError("Cannot happen");
            }
        }
    }
    
    @Override
    public XWritableField getField(XId fieldId) {
        // this returns different instances for each call
        XReadableField baseField = super.getField(fieldId);
        if(baseField == null) {
            return null;
        }
        WritableFieldOnStore revWritableField = new WritableFieldOnStore(this.credentials,
                this.store, baseField.getAddress());
        revWritableField.setValue(baseField.getValue());
        return revWritableField;
    }
    
    @Override
    public boolean removeField(XId fieldId) {
        XCommand command = X.getCommandFactory().createRemoveFieldCommand(
                this.address.getRepository(), this.address.getModel(), this.address.getObject(),
                fieldId, this.getField(fieldId).getRevisionNumber(), true);
        return executeCommand(command);
    }
    
}
