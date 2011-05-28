package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedEventTest;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.XydraOut;


public class JsonEventTest extends AbstractSerializedEventTest {
	
	@Override
	protected XydraOut getNewOut() {
		return new JsonOut();
	}
	
	@Override
	protected MiniParser getParser() {
		return new MiniParserJson();
	}
	
}
