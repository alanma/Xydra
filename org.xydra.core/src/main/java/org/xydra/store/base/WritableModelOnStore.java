package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XReadableModel} snapshot.
 *
 * This class is mostly useful for testing purposes. For production use it has
 * not been tested enough and performance is expected to be rather bad.
 *
 * @author xamde
 */
@RunsInGWT(false)
public class WritableModelOnStore extends ReadableModelOnStore implements XWritableModel,
        Serializable {

    private static final long serialVersionUID = -6112519567015753881L;

    public WritableModelOnStore(final Credentials credentials, final XydraStore store, final XAddress address) {
        super(credentials, store, address);
    }

    @Override
    public XWritableObject createObject(@NeverNull final XId objectId) {
        final XCommand command = BaseRuntime.getCommandFactory().createAddObjectCommand(this.address, objectId,
                true);
        executeCommand(command);
        return getObject(objectId);
    }

    private boolean executeCommand(final XCommand command) {
        final long result = ExecuteCommandsUtils.executeCommand(this.credentials, this.store, command);
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
    public XWritableObject getObject(@NeverNull final XId objectId) {
        final XReadableObject baseObject = super.getObject(objectId);

        if(baseObject == null) {
            return null;
        }

        final WritableObjectOnStore revWritableObject = new WritableObjectOnStore(this.credentials,
                this.store, baseObject.getAddress());
        for(final XId fieldId : baseObject) {
            final XWritableField writabelField = revWritableObject.createField(fieldId);
            writabelField.setValue(baseObject.getField(fieldId).getValue());
        }
        return revWritableObject;
    }

    @Override
    public boolean removeObject(@NeverNull final XId objectId) {
        final XCommand command = BaseRuntime.getCommandFactory().createRemoveObjectCommand(
                this.address.getRepository(), this.address.getModel(), objectId,
                getObject(objectId).getRevisionNumber(), true);
        return executeCommand(command);
    }

}
