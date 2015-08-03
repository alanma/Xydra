package org.xydra.store.base;

import org.xydra.base.XId;


/**
 * Internal class that simply stores an actorId and a passwordHash.
 *
 * @author xamde
 */
public class Credentials {

	private XId actorId;
	private String passwordHash;

	public Credentials(final XId actorId, final String passwordHash) {
		super();
		setActorId(actorId);
		setPasswordHash(passwordHash);
	}

	public XId getActorId() {
		return this.actorId;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public void setActorId(final XId actorId) {
		this.actorId = actorId;
	}

	public void setPasswordHash(final String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
