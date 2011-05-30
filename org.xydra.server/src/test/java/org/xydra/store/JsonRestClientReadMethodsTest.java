package org.xydra.store;

import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


public class JsonRestClientReadMethodsTest extends AbstractRestClientReadMethodsTest {
	
	@Override
	protected XydraParser getParser() {
		return new JsonParser();
	}
	
	@Override
	protected XydraSerializer getSerializer() {
		return new JsonSerializer();
	}
	
}
