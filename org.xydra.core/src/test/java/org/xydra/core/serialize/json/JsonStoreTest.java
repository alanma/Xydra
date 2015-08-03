package org.xydra.core.serialize.json;

import org.xydra.core.serialize.AbstractSerializedStoreTest;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;


public class JsonStoreTest extends AbstractSerializedStoreTest {

	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}

	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}

}
