package org.xydra.store;

import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlSerializer;


/**
 * Please make sure your remote server is running.
 */
public class RemoteXmlRestClientWriteMethodsTest extends AbstractRestClientWriteMethodsTest {
	
	static {
		serverconfig = ServerConfig.XYDRA_LIVE;
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
