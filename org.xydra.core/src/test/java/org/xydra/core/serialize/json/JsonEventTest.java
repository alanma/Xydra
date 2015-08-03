package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedEventTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class JsonEventTest extends AbstractSerializedEventTest {

	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}

	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}

}
