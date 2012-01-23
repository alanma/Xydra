package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public abstract class XModelObjectLevelIndexTest {
	
	/**
	 * The basic model, can be used to get the "old" objects etc. to test
	 * updateIndex methods
	 */
	private XModel oldModel;
	
	private XModel newModel;
	
	/*
	 * TODO initialize the index and indexer!
	 */
	private XModelObjectLevelIndex oldIndex;
	private XModelObjectLevelIndex newIndex;
	private XValueIndexer oldIndexer;
	private XValueIndexer newIndexer;
	
	public abstract void initializeIndex(XModel model, XValueIndexer indexer);
	
	public abstract void initializeIndexers();
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo1 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo1);
		
		this.oldModel = repo1.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo2 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo2);
		
		this.newModel = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		initializeIndexers();
		
		initializeIndex(this.oldModel, this.oldIndexer);
		initializeIndex(this.newModel, this.newIndexer);
	}
	
	@Test
	public void testIndexingXModel() {
		/*
		 * The model was already indexed during the setup() method
		 */

		for(XID objectId : this.oldModel) {
			XObject object = this.oldModel.getObject(objectId);
			
			for(XID fieldId : object) {
				XField field = object.getField(fieldId);
				XValue value = field.getValue();
				
				List<String> list = this.oldIndexer.getIndexStrings(value);
				
				for(String s : list) {
					List<XAddress> adrList = this.oldIndex.search(s);
					
					assertTrue(adrList.contains(object.getAddress()));
				}
			}
		}
	}
	
	@Test
	public void testUpdateIndexObjectAddValue() {
		String valueString = "This is a string which shouldn't exist as value in the oldModel";
		XValue value = X.getValueFactory().createStringValue(valueString);
		
		// check that no entry for valueString exists in the old index
		List<XAddress> oldList = this.oldIndex.search(valueString);
		assertTrue(oldList.isEmpty());
		
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// add the new value, update index and check whether an entry exists or
		// not
		XID id = XX.createUniqueId();
		XField newField = newJohn.createField(id);
		newField.setValue(value);
		
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		List<XAddress> newList = this.newIndex.search(valueString);
		assertEquals(1, newList.size());
		assertEquals(newJohn.getAddress(), newList.get(0));
	}
	
	@Test
	public void testUpdateIndexObjectDeleteValue() {
		XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
		XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
		
		// Remove an existing value, update index, and check whether no entry
		// exists any more or not
		XField titleField = newJohn.getField(DemoModelUtil.TITLE_ID);
		XValue value = titleField.getValue();
		List<String> indexStrings = this.newIndexer.getIndexStrings(value);
		
		titleField.setValue(null);
		this.newIndex.updateIndex(oldJohn, newJohn);
		
		for(String s : indexStrings) {
			List<XAddress> found = this.newIndex.search(s);
			assertTrue(found.isEmpty());
		}
	}
	
	// TODO write test for deleting a value which exists multiple times
}
