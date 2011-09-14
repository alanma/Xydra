package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
 * @author voelkel
 */
@Deprecated
@RunsInGWT(false)
public class WritableModelOnStore extends ReadableModelOnStore implements XWritableModel,
        Serializable {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public WritableModelOnStore(Credentials credentials, XydraStore store, XAddress address) {
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
	@SuppressWarnings("deprecation")
	public XWritableObject getObject(XID objectId) {
		XReadableObject baseObject = super.getObject(objectId);
		
		if(baseObject == null) {
			return null;
		}
		
		WritableObjectOnStore revWritableObject = new WritableObjectOnStore(this.credentials,
		        this.store, baseObject.getAddress());
		for(XID fieldId : baseObject) {
			XWritableField writabelField = revWritableObject.createField(fieldId);
			writabelField.setValue(baseObject.getField(fieldId).getValue());
		}
		return revWritableObject;
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		XCommand command = X.getCommandFactory().createRemoveObjectCommand(
		        this.address.getRepository(), this.address.getModel(), objectId,
		        this.getObject(objectId).getRevisionNumber(), true);
		return executeCommand(command);
	}
	
}
