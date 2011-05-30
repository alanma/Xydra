package org.xydra.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.test.TestServer;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.impl.rest.XydraStoreRestClient;


/**
 * Abstract test framework for Xydra REST APIs
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractRestClientReadMethodsTest extends AbstractStoreReadMethodsTest {
	
	abstract protected XydraSerializer getSerializer();
	
	abstract protected XydraParser getParser();
	
	private final XydraSerializer serializer;
	private final XydraParser parser;
	
	protected AbstractRestClientReadMethodsTest() {
		this.serializer = getSerializer();
		this.parser = getParser();
	}
	
	protected XydraOut create() {
		return this.serializer.create();
	}
	
	protected XydraElement parse(String data) {
		return this.parser.parse(data);
	}
	
	private static final String PASSWORD_TESTER = "secret";
	
	private static final XID ACTOR_TESTER = XX.toId("tester");
	
	private static final Logger log = LoggerFactory
	        .getLogger(AbstractRestClientReadMethodsTest.class);
	
	private XydraStore clientStore;
	private static TestServer server;
	private static URI storeapi;
	
	@BeforeClass
	public static void init() {
		
		// initialize Jersey
		assertNull(server);
		server = new TestServer(8973);
		
		URI apiprefix = server.startServer("/xydra", new File("src/main/webapp"));
		
		log.info("Using apiprefix = " + apiprefix.toASCIIString());
		
		/**
		 * Note: resolve implicitly creates a sub-url as if it had started with
		 * a '/'. However, local urls starting with a slash '/' are resolved as
		 * the webapp root. Therefore we leave the slash out here.
		 */
		
		storeapi = apiprefix.resolve("store/v1/");
		log.info("Using store API url = " + storeapi.toASCIIString());
		
		XydraStore store = server.getStore();
		
		XydraStoreAdmin admin = store.getXydraStoreAdmin();
		XAuthenticationDatabase auth = admin.getAccessControlManager().getAuthenticationDatabase();
		auth.setPasswordHash(ACTOR_TESTER, PASSWORD_TESTER);
		XAuthorisationManager access = admin.getAccessControlManager().getAuthorisationManager();
		XAddress repoAddr = XX.toAddress(admin.getRepositoryId(), null, null, null);
		access.getAuthorisationDatabase().setAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_READ, true);
		access.getAuthorisationDatabase().setAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_WRITE, true);
	}
	
	@Override
	@Before
	public void setUp() {
		this.clientStore = new XydraStoreRestClient(storeapi, this.serializer, this.parser);
		super.setUp();
	}
	
	@After
	public void tearDown() {
		SynchronousTestCallback<Set<XID>> mids = new SynchronousTestCallback<Set<XID>>();
		this.store.getModelIds(getCorrectUser(), getCorrectUserPasswordHash(), mids);
		assertEquals(SynchronousTestCallback.SUCCESS, mids.waitOnCallback(Long.MAX_VALUE));
		XAddress repoAddr = XX.toAddress(getRepositoryId(), null, null, null);
		for(XID modelId : mids.effect) {
			if(modelId.toString().startsWith("internal--")) {
				continue;
			}
			XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(repoAddr,
			        XCommand.FORCED, modelId);
			this.store.executeCommands(getCorrectUser(), getCorrectUserPasswordHash(),
			        new XCommand[] { removeCommand }, null);
		}
	}
	
	@AfterClass
	public static void cleanup() {
		
		XydraStore store = server.getStore();
		
		// cleanup access rights
		XydraStoreAdmin admin = store.getXydraStoreAdmin();
		XAuthenticationDatabase auth = admin.getAccessControlManager().getAuthenticationDatabase();
		auth.setPasswordHash(ACTOR_TESTER, null);
		XAuthorisationManager access = admin.getAccessControlManager().getAuthorisationManager();
		XAddress repoAddr = XX.toAddress(admin.getRepositoryId(), null, null, null);
		access.getAuthorisationDatabase().resetAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_READ);
		access.getAuthorisationDatabase().resetAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_WRITE);
		
		// stop Jersey
		server.stopServer();
		server = null;
		
	}
	
	@Override
	protected XCommandFactory getCommandFactory() {
		return X.getCommandFactory();
	}
	
	@Override
	protected XID getCorrectUser() {
		return ACTOR_TESTER;
	}
	
	@Override
	protected String getCorrectUserPasswordHash() {
		return PASSWORD_TESTER;
	}
	
	@Override
	protected XID getIncorrectUser() {
		return XX.toId("bob");
	}
	
	@Override
	protected String getIncorrectUserPasswordHash() {
		return "wrong";
	}
	
	@Override
	protected XID getRepositoryId() {
		return server.getStore().getXydraStoreAdmin().getRepositoryId();
	}
	
	@Override
	protected XydraStore getStore() {
		return this.clientStore;
	}
	
}
