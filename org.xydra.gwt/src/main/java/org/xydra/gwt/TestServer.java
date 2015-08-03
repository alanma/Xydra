package org.xydra.gwt;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationManager;

/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
 * required to run the webapp.
 *
 * This class requires to build and assemble the web app: call
 * <code>mvn clean compile gwt:compile gwt:mergewebxml war:war -Dmaven.test.skip=true -o
 * </code>
 *
 * <p>
 * If only the client source code changed, use <code>mvn gwt:compile</code>
 *
 * <p>
 * If the web.xml changed use
 * <code>mvn gwt:mergwebxml war:war to update the web.xml in target/iba-1.0.0-SNAPSHOT/WEB-INF
 *
 * <p>If only static files have been modified, no call is neccesary as this Jetty is configured to load the directly from src/main/webapp.
 *
 * @author xamde
 *
 */
public class TestServer {

	private static final Logger log = getLogger();

	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(TestServer.class);
	}

	private final int port;

	private Server server;

	private WebAppContext webapp;

	public TestServer() {
		this(8080);
	}

	public TestServer(final int port) {
		this.port = port;
	}

	/**
	 * @param contextPath
	 *            of webapp
	 * @param docRoot
	 *            webapp root on local disc
	 * @return absolute url of server + port + context
	 */
	public URI startServer(final String contextPath, final File docRoot) {
		log.info("Starting server with docRoot=" + docRoot.getAbsolutePath() + " contextPaht="
				+ contextPath);

		if (this.server != null) {
			throw new RuntimeException("server is already startet");
		}

		// Create an instance of the jetty server.
		this.server = new Server(this.port);

		/*
		 * Create the webapp. This will load the servlet configuration from
		 * docRoot + WEB-INF/web.xml
		 */
		this.webapp = new WebAppContext(docRoot.getAbsolutePath(), contextPath);

		// Add the webapp to the server.
		this.server.setHandler(this.webapp);

		try {
			this.server.start();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		try {
			return new URI("http://localhost:" + this.port + "/").resolve(contextPath + "/");
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

	public void stopServer() {
		if (this.server != null) {
			try {
				this.server.stop();
				this.server.destroy();
			} catch (final Exception e) {
				throw new RuntimeException("error stopping server", e);
			}
			this.server = null;
		}
	}

	public XydraStore getStore() {
		if (this.webapp == null) {
			throw new RuntimeException("cannot get backend before server is started");
		}
		final ServletContext sc = this.webapp.getServletContext();
		return (XydraStore) sc.getAttribute("org.xydra.store");
	}

	public static void main(final String[] args) throws Exception {

		// start jetty
		new TestServer().startXydraServer(new File("src/main/webapp"));

	}

	public URI startXydraServer(final File webapp) {

		final URI uri = startServer("/xydra", webapp);

		// initialize the store

		final XydraStore store = getStore();
		assert store != null;

		final XydraStoreAdmin admin = store.getXydraStoreAdmin();
		final XAuthenticationDatabase auth = admin.getAccessControlManager().getAuthenticationDatabase();
		final XId actorId = Base.toId("tester");
		final String passwordHash = "secret";
		auth.setPasswordHash(actorId, "secret");
		final XAuthorisationManager access = admin.getAccessControlManager().getAuthorisationManager();
		final XAddress repoAddr2 = Base.toAddress(admin.getRepositoryId(), null, null, null);
		access.getAuthorisationDatabase().setAccess(actorId, repoAddr2, XA.ACCESS_READ, true);
		access.getAuthorisationDatabase().setAccess(actorId, repoAddr2, XA.ACCESS_WRITE, true);

		final XRepositoryCommand createCommand2 = MemoryRepositoryCommand.createAddCommand(repoAddr2,
				XCommand.SAFE_STATE_BOUND, DemoModelUtil.PHONEBOOK_ID);
		final XAddress modelAddr2 = createCommand2.getChangedEntity();
		final XTransactionBuilder tb2 = new XTransactionBuilder(modelAddr2);
		DemoModelUtil.setupPhonebook(modelAddr2, tb2, true);

		store.executeCommands(actorId, passwordHash,
				new XCommand[] { createCommand2, tb2.buildCommand() }, null);

		log.info("Started embedded Jetty server. User interface is at " + uri.toString());

		return uri;
	}
}
