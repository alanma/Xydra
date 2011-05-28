package org.xydra.core.serialize.xml;

import org.xydra.core.serialize.AbstractSerializedEventTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class XmlEventTest extends AbstractSerializedEventTest {
	
	@Override
	protected XydraOut getNewOut() {
		return new XmlOut();
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserXml();
	}
	
}
