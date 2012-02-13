package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;

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
		/*
		 * The model was already indexed during the setup() method
		 */

		for(XID objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				ValueIndexEntry entry = new ValueIndexEntry(object.getAddress(), value, 42);
				
				String s = entry.serializeAsString(false);
				
				ValueIndexEntry parsedEntry = ValueIndexEntry.fromString(s);
				
				assertEquals(entry.getAddress(), parsedEntry.getAddress());
				assertEquals(entry.getValue(), parsedEntry.getValue());
				assertEquals(entry.getCounter(), parsedEntry.getCounter());
			}
		}
	}
}
