package org.xydra.store;

import java.io.File;
import java.net.URI;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.server.TestServer;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationManager;

public class LocalTestServerManager extends TestServer {

	private static final Logger log = LoggerFactory.getLogger(LocalTestServerManager.class);

	/**
	 * Start a local testserver that exposes the store API at '/xydra/store/v1/'
	 *
	 * @param serverConfig
	 *            config to reach and use the test server
	 * @return the started testserver instance
	 */
	public static TestServer start(final ServerConfig serverConfig) {
		final TestServer server = new TestServer(8973);

		final URI apiprefix = server.startServer("/xydra", new File("src/main/webapp"));

		log.info("Using apiprefix = " + apiprefix.toASCIIString());

		/**
		 * Note: resolve implicitly creates a sub-url as if it had started with
		 * a '/'. However, local urls starting with a slash '/' are resolved as
		 * the webapp root. Therefore we leave the slash out here.
		 */
		final URI storeapi = apiprefix.resolve("store/v1/");
		log.info("Using store API url = " + storeapi.toASCIIString());

		final XydraStore store = server.getStore();

		final XydraStoreAdmin admin = store.getXydraStoreAdmin();
		final XAuthenticationDatabase auth = admin.getAccessControlManager().getAuthenticationDatabase();
		auth.setPasswordHash(serverConfig.testerActor, serverConfig.testerPasswordHash);
		final XAuthorisationManager access = admin.getAccessControlManager().getAuthorisationManager();
		final XAddress repoAddr = Base.toAddress(admin.getRepositoryId(), null, null, null);
		access.getAuthorisationDatabase().setAccess(serverConfig.testerActor, repoAddr,
				XA.ACCESS_READ, true);
		access.getAuthorisationDatabase().setAccess(serverConfig.testerActor, repoAddr,
				XA.ACCESS_WRITE, true);

		return server;
	}

	public static void stop(final TestServer server, final ServerConfig serverConfig) {
		final XydraStore store = server.getStore();

		// cleanup access rights
		final XydraStoreAdmin admin = store.getXydraStoreAdmin();
		final XAuthenticationDatabase auth = admin.getAccessControlManager().getAuthenticationDatabase();
		auth.setPasswordHash(serverConfig.testerActor, null);
		final XAuthorisationManager access = admin.getAccessControlManager().getAuthorisationManager();
		final XAddress repoAddr = Base.toAddress(admin.getRepositoryId(), null, null, null);
		access.getAuthorisationDatabase().resetAccess(serverConfig.testerActor, repoAddr,
				XA.ACCESS_READ);
		access.getAuthorisationDatabase().resetAccess(serverConfig.testerActor, repoAddr,
				XA.ACCESS_WRITE);

		// stop Jersey
		server.stopServer();
	}

}
