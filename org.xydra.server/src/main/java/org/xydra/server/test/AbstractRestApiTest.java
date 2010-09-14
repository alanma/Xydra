package org.xydra.server.test;

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
import org.xydra.core.XX;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.server.RepositoryManager;


/**
 * Abstract test framework for Xydra REST APIs
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractRestApiTest {
	
	protected static final XID REPO_ID = XX.toId("localrepo");
	
	protected static final XID ACTOR_TESTER = XX.toId("tester");
	
	@BeforeClass
	public static void init() {
		
		// initialize Jersey
		assertNull(server);
		server = new Jetty(8973);
		
		apiprefix = server.startServer("/xydra", new File("src/main/webapp"));
		
		dataapi = apiprefix.resolve("data/");
		
		// set access rights to ALLOW for all users
		XProtectedRepository remoteRepo = RepositoryManager.getRepository(null);
		XAccessManager arm = RepositoryManager.getArmForRepository();
		arm.setAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_READ, true);
		arm.setAccess(ACTOR_TESTER, remoteRepo.getAddress(), XA.ACCESS_WRITE, true);
		
	}
	
	public static final String BAD_ID = "-";
	
	public static final String MISSING_ID = "cookiemonster";
	
	public static final XID NEW_ID = XX.toId("new");
	
	@Before
	public void setUp() {
		
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
		
	}
	
	protected void setLoginDetails(HttpURLConnection c) {
		c.setRequestProperty("Cookie", "actor=" + ACTOR_TESTER.toString());
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
