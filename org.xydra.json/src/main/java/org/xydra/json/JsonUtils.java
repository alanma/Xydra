package org.xydra.json;

import org.xydra.core.serialize.json.JSONException;
import org.xydra.core.serialize.json.JsonParserSAJ;

public class JsonUtils {
	
	public static Object parse(String json) throws JSONException {
		BuilderSAJ saj = new BuilderSAJ();
		JsonParserSAJ parser = new JsonParserSAJ(saj);
		parser.parse(json);
		return saj.getParsed();
	}
	
}
