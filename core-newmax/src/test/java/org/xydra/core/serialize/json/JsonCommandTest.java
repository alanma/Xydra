package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedCommandTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class JsonCommandTest extends AbstractSerializedCommandTest {
	
	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}
	
}
