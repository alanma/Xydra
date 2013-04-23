package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.X;
import org.xydra.store.XydraStore;


/**
 * Extends a {@link XReadableRepository} with write operations. Write operations
 * are immediately translated to commands against the underlying
 * {@link XydraStore} and executed. Then the implementation blocks until the
 * store asynchronously responds.
 * 
 * This class is mostly useful for testing purposes. For production use it has
 * not been tested enough and performance is expected to be rather bad.
 * 
 * @author voelkel
 */
@RunsInGWT(false)
public class WritableRepositoryOnStore extends ReadableRepositoryOnStore implements Serializable,
        XWritableRepository {
    
    private static final long serialVersionUID = -6112519567015753881L;
    
    /**
     * @param credentials The credentials used for accessing the store.
     * @param store The sore to load from and persist changes to.
     */
    public WritableRepositoryOnStore(Credentials credentials, XydraStore store) {
        super(credentials, store);
    }
    
    @Override
    public XWritableModel createModel(XId modelId) {
        XCommand command = X.getCommandFactory().createAddModelCommand(this.getId(), modelId, true);
        long result = ExecuteCommandsUtils.executeCommand(this.credentials, this.store, command);
        if(result >= 0) {
            this.modelIds = null;
            return this.getModel(modelId);
        } else {
            // something went wrong
            if(result == XCommand.FAILED) {
                throw new RuntimeException("Command failed");
            } else if(result == XCommand.NOCHANGE) {
                return this.getModel(modelId);
            } else {
                throw new AssertionError("Cannot happen");
            }
        }
    }
    
    @Override
    public XWritableModel getModel(XId modelId) {
        XReadableModel baseModel = super.getModel(modelId);
        if(baseModel == null) {
            return null;
        }
        WritableModelOnStore revWritableModel = new WritableModelOnStore(this.credentials,
                this.store, baseModel.getAddress());
        for(XId objectId : baseModel) {
            XReadableObject baseObject = baseModel.getObject(objectId);
            XWritableObject revWritableObject = revWritableModel.getObject(objectId);
            for(XId fieldId : revWritableObject) {
                XWritableField revWritableField = revWritableObject.createField(fieldId);
                revWritableField.setValue(baseObject.getField(fieldId).getValue());
            }
        }
        return revWritableModel;
    }
    
    @Override
    public boolean removeModel(XId modelId) {
        XCommand command = X.getCommandFactory().createRemoveModelCommand(this.getId(), modelId,
                this.getModel(modelId).getRevisionNumber(), true);
        long result = ExecuteCommandsUtils.executeCommand(this.credentials, this.store, command);
        if(result >= 0) {
            this.modelIds = null;
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
    
}