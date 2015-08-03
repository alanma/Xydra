package org.xydra.store;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.xydra.base.Base;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlSerializer;
import org.xydra.server.TestServer;
import org.xydra.store.impl.memory.SecureMemoryStore;

/**
 * Test XML REST API against a local server
 *
 * @author xamde
 *
 */
public class LocalXmlRestClientWriteMethodsTest extends AbstractRestClientWriteMethodsTest {

	static {
		serverconfig = new ServerConfig(URI.create("http://localhost:8973/xydra/store/v1/"),
				Base.toId("testActor"), "secret", SecureMemoryStore.DEFAULT_REPOSITORY_ID);
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
		return new XmlParser();
	}

	@Override
	protected XydraSerializer getSerializer() {
		return new XmlSerializer();
	}

}
