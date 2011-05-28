package org.xydra.core.serialize.xml;

import org.xydra.core.serialize.AbstractSerializedValueTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class XmlValueTest extends AbstractSerializedValueTest {
	
	@Override
	protected XydraOut getNewOut() {
		return new XmlOut();
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserXml();
	}
	
}
