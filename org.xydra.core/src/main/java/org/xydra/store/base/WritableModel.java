package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableModel;
import org.xydra.core.model.XWritableObject;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XBaseModel} snapshot.
 * 
 * @author voelkel
 */
public class WritableModel extends BaseModel implements XWritableModel, Serializable {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public WritableModel(Credentials credentials, XydraStore store, XAddress address) {
		super(credentials, store, address);
	}
	
	@Override
	public XWritableObject createObject(XID objectId) {
		XCommand command = X.getCommandFactory().createAddObjectCommand(
		        this.address.getRepository(), this.address.getModel(), objectId, true);
		executeCommand(command);
		return this.getObject(objectId);
	}
	
	private boolean executeCommand(XCommand command) {
		long result = WritableUtils.executeCommand(this.credentials, this.store, command);
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
	public boolean removeObject(XID objectId) {
		XCommand command = X.getCommandFactory().createRemoveObjectCommand(
		        this.address.getRepository(), this.address.getModel(), objectId,
		        this.getObject(objectId).getRevisionNumber(), true);
		return executeCommand(command);
	}
	
	@Override
	public XWritableObject getObject(XID objectId) {
		XBaseObject baseObject = super.getObject(objectId);
		
		if(baseObject == null) {
			return null;
		}
		
		WritableObject writableObject = new WritableObject(this.credentials, this.store, baseObject
		        .getAddress());
		for(XID fieldId : baseObject) {
			XWritableField writabelField = writableObject.createField(fieldId);
			writabelField.setValue(baseObject.getField(fieldId).getValue());
		}
		return writableObject;
	}
	
}
