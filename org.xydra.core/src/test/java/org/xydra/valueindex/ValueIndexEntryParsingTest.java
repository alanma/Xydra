package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.X;
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
 * @author kaidel
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
		final XId actorId = Base.createUniqueId();

		final XRepository repo = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo);

		this.model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
	}

	/**
	 * Tests whether parsing an {@link ValueIndexEntry} as a String works
	 * correctly.
	 */
	@Test
	public void testParsingEntries() {
		for(final XId objectId : this.model) {
			final XObject object = this.model.getObject(objectId);

			for(final XId fieldId : object) {
				final XField field = object.getField(fieldId);
				final XValue value = field.getValue();

				final ValueIndexEntry entry = new ValueIndexEntry(field.getAddress(), value);

				final String s = ValueIndexEntryUtils.serializeAsString(entry);

				final ValueIndexEntry parsedEntry = ValueIndexEntryUtils.fromString(s);

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
		final ArrayList<ValueIndexEntry> list = new ArrayList<ValueIndexEntry>();

		for(final XId objectId : this.model) {
			final XObject object = this.model.getObject(objectId);

			for(final XId fieldId : object) {
				final XField field = object.getField(fieldId);
				final XValue value = field.getValue();

				final ValueIndexEntry entry = new ValueIndexEntry(field.getAddress(), value);

				list.add(entry);
			}
		}

		final ValueIndexEntry[] array = new ValueIndexEntry[list.size()];
		final String asString = ValueIndexEntryUtils.serializeAsString(list.toArray(array));

		final ValueIndexEntry[] parsedArray = ValueIndexEntryUtils.getArrayFromString(asString);

		assertEquals(array.length, parsedArray.length);

		for(int i = 0; i < array.length; i++) {
			assertEquals(array[i].getAddress(), parsedArray[i].getAddress());
			assertEquals(array[i].getValue(), parsedArray[i].getValue());
		}
	}

	@Test
	public void testParsingSpecialStrings() {
		final String s1 = "\\this\\is<entry>atest<entry> whether \" escaping really works \\ as it <entry> should.";
		final String s2 = "another string containing <entry> the <\\entry> special symbols \" "
		        + "\\\" <entry> \" \" lets see if this works \\\\\\\\\\";

		final XValue value1 = BaseRuntime.getValueFactory().createStringValue(s1);
		final XValue value2 = BaseRuntime.getValueFactory().createStringValue(s2);

		final XAddress address1 = Base.resolveField(Base.createUniqueId(), Base.createUniqueId(),
		        Base.createUniqueId(), Base.createUniqueId());
		final XAddress address2 = Base.resolveField(Base.createUniqueId(), Base.createUniqueId(),
		        Base.createUniqueId(), Base.createUniqueId());

		final ValueIndexEntry entry1 = new ValueIndexEntry(address1, value1);
		final ValueIndexEntry entry2 = new ValueIndexEntry(address2, value2);

		final String serializedEntry1 = ValueIndexEntryUtils.serializeAsString(entry1);
		final ValueIndexEntry parsedEntry1 = ValueIndexEntryUtils.fromString(serializedEntry1);

		assertEquals(entry1.getAddress(), parsedEntry1.getAddress());
		assertEquals(entry1.getValue(), parsedEntry1.getValue());

		final String serializedEntry2 = ValueIndexEntryUtils.serializeAsString(entry2);
		final ValueIndexEntry parsedEntry2 = ValueIndexEntryUtils.fromString(serializedEntry2);

		assertEquals(entry2.getAddress(), parsedEntry2.getAddress());
		assertEquals(entry2.getValue(), parsedEntry2.getValue());

		// test list functionality
		final ValueIndexEntry[] entries = new ValueIndexEntry[2];
		entries[0] = entry1;
		entries[1] = entry2;

		String serializedArray = ValueIndexEntryUtils.serializeAsString(entries);
		ValueIndexEntry[] parsedEntries = ValueIndexEntryUtils.getArrayFromString(serializedArray);

		for(int i = 0; i < entries.length; i++) {
			assertEquals(entries[i].getAddress(), parsedEntries[i].getAddress());
			assertEquals(entries[i].getValue(), parsedEntries[i].getValue());
		}

		final String s3 = "another \\\\\\\\\" string \\ <entry><\\entry> \" to check the last function \\ \\ \" ";

		final XValue value3 = BaseRuntime.getValueFactory().createStringValue(s3);

		final XAddress address3 = Base.resolveField(Base.createUniqueId(), Base.createUniqueId(),
		        Base.createUniqueId(), Base.createUniqueId());

		final ValueIndexEntry entry3 = new ValueIndexEntry(address3, value3);

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
