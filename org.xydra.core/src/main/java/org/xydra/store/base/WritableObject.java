package org.xydra.store.base;

import java.io.Serializable;

import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.XWritableField;
import org.xydra.core.model.XWritableObject;
import org.xydra.store.XydraStore;


/**
 * Uses an internal {@link XBaseObject} snapshot.
 * 
 * @author voelkel
 */
public class WritableObject extends BaseObject implements XWritableObject, Serializable {
	
	private static final long serialVersionUID = -6112519567015753881L;
	
	public WritableObject(Credentials credentials, XydraStore store, XAddress address) {
		super(credentials, store, address);
	}
	
	@Override
	public XWritableField createField(XID fieldId) {
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
	public XWritableField getField(XID fieldId) {
		// FIXME this returns different instances for each call
		XBaseField baseField = super.getField(fieldId);
		if(baseField == null) {
			return null;
		}
		WritableField writableField = new WritableField(this.credentials, this.store,
		        baseField.getAddress());
		writableField.setValue(baseField.getValue());
		return writableField;
	}
	
}
