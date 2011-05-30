package org.xydra.core.serialize.xml;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class XmlValueTestWhitespace extends AbstractSerializedModelTest {
	
	@Override
	protected XydraParser getParser() {
		return new XmlParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new XmlSerializer();
	}
	
}
