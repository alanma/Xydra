package org.xydra.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.xydra.core.X;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.rest.RepositoryManager;
import org.xydra.server.Jetty;
import org.xydra.server.gae.GaeTestfixer;



/**
 * Test for implementations of {@link IGroupDatabase}.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractRestApiTest {
	
	protected static final XID REPO_ID = X.getIDProvider().fromString("localrepo");
	
	protected static final XID ACTOR_TESTER = X.getIDProvider().fromString("tester");
	
	@BeforeClass
	public static void init() {
		
		GaeTestfixer.enable();
		
		// initialize GAE
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// initialize Jersey
		assertNull(server);
		server = new Jetty(8973);
		
		// configure
		XSPI.setStateStore(new GaeStateStore());
		
		apiprefix = server.startServer("/cxm", new File("src/main/webapp"));
		
		dataapi = apiprefix.resolve("data/");
		
		// set access rights to ALLOW for all users
		XProtectedRepository remoteRepo = RepositoryManager.getRepository(null);
		XAccessManager arm = RepositoryManager.getArmForRepository();
		arm.setAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_READ, true);
		arm.setAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_WRITE, true);
		
	}
	
	public static final String BAD_ID = "-";
	
	public static final String MISSING_ID = "cookiemonster";
	
	public static final XID NEW_ID = X.getIDProvider().fromString("new");
	
	@Before
	public void setUp() {
		
		// initialize GAE
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// initialize XModel
		// make sure that we don't write to the server's persistence backend
		repo = new MemoryRepository(REPO_ID);
		
		DemoModelUtil.addPhonebookModel(repo);
		
		// add something to the server for testing
		XRepository remoteRepo = RepositoryManager.getRepository();
		assertFalse(remoteRepo.hasModel(DemoModelUtil.PHONEBOOK_ID));
		DemoModelUtil.addPhonebookModel(remoteRepo);
		
	}
	
	@After
	public void tearDown() {
		
		XRepository remoteRepo = RepositoryManager.getRepository();
		remoteRepo.removeModel(null, DemoModelUtil.PHONEBOOK_ID);
		
	}
	
	@AfterClass
	public static void cleanup() {
		
		// cleanup access rights
		XRepository remoteRepo = RepositoryManager.getRepository();
		XAccessManager arm = RepositoryManager.getArmForRepository();
		arm.resetAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_READ);
		arm.resetAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_WRITE);
		
		// stop Jersey
		server.stopServer();
		server = null;
		
		// stop GAE
		GaeTestfixer.tearDown();
		
	}
	
	protected void setLoginDetails(HttpURLConnection c) {
		c.setRequestProperty("Cookie", "actor=" + ACTOR_TESTER.toString() + ";");
		// TODO set password once the server supports it
	}
	
	protected static Jetty server;
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
	
	protected MiniElement loadXml(URL url) throws IOException {
		HttpURLConnection c = (HttpURLConnection)url.openConnection();
		setLoginDetails(c);
		c.connect();
		int resp = c.getResponseCode();
		assertTrue(resp == HttpURLConnection.HTTP_OK || resp == HttpURLConnection.HTTP_NOT_FOUND);
		if(resp == HttpURLConnection.HTTP_NOT_FOUND) {
			return null;
		}
		assertEquals("application/xml", c.getContentType());
		String data = readAll((InputStream)c.getContent());
		
		try {
			return new MiniXMLParserImpl().parseXml(data);
		} catch(IllegalArgumentException iae) {
			fail(iae.getMessage());
			throw new RuntimeException();
		}
	}
	
	protected XModel getRemoteModel(XID modelId) throws IOException {
		
		URL modelUrl = dataapi.resolve(modelId.toURI()).toURL();
		
		MiniElement modelElement = loadXml(modelUrl);
		if(modelElement == null) {
			return null;
		}
		
		try {
			return XmlModel.toModel(modelElement);
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
