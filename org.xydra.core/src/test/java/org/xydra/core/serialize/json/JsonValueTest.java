package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedModelTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class JsonValueTest extends AbstractSerializedModelTest {
	
	@Override
	protected XydraOut getNewOut() {
		return new JsonOut();
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserJson();
	}
	
}
