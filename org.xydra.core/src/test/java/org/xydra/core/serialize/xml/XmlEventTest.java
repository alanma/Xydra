package org.xydra.core.serialize.xml;

import org.xydra.core.serialize.AbstractSerializedEventTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class XmlEventTest extends AbstractSerializedEventTest {
	
	@Override
	protected XydraParser getParser() {
		return new XmlParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new XmlSerializer();
	}
	
}
