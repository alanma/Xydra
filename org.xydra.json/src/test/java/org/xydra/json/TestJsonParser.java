package org.xydra.json;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.core.serialize.json.JSONException;
import org.xydra.core.serialize.json.JsonParserSAJ;
import org.xydra.core.serialize.json.SAJ;
import org.xydra.json.BroadcastSAJ;
import org.xydra.json.BuilderSAJ;
import org.xydra.json.DumpSAJ;
import org.xydra.json.JSONObject;


public class TestJsonParser {
	static String test = "{\"phonebook\": {\"john\": {\r\n" + " \"age\": 32,\r\n"
	        + " \"aliases\": [\r\n" + "  \"Johnny\",\r\n" + "  \"Joe Black\"\r\n" + " ],\r\n"
	        + " \"knows\": {\r\n" + "  \"age\": 31,\r\n" + "  \"name\": \"Mr. Hagemann\"\r\n"
	        + " },\r\n" + " \"name\": \"John Doe\"\r\n" + "}}}";
	
	@Test
	public void testSimple() throws JSONException {
		SAJ saj = new DumpSAJ();
		JsonParserSAJ jsonParser = new JsonParserSAJ(saj);
		jsonParser.parse(test);
	}
	
	@Test
	public void testSimple2() throws JSONException {
		BuilderSAJ saj = new BuilderSAJ();
		DumpSAJ dumpSAJ = new DumpSAJ();
		BroadcastSAJ broadcastSAJ = new BroadcastSAJ();
		broadcastSAJ.addSAJ(saj);
		broadcastSAJ.addSAJ(dumpSAJ);
		JsonParserSAJ jsonParser = new JsonParserSAJ(broadcastSAJ);
		jsonParser.parse(test);
		Object o = saj.getParsed();
		assertTrue(o instanceof JSONObject);
		JSONObject jo = (JSONObject)o;
		assertTrue(jo.has("phonebook"));
	}
	
}
