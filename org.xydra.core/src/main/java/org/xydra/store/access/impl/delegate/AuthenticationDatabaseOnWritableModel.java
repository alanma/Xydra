package org.xydra.store.access.impl.delegate;

import java.io.Serializable;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.WritableUtils;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.sharedutils.XyAssert;
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
 * @author xamde
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class AuthenticationDatabaseOnWritableModel implements XAuthenticationDatabase, Serializable {

	public static final XId hasFailedLoginAttempts = XX.toId("hasFailedLoginAttempts");

	public static final XId hasPasswordHash = XX.toId("hasPasswordHash");
	private static final long serialVersionUID = 21938374542095483L;

	protected XWritableModel authenticationModel;

	/**
	 * @param authenticationModel used to read and write account management
	 *            data.
	 */
	public AuthenticationDatabaseOnWritableModel(final XWritableModel authenticationModel) {
		this.authenticationModel = authenticationModel;
	}

	@Override
	public void clear() {
		// delete all objects in account model
		WritableUtils.deleteAllObjects(this.authenticationModel);
	}

	@Override
	public int getFailedLoginAttempts(final XId actorId) {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		final XValue value = WritableUtils.getValue(this.authenticationModel, actorId,
		        hasFailedLoginAttempts);
		return XV.toInteger(value);
	}

	@Override
	public String getPasswordHash(final XId actorId) {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		final XValue value = WritableUtils.getValue(this.authenticationModel, actorId, hasPasswordHash);
		return XV.toString(value);
	}

	@Override
	@ModificationOperation
	public int incrementFailedLoginAttempts(final XId actorId) {
		int failedLoginAttempts = getFailedLoginAttempts(actorId);
		failedLoginAttempts++;
		final XIntegerValue failedLoginAttemptsValue = BaseRuntime.getValueFactory().createIntegerValue(
		        failedLoginAttempts);
		final boolean result = WritableUtils.setValue(this.authenticationModel, actorId,
		        hasFailedLoginAttempts, failedLoginAttemptsValue);
		assert result : "we should have been able to set the new number of failed login attempts for actor '"
		        + actorId + "' to " + failedLoginAttempts;
		return failedLoginAttempts;
	}

	@Override
	@ModificationOperation
	public void removePasswordHash(final XId actorId) {
		WritableUtils.removeValue(this.authenticationModel, actorId, hasPasswordHash);
	}

	@Override
	@ModificationOperation
	public void resetFailedLoginAttempts(final XId actorId) {
		WritableUtils.removeValue(this.authenticationModel, actorId, hasFailedLoginAttempts);
	}

	@Override
	@ModificationOperation
	public void setPasswordHash(final XId actorId, final String passwordHash) {
		final String currentHash = getPasswordHash(actorId);
		if(currentHash != null && currentHash.equals(passwordHash)) {
			// prevent NOOP
			return;
		}
		final XStringValue passwordHashValue = BaseRuntime.getValueFactory().createStringValue(passwordHash);
		final boolean result = WritableUtils.setValue(this.authenticationModel, actorId, hasPasswordHash,
		        passwordHashValue);
		assert result : "command to set passwordHash should execute with success";
	}

}
