package org.xydra.store;

import java.net.URI;

import org.xydra.base.XID;
import org.xydra.base.XX;


/**
 * Configuration for a server used in tests.
 * 
 * Required for both server and client side.
 * 
 */
public class ServerConfig {
	
	public static final ServerConfig XYDRA_LIVE = new ServerConfig(
	        URI.create("http://testgae20111009.xydra-live.appspot.com/logged/xydra/store/v1/"),
	        XX.toId("testActor"), "secret", XX.toId("repo_allow_all"));
	
	public static final ServerConfig TEST_GAE_LOCAL = new ServerConfig(
	        URI.create("http://localhost:8787/xydra/store/v1/"), XX.toId("tester"), "secret",
	        XX.toId("repo_allow_all"));
	
	/**
	 * @param absoluteURI make sure to have a trailing slash
	 * @param testerActor ..
	 * @param testerPasswordHash ..
	 * @param mainRepositoryId as configured for the server
	 */
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