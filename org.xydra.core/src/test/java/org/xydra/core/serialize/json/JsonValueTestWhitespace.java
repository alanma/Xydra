package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class JsonValueTestWhitespace extends AbstractSerializedModelTest {
	
	@Override
	protected XydraOut getNewOut() {
		XydraOut out = new JsonOut();
		out.enableWhitespace(true, true);
		return out;
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserJson();
	}
	
}
