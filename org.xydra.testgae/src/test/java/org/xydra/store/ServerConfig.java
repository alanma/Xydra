package org.xydra.store;

import java.net.URI;

import org.xydra.base.XId;
import org.xydra.core.XX;

/**
 * Configuration for a server used in tests.
 *
 * Required for both server and client side.
 *
 */
public class ServerConfig {

	public static final ServerConfig XYDRA_LIVE = new ServerConfig(
			URI.create("http://testgae20121129.xydra-1.appspot.com/logged/xydra/store/v1/"),
			XX.toId("testActor"), "secret", XX.toId("repo_allow_all"));

	public static final ServerConfig TEST_GAE_LOCAL = new ServerConfig(
			URI.create("http://localhost:8787/xydra/store/v1/"), XX.toId("tester"), "secret",
			XX.toId("repo_allow_all"));

	/**
	 * @param absoluteURI
	 *            make sure to have a trailing slash
	 * @param testerActor
	 *            ..
	 * @param testerPasswordHash
	 *            ..
	 * @param mainRepositoryId
	 *            as configured for the server
	 */
	public ServerConfig(final URI absoluteURI, final XId testerActor, final String testerPasswordHash,
			final XId mainRepositoryId) {
		this.absoluteURI = absoluteURI;
		this.testerActor = testerActor;
		this.testerPasswordHash = testerPasswordHash;
		this.mainRepositoryId = mainRepositoryId;
	}

	public URI absoluteURI;

	public XId testerActor;

	public String testerPasswordHash;

	public XId mainRepositoryId;

}
