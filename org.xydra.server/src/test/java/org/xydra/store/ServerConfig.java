package org.xydra.store;

import java.net.URI;

import org.xydra.base.XID;


/**
 * Configuration for a server used in tests.
 * 
 * Required for both server and client side.
 * 
 */
public class ServerConfig {
	
	public ServerConfig(URI absoluteURI, XID testerActor, String testerPasswordHash,
	        XID mainRepositoryId) {
		this.absoluteURI = absoluteURI;
		this.testerActor = testerActor;
		this.testerPasswordHash = testerPasswordHash;
		this.mainRepositoryId = mainRepositoryId;
	}
	
	public URI absoluteURI;
	
	public XID testerActor;
	
	public String testerPasswordHash;
	
	public XID mainRepositoryId;
	
}
