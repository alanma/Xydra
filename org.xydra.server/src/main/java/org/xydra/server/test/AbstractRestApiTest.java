package org.xydra.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.store.access.XA;


/**
 * Abstract test framework for Xydra REST APIs
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractRestApiTest {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractRestApiTest.class);
	
	protected static final XID REPO_ID = XX.toId("localrepo");
	
	protected static final XID ACTOR_TESTER = XX.toId("tester");
	
	private static IXydraServer xydraServer;
	
	private static URI ping;
	
	@BeforeClass
	public static void init() {
		
		// initialize Jersey
		assertNull(server);
		server = new TestServer(8973);
		
		apiprefix = server.startServer("/xydra", new File("src/main/webapp"));
		
		log.info("Using apiprefix = " + apiprefix.toASCIIString());
		
		/**
		 * Note: resolve implicitly creates a sub-url as if it had started with
		 * a '/'. However, local urls starting with a slash '/' are resolved as
		 * the webapp root. Therefore we leave the slash out here.
		 */
		
		dataapi = apiprefix.resolve("data/");
		log.info("Using data API url = " + dataapi.toASCIIString());
		
		ping = apiprefix.resolve("ping/");
		
		// set access rights to ALLOW for all users
		xydraServer = server.getBackend();
		
		XAddress repoAddr = xydraServer.getRepositoryAddress();
		XAccessManager arm = xydraServer.getAccessManager();
		arm.setAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_READ, true);
		arm.setAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_WRITE, true);
		
	}
	
	public static final String BAD_ID = "-";
	
	public static final String MISSING_ID = "cookiemonster";
	
	public static final XID NEW_ID = XX.toId("new");
	
	@Before
	public void setUp() {
		
		// initialize XModel
		// make sure that we don't write to the server's persistence backend
		repo = new MemoryRepository(ACTOR_TESTER, null, REPO_ID);
		
		// add something to the server for testing
		
		XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(xydraServer
		        .getRepositoryAddress(), XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		assertTrue(xydraServer.executeCommand(createCommand, ACTOR_TESTER) >= 0);
		XAddress modelAddr = createCommand.getChangedEntity();
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		DemoModelUtil.setupPhonebook(modelAddr, tb);
		XTransaction trans = tb.build();
		//  Apply events individually so there is something in the change log to
		// test
		for(XAtomicCommand ac : trans) {
			assertTrue(xydraServer.executeCommand(ac, ACTOR_TESTER) != XCommand.FAILED);
		}
		
		XRepositoryCommand createCommand2 = MemoryRepositoryCommand.createAddCommand(repo
		        .getAddress(), XCommand.SAFE, DemoModelUtil.PHONEBOOK_ID);
		assertTrue(repo.executeCommand(createCommand2) >= 0);
		XAddress localModelAddr = createCommand2.getChangedEntity();
		XTransactionBuilder tb2 = new XTransactionBuilder(localModelAddr);
		DemoModelUtil.setupPhonebook(localModelAddr, tb2);
		XTransaction trans2 = tb2.build();
		for(XAtomicCommand ac : trans2) {
			assertTrue(repo.executeCommand(ac) != XCommand.FAILED);
		}
		
	}
	
	@After
	public void tearDown() {
		
		XCommand removeCommand2 = MemoryRepositoryCommand.createRemoveCommand(repo.getAddress(),
		        XCommand.FORCED, DemoModelUtil.PHONEBOOK_ID);
		assertTrue(repo.executeCommand(removeCommand2) != XCommand.FAILED);
		
		XCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(xydraServer
		        .getRepositoryAddress(), XCommand.FORCED, DemoModelUtil.PHONEBOOK_ID);
		assertTrue(xydraServer.executeCommand(removeCommand, ACTOR_TESTER) != XCommand.FAILED);
		
	}
	
	@AfterClass
	public static void cleanup() {
		
		// cleanup access rights
		XAddress repoAddr = xydraServer.getRepositoryAddress();
		XAccessManager arm = xydraServer.getAccessManager();
		arm.resetAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_READ);
		arm.resetAccess(ACTOR_TESTER, repoAddr, XA.ACCESS_WRITE);
		
		// stop Jersey
		server.stopServer();
		server = null;
		
	}
	
	protected void setLoginDetails(HttpURLConnection c) {
		c.setRequestProperty("Cookie", "actor=" + ACTOR_TESTER.toString());
		// TODO set password once the server supports it
	}
	
	protected static TestServer server;
	protected static XRepository repo;
	protected static URI dataapi;
	protected static URI apiprefix;
	
	protected String readAll(InputStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		Reader reader = new InputStreamReader(stream, "UTF-8");
		int nRead;
		while((nRead = reader.read(buf)) != -1)
			sb.append(buf, 0, nRead);
		return sb.toString();
	}
	
	protected int getPing() throws IOException {
		URL pingurl = ping.toURL();
		HttpURLConnection c = (HttpURLConnection)pingurl.openConnection();
		setLoginDetails(c);
		c.connect();
		int resp = c.getResponseCode();
		return resp;
	}
	
	protected MiniElement loadXml(URL url) throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		int resp = c.getResponseCode();
		assertTrue("unexpected response: " + resp, resp == HttpURLConnection.HTTP_OK
		        || resp == HttpURLConnection.HTTP_NOT_FOUND);
		if(resp == HttpURLConnection.HTTP_NOT_FOUND) {
			return null;
		}
		assertEquals("application/xml; charset=UTF-8", c.getContentType());
		String data = readAll((InputStream)c.getContent());
		
		try {
			return new MiniXMLParserImpl().parseXml(data);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
	}
	
	protected XModel getRemoteModel(XID modelId) throws IOException {
		
		URL modelUrl = dataapi.resolve(modelId.toString()).toURL();
		
		MiniElement modelElement = loadXml(modelUrl);
		if(modelElement == null) {
			return null;
		}
		
		try {
			return XmlModel.toModel(ACTOR_TESTER, null, modelElement);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
		
	}
	
	protected void deleteResource(URL url) throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		c.setRequestMethod("DELETE");
		setLoginDetails(c);
		c.connect();
		assertEquals(HttpURLConnection.HTTP_NO_CONTENT, c.getResponseCode());
	}
	
}
