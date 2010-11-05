package org.xydra.core.json;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.XCompareUtils;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.test.DemoModelUtil;
import org.xydra.json.JSONException;


@RunsInJava
public class XJsonTest {
	
	public static final String PHONEBOOK_MODEL_JSON = "{\r\n" + "    \"phonebook\": [\r\n"
	        + "        {\r\n" + "            \"john\": [\r\n" + "                {\r\n"
	        + "                    \"phone\": \"3463-2346\"\r\n" + "                },\r\n"
	        + "                {\r\n" + "                    \"maxCallTime\": 675874678478467\r\n"
	        + "                },\r\n" + "                {\r\n"
	        + "                    \"title\": \"Dr. John Doe\"\r\n" + "                },\r\n"
	        + "                {\r\n" + "                    \"scores\": [\r\n"
	        + "                        34,\r\n" + "                        234,\r\n"
	        + "                        34\r\n" + "                    ]\r\n"
	        + "                },\r\n" + "                {\r\n"
	        + "                    \"emptyfield\": null\r\n" + "                },\r\n"
	        + "                {\r\n" + "                    \"friends\": [\r\n"
	        + "                        \"claudia\",\r\n" + "                        \"peter\"\r\n"
	        + "                    ]\r\n" + "                },\r\n" + "                {\r\n"
	        + "                    \"age\": 42\r\n" + "                },\r\n"
	        + "                {\r\n" + "                    \"height\": 121.3\r\n"
	        + "                },\r\n" + "                {\r\n"
	        + "                    \"lastCallTime\": [\r\n" + "                        32456,\r\n"
	        + "                        7664,\r\n" + "                        56\r\n"
	        + "                    ]\r\n" + "                },\r\n" + "                {\r\n"
	        + "                    \"hidden\": false\r\n" + "                },\r\n"
	        + "                {\r\n" + "                    \"spouse\": \"claudia\"\r\n"
	        + "                },\r\n" + "                {\r\n"
	        + "                    \"flags\": [\r\n" + "                        true,\r\n"
	        + "                        false,\r\n" + "                        true,\r\n"
	        + "                        true,\r\n" + "                        false\r\n"
	        + "                    ]\r\n" + "                },\r\n" + "                {\r\n"
	        + "                    \"aliases\": [\r\n" + "                        \"Johnny\",\r\n"
	        + "                        \"John the Man\",\r\n"
	        + "                        \"Cookie Monster\"\r\n" + "                    ]\r\n"
	        + "                },\r\n" + "                {\r\n"
	        + "                    \"coordinates\": [\r\n" + "                        32.465,\r\n"
	        + "                        19.34\r\n" + "                    ]\r\n"
	        + "                }\r\n" + "            ]\r\n" + "        },\r\n" + "        {\r\n"
	        + "            \"peter\": [\r\n" + "                \r\n" + "            ]\r\n"
	        + "        },\r\n" + "        {\r\n" + "            \"claudia\": [\r\n"
	        + "                \r\n" + "            ]\r\n" + "        }\r\n" + "    ]\r\n" + "}";
	
	@Test
	public void testAsJsonStringXModel() {
		XRepository repo = X.createMemoryRepository();
		DemoModelUtil.addPhonebookModel(repo);
		XModel phonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		String json = XJson.asJsonString(phonebook);
		System.out.println(json);
	}
	
	@Test
	public void testParseJsonModel() throws IllegalArgumentException, JSONException {
		XRepository repo = X.createMemoryRepository();
		XJson.addToXRepository(X.getIDProvider().fromString("test-actor"), PHONEBOOK_MODEL_JSON,
		        repo);
	}
	
	@Test
	public void serialiseAndParse() throws IllegalArgumentException, JSONException {
		XRepository repo = X.createMemoryRepository();
		XJson.addToXRepository(X.getIDProvider().fromString("test-actor"), PHONEBOOK_MODEL_JSON,
		        repo);
		XModel phonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		String json = XJson.asJsonString(phonebook);
		
		XRepository repo2 = X.createMemoryRepository();
		XJson.addToXRepository(X.getIDProvider().fromString("test-actor"), json, repo2);
		XModel phonebook2 = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		assertTrue(XCompareUtils.equalTree(phonebook, phonebook2));
	}
	
}
