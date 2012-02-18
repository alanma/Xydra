package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.serialize.SerializedValue;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


public class ValueIndexEntryParsingTest {
	private XModel model;
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo);
		
		this.model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
	}
	
	@Test
	public void testParsingEntries() {
		for(XID objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				double d = Math.random() * 100;
				int i = (int)d;
				
				ValueIndexEntry entry = new ValueIndexEntry(object.getAddress(), value, i);
				
				String s = ValueIndexEntryUtils.serializeAsString(entry);
				
				ValueIndexEntry parsedEntry = ValueIndexEntryUtils.fromString(s);
				
				assertEquals(entry.getAddress(), parsedEntry.getAddress());
				assertEquals(entry.getValue(), parsedEntry.getValue());
				assertEquals(entry.getCounter(), parsedEntry.getCounter());
			}
		}
	}
	
	@Test
	public void testParstingEntryArrays() {
		ArrayList<ValueIndexEntry> list = new ArrayList<ValueIndexEntry>();
		
		for(XID objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				double d = Math.random() * 100;
				int i = (int)d;
				ValueIndexEntry entry = new ValueIndexEntry(object.getAddress(), value, i);
				
				list.add(entry);
			}
		}
		
		ValueIndexEntry[] array = new ValueIndexEntry[list.size()];
		String asString = ValueIndexEntryUtils.serializeAsString(list.toArray(array));
		
		ValueIndexEntry[] parsedArray = ValueIndexEntryUtils.getArrayFromString(asString);
		
		assertEquals(array.length, parsedArray.length);
		
		for(int i = 0; i < array.length; i++) {
			assertEquals(array[i].getAddress(), parsedArray[i].getAddress());
			assertEquals(array[i].getValue(), parsedArray[i].getValue());
			assertEquals(array[i].getCounter(), parsedArray[i].getCounter());
		}
	}
	
	@Test
	public void linebreakJsonTest() {
		/*
		 * This test only fails if linebreaks in a String value are also
		 * existent in the Json string after serializing. The whole index works
		 * under the assumption that a Json string will never contain a
		 * linebreak if enableWhitespace(false,false) was called on the used
		 * XydraOut instance. Even if this test succeeds, it doesn't mean that
		 * Json strings of other value types might contain linebreaks - but if
		 * it fails, it is a sure sign that there's something wrong with this
		 * assumption for your implementation.
		 */

		JsonSerializer serializer = new JsonSerializer();
		
		XValue value = X.getValueFactory().createStringValue("test" + '\n' + "test");
		
		XydraOut out = serializer.create();
		out.enableWhitespace(false, false);
		SerializedValue.serialize(value, out);
		
		String valString = out.getData();
		
		System.out.println(valString);
		assertFalse("There was a linebreak in the Json string - but the whole index works "
		        + "under the assumption that a Json string will never contain "
		        + "a linebreak if enableWhitespace(false,false) was called.",
		        valString.contains("" + '\n'));
		
		JsonParser parser = new JsonParser();
		
		// parse address (use substrings to get rid of beginning/trailing ")
		XydraElement valElement = parser.parse(valString);
		
		XValue parsedValue = SerializedValue.toValue(valElement);
		assertEquals(value, parsedValue);
		
	}
}
