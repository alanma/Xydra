package org.xydra.store;

import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlSerializer;


public class XmlRestClientWriteMethodsTest extends AbstractRestClientWriteMethodsTest {
	
	@Override
	protected XydraParser getParser() {
		return new XmlParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new XmlSerializer();
	}
	
}
