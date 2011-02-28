package org.xydra.json;

public class JsonUtils {
	
	public static Object parse(String json) throws JSONException {
		BuilderSAJ saj = new BuilderSAJ();
		JsonParser parser = new JsonParser(saj);
		parser.parse(json);
		return saj.getParsed();
	}
	
}
