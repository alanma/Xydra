package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;

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


/**
 * This test checks whether the methods for parsing {@link ValueIndexEntry
 * ValueIndexEntries} provided by {@link ValueIndexEntryUtils} work correctly or
 * not. The phonebook model from {@link DemoModelUtil} is used as the basic
 * source of data.
 * 
 * @author Kaidel
 * 
 */

/*
 * TODO add testvalues which explicitly contain the symbols that will be escaped
 * to check whether the escaping works
 */

/*
 * TODO document in which cases an emtpy string might be returned and what that
 * means
 */

public class ValueIndexEntryParsingTest {
	private XModel model;
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo);
		
		this.model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
	}
	
	/**
	 * Tests whether parsing an {@link ValueIndexEntry} as a String works
	 * correctly.
	 */
	@Test
	public void testParsingEntries() {
		for(XID objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				ValueIndexEntry entry = new ValueIndexEntry(field.getAddress(), value);
				
				String s = ValueIndexEntryUtils.serializeAsString(entry);
				
				ValueIndexEntry parsedEntry = ValueIndexEntryUtils.fromString(s);
				
				assertEquals(entry.getAddress(), parsedEntry.getAddress());
				assertEquals(entry.getValue(), parsedEntry.getValue());
			}
		}
	}
	
	/**
	 * Tests whether parsing an array of {@link ValueIndexEntry
	 * ValueIndexEntries} as a String works correctly.
	 */
	@Test
	public void testParstingEntryArrays() {
		ArrayList<ValueIndexEntry> list = new ArrayList<ValueIndexEntry>();
		
		for(XID objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				ValueIndexEntry entry = new ValueIndexEntry(field.getAddress(), value);
				
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
		}
	}
}
