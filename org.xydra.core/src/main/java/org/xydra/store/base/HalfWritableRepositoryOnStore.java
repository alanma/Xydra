package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XReadableRepository;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.XydraStore;


/**
 * Extends a {@link XReadableRepository} with write operations. Write operations are
 * immediately translated to commands against the underlying {@link XydraStore}
 * and executed. Then the implementation blocks until the store asynchronously
 * responds.
 * 
 * @author voelkel
 */
public class HalfWritableRepositoryOnStore extends ReadableRepositoryOnStore implements Serializable, XHalfWritableRepository {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	/**
	 * @param credentials
	 * @param store
	 */
	public HalfWritableRepositoryOnStore(Credentials credentials, XydraStore store) {
		super(credentials, store);
	}
	
	@Override
	public XHalfWritableModel createModel(XID modelId) {
		XCommand command = X.getCommandFactory().createAddModelCommand(this.getID(), modelId, true);
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
	public boolean removeModel(XID modelId) {
		XCommand command = X.getCommandFactory().createRemoveModelCommand(this.getID(), modelId,
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
	
	@Override
	public XHalfWritableModel getModel(XID modelId) {
		XReadableModel baseModel = super.getModel(modelId);
		if(baseModel == null) {
			return null;
		}
		HalfWritableModelOnStore writableModel = new HalfWritableModelOnStore(this.credentials, this.store,
		        baseModel.getAddress());
		for(XID objectId : baseModel) {
			XReadableObject baseObject = baseModel.getObject(objectId);
			XHalfWritableObject writableObject = writableModel.getObject(objectId);
			for(XID fieldId : writableObject) {
				XHalfWritableField writableField = writableObject.createField(fieldId);
				writableField.setValue(baseObject.getField(fieldId).getValue());
			}
		}
		return writableModel;
	}
	
}
