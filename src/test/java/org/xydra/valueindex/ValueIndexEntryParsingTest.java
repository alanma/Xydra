package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
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
		XId actorId = XX.createUniqueId();
		
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
		for(XId objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XId fieldId : object) {
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
	public void testParsingEntryArrays() {
		ArrayList<ValueIndexEntry> list = new ArrayList<ValueIndexEntry>();
		
		for(XId objectId : this.model) {
			XObject object = this.model.getObject(objectId);
			
			for(XId fieldId : object) {
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
	
	@Test
	public void testParsingSpecialStrings() {
		String s1 = "\\this\\is<entry>atest<entry> whether \" escaping really works \\ as it <entry> should.";
		String s2 = "another string containing <entry> the <\\entry> special symbols \" "
		        + "\\\" <entry> \" \" lets see if this works \\\\\\\\\\";
		
		XValue value1 = X.getValueFactory().createStringValue(s1);
		XValue value2 = X.getValueFactory().createStringValue(s2);
		
		XAddress address1 = XX.resolveField(XX.createUniqueId(), XX.createUniqueId(),
		        XX.createUniqueId(), XX.createUniqueId());
		XAddress address2 = XX.resolveField(XX.createUniqueId(), XX.createUniqueId(),
		        XX.createUniqueId(), XX.createUniqueId());
		
		ValueIndexEntry entry1 = new ValueIndexEntry(address1, value1);
		ValueIndexEntry entry2 = new ValueIndexEntry(address2, value2);
		
		String serializedEntry1 = ValueIndexEntryUtils.serializeAsString(entry1);
		ValueIndexEntry parsedEntry1 = ValueIndexEntryUtils.fromString(serializedEntry1);
		
		assertEquals(entry1.getAddress(), parsedEntry1.getAddress());
		assertEquals(entry1.getValue(), parsedEntry1.getValue());
		
		String serializedEntry2 = ValueIndexEntryUtils.serializeAsString(entry2);
		ValueIndexEntry parsedEntry2 = ValueIndexEntryUtils.fromString(serializedEntry2);
		
		assertEquals(entry2.getAddress(), parsedEntry2.getAddress());
		assertEquals(entry2.getValue(), parsedEntry2.getValue());
		
		// test list functionality
		ValueIndexEntry[] entries = new ValueIndexEntry[2];
		entries[0] = entry1;
		entries[1] = entry2;
		
		String serializedArray = ValueIndexEntryUtils.serializeAsString(entries);
		ValueIndexEntry[] parsedEntries = ValueIndexEntryUtils.getArrayFromString(serializedArray);
		
		for(int i = 0; i < entries.length; i++) {
			assertEquals(entries[i].getAddress(), parsedEntries[i].getAddress());
			assertEquals(entries[i].getValue(), parsedEntries[i].getValue());
		}
		
		String s3 = "another \\\\\\\\\" string \\ <entry><\\entry> \" to check the last function \\ \\ \" ";
		
		XValue value3 = X.getValueFactory().createStringValue(s3);
		
		XAddress address3 = XX.resolveField(XX.createUniqueId(), XX.createUniqueId(),
		        XX.createUniqueId(), XX.createUniqueId());
		
		ValueIndexEntry entry3 = new ValueIndexEntry(address3, value3);
		
		serializedArray = ValueIndexEntryUtils.serializeAsString(entries, entry3);
		parsedEntries = ValueIndexEntryUtils.getArrayFromString(serializedArray);
		
		for(int i = 0; i < entries.length; i++) {
			assertEquals(entries[i].getAddress(), parsedEntries[i].getAddress());
			assertEquals(entries[i].getValue(), parsedEntries[i].getValue());
		}
		
		assertEquals(entry3.getAddress(), parsedEntries[2].getAddress());
		assertEquals(entry3.getValue(), parsedEntries[2].getValue());
	}
}
