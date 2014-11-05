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
	
	public Credentials(XId actorId, String passwordHash) {
		super();
		this.setActorId(actorId);
		this.setPasswordHash(passwordHash);
	}
	
	public XId getActorId() {
		return this.actorId;
	}
	
	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	public void setActorId(XId actorId) {
		this.actorId = actorId;
	}
	
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
