package org.xydra.json;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.json.BroadcastSAJ;
import org.xydra.json.BuilderSAJ;
import org.xydra.json.DumpSAJ;
import org.xydra.json.JSONException;
import org.xydra.json.JSONObject;
import org.xydra.json.JsonParser;
import org.xydra.json.SAJ;


public class TestJsonParser {
	static String test = "{\"phonebook\": {\"john\": {\r\n" + " \"age\": 32,\r\n"
	        + " \"aliases\": [\r\n" + "  \"Johnny\",\r\n" + "  \"Joe Black\"\r\n" + " ],\r\n"
	        + " \"knows\": {\r\n" + "  \"age\": 31,\r\n" + "  \"name\": \"Mr. Hagemann\"\r\n"
	        + " },\r\n" + " \"name\": \"John Doe\"\r\n" + "}}}";
	
	@Test
	public void testSimple() throws JSONException {
		SAJ saj = new DumpSAJ();
		JsonParser jsonParser = new JsonParser(saj);
		jsonParser.parse(test);
	}
	
	@Test
	public void testSimple2() throws JSONException {
		BuilderSAJ saj = new BuilderSAJ();
		DumpSAJ dumpSAJ = new DumpSAJ();
		BroadcastSAJ broadcastSAJ = new BroadcastSAJ();
		broadcastSAJ.addSAJ(saj);
		broadcastSAJ.addSAJ(dumpSAJ);
		JsonParser jsonParser = new JsonParser(broadcastSAJ);
		jsonParser.parse(test);
		Object o = saj.getParsed();
		assertTrue(o instanceof JSONObject);
		JSONObject jo = (JSONObject)o;
		assertTrue(jo.has("phonebook"));
	}
	
}
