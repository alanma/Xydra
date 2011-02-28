package org.xydra.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.json.JSONObject;
import org.xydra.json.JsonUtils;


public class TestJSon {
	
	@Test
	public void testJsonObject() throws Exception {
		JSONObject model = new JSONObject();
		JSONObject phonebook = new JSONObject();
		JSONObject john = new JSONObject();
		john.put("age", 32);
		john.put("name", "John Doe");
		john.put("aliases", new String[] { "Johnny", "Joe Black" });
		JSONObject dirk = new JSONObject();
		dirk.put("age", 31);
		dirk.put("name", "Mr. Hagemann");
		john.put("knows", dirk);
		phonebook.put("john", john);
		model.put("phonebook", phonebook);
		
		String syntax = model.toString(1);
		System.out.println(syntax);
		
		JSONObject model2 = (JSONObject)JsonUtils.parse(syntax);
		
		assertEquals(model.toString(), model2.toString());
		
		System.out.println(model.toString(1));
	}
}
