package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XBaseRepository;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.core.model.XWritableRepository;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XBaseModel} snapshot.
 * 
 * @author voelkel
 */
public class WritableRepository extends BaseRepository implements XWritableRepository, Serializable {
	
	protected XBaseRepository writableRepository;
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public WritableRepository(Credentials credentials, XydraStore store, XAddress address) {
		super(credentials, store, address);
	}
	
	@Override
	public XWritableModel createModel(XID modelId) {
		XCommand command = X.getCommandFactory().createAddModelCommand(this.getID(), modelId, true);
		long result = WritableUtils.executeCommand(this.credentials, this.store, command);
		if(result >= 0) {
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
		long result = WritableUtils.executeCommand(this.credentials, this.store, command);
		if(result >= 0) {
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
	public XWritableModel getModel(XID modelId) {
		XBaseModel baseModel = super.getModel(modelId);
		WritableModel writableModel = new WritableModel(this.credentials, this.store, baseModel
		        .getAddress());
		for(XID objectId : baseModel) {
			XBaseObject baseObject = baseModel.getObject(objectId);
			XWritableObject writableObject = writableModel.getObject(objectId);
			for(XID fieldId : writableObject) {
				XWritableField writableField = writableObject.createField(fieldId);
				writableField.setValue(baseObject.getField(fieldId).getValue());
			}
		}
		return writableModel;
	}
	
}
