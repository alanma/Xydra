package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XReadableModel} snapshot.
 * 
 * @author voelkel
 */
public class HalfWritableModelOnStore extends ReadableModelOnStore implements XHalfWritableModel, Serializable {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public HalfWritableModelOnStore(Credentials credentials, XydraStore store, XAddress address) {
		super(credentials, store, address);
	}
	
	@Override
	public XHalfWritableObject createObject(XID objectId) {
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
	public boolean removeObject(XID objectId) {
		XCommand command = X.getCommandFactory().createRemoveObjectCommand(
		        this.address.getRepository(), this.address.getModel(), objectId,
		        this.getObject(objectId).getRevisionNumber(), true);
		return executeCommand(command);
	}
	
	@Override
	public XHalfWritableObject getObject(XID objectId) {
		XReadableObject baseObject = super.getObject(objectId);
		
		if(baseObject == null) {
			return null;
		}
		
		HalfWritableObjectOnStore writableObject = new HalfWritableObjectOnStore(this.credentials, this.store, baseObject
		        .getAddress());
		for(XID fieldId : baseObject) {
			XHalfWritableField writabelField = writableObject.createField(fieldId);
			writabelField.setValue(baseObject.getField(fieldId).getValue());
		}
		return writableObject;
	}
	
}
