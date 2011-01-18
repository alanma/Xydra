package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableField;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XReadableObject} snapshot.
 * 
 * @author voelkel
 */
public class HalfWritableObjectOnStore extends ReadableObjectOnStore implements XHalfWritableObject, Serializable {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public HalfWritableObjectOnStore(Credentials credentials, XydraStore store, XAddress address) {
		super(credentials, store, address);
	}
	
	@Override
	public XHalfWritableField createField(XID fieldId) {
		XCommand command = X.getCommandFactory().createAddFieldCommand(
		        this.address.getRepository(), this.address.getModel(), this.address.getObject(),
		        fieldId, true);
		executeCommand(command);
		return this.getField(fieldId);
	}
	
	@Override
	public boolean removeField(XID fieldId) {
		XCommand command = X.getCommandFactory().createRemoveFieldCommand(
		        this.address.getRepository(), this.address.getModel(), this.address.getObject(),
		        fieldId, this.getField(fieldId).getRevisionNumber(), true);
		return executeCommand(command);
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
	public XHalfWritableField getField(XID fieldId) {
		// FIXME this returns different instances for each call
		XReadableField baseField = super.getField(fieldId);
		if(baseField == null) {
			return null;
		}
		HalfWritableFieldOnStore writableField = new HalfWritableFieldOnStore(this.credentials, this.store,
		        baseField.getAddress());
		writableField.setValue(baseField.getValue());
		return writableField;
	}
	
}
