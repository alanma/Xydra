package org.xydra.store;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.base.XX;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.server.TestServer;
import org.xydra.store.impl.memory.SecureMemoryStore;


public class LocalJsonRestClientReadMethodsTest extends AbstractRestClientReadMethodsTest {
	
	static {
		serverconfig = new ServerConfig(URI.create("http://localhost:8973/xydra/store/v1/"),
		        XX.toId("testActor"), "secret", SecureMemoryStore.DEFAULT_REPOSITORY_ID);
	}
	
	private static TestServer server;
	
	@BeforeClass
	public static void init() {
		server = LocalTestServerManager.start(getServerConfig());
	}
	
	@AfterClass
	public static void cleanup() {
		LocalTestServerManager.stop(server, getServerConfig());
		server = null;
	}
	
	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}
	
}
