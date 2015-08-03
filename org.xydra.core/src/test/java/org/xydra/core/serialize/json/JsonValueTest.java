package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedValueTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class JsonValueTest extends AbstractSerializedValueTest {

	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}

	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}

}
