package org.xydra.core.serialize.xml;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class XmlValueTestWhitespace extends AbstractSerializedModelTest {
	
	@Override
	protected XydraOut getNewOut() {
		XydraOut out = new XmlOut();
		out.enableWhitespace(true, true);
		return out;
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserXml();
	}
	
}
