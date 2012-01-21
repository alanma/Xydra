package org.xydra.valueindex;

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
	private XModelObjectLevelIndex testIndex;
	private XValueIndexer indexer;
	
	public abstract void initializeIndex(XModel model, XValueIndexer indexer);
	
	public abstract void initializeIndexer();
	
	@Before
	public void setup() {
		XID actorId = XX.createUniqueId();
		
		XRepository repo1 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo1);
		
		this.oldModel = repo1.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		XRepository repo2 = X.createMemoryRepository(actorId);
		DemoModelUtil.addPhonebookModel(repo2);
		
		this.newModel = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);
		
		initializeIndexer();
		
		initializeIndex(this.oldModel, this.indexer);
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
				
				List<String> list = this.indexer.getIndexStrings(value);
				
				for(String s : list) {
					List<XAddress> adrList = this.testIndex.search(s);
					
					assertTrue(adrList.contains(object.getAddress()));
				}
			}
		}
	}
}
