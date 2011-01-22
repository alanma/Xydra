package org.xydra.store.access.impl.delegate;

import java.io.Serializable;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.WritableUtils;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.value.XV;
import org.xydra.store.access.HashUtils;
import org.xydra.store.access.XAuthenticationDatabase;


/**
 * Maps reads and writes to a {@link XWritableModel}.
 * 
 * <h4>Data modelling</h4>
 * 
 * Accounts (Passwords, failed login attempts)
 * 
 * <pre>
 * objectId | fieldId                  | value
 * ---------+--------------------------+----------------------------
 * actorId  | "hasPasswordHash"        | the password hash (see {@link HashUtils})
 * actorId  | "hasFailedLoginAttempts" | if present: number of failed login attempts
 * </pre>
 * 
 * @author voelkel
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class AuthenticationDatabaseOnWritableModel implements XAuthenticationDatabase, Serializable {
	
	public static final XID hasFailedLoginAttempts = XX.toId("hasFailedLoginAttempts");
	
	public static final XID hasPasswordHash = XX.toId("hasPasswordHash");
	private static final long serialVersionUID = 21938374542095483L;
	
	protected XWritableModel authenticationModel;
	
	/**
	 * @param authenticationModel used to read and write account management
	 *            data.
	 */
	public AuthenticationDatabaseOnWritableModel(XWritableModel authenticationModel) {
		this.authenticationModel = authenticationModel;
	}
	
	@Override
	public void clear() {
		// delete all objects in account model
		WritableUtils.deleteAllObjects(this.authenticationModel);
	}
	
	@Override
	public int getFailedLoginAttempts(XID actorId) {
		assert actorId != null;
		XValue value = WritableUtils.getValue(this.authenticationModel, actorId,
		        hasFailedLoginAttempts);
		return XV.toInteger(value);
	}
	
	@Override
	public String getPasswordHash(XID actorId) {
		assert actorId != null;
		XValue value = WritableUtils.getValue(this.authenticationModel, actorId, hasPasswordHash);
		return XV.toString(value);
	}
	
	@Override
	@ModificationOperation
	public int incrementFailedLoginAttempts(XID actorId) {
		int failedLoginAttempts = getFailedLoginAttempts(actorId);
		failedLoginAttempts++;
		XIntegerValue failedLoginAttemptsValue = X.getValueFactory().createIntegerValue(
		        failedLoginAttempts);
		boolean result = WritableUtils.setValue(this.authenticationModel, actorId,
		        hasFailedLoginAttempts, failedLoginAttemptsValue);
		assert result : "we should have been able to set the new number of failed login attempts for actor '"
		        + actorId + "' to " + failedLoginAttempts;
		return failedLoginAttempts;
	}
	
	@Override
	@ModificationOperation
	public void removePasswordHash(XID actorId) {
		WritableUtils.removeValue(this.authenticationModel, actorId, hasPasswordHash);
	}
	
	@Override
	@ModificationOperation
	public void resetFailedLoginAttempts(XID actorId) {
		WritableUtils.removeValue(this.authenticationModel, actorId, hasFailedLoginAttempts);
	}
	
	@Override
	@ModificationOperation
	public void setPasswordHash(XID actorId, String passwordHash) {
		XStringValue passwordHashValue = X.getValueFactory().createStringValue(passwordHash);
		boolean result = WritableUtils.setValue(this.authenticationModel, actorId, hasPasswordHash,
		        passwordHashValue);
		assert result;
	}
	
}
