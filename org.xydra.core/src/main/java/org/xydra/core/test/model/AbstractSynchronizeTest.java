package org.xydra.core.test.model;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.test.DemoModelUtil;


public class AbstractSynchronizeTest extends TestCase {
	
	private static final XID ACTOR_ID = X.getIDProvider().fromString("tester");
	
	private XRepository localRepo;
	private XModel remoteModel;
	private XModel localModel;
	
	@Override
	@Before
	public void setUp() {
		
		this.localRepo = X.createMemoryRepository();
		
		assertFalse(this.localRepo.hasModel(DemoModelUtil.PHONEBOOK_ID));
		
		// create two identical phonebook models
		this.remoteModel = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(this.remoteModel);
		DemoModelUtil.addPhonebookModel(this.localRepo);
		this.localModel = this.localRepo.getModel(DemoModelUtil.PHONEBOOK_ID);
		assertNotNull(this.localModel);
		
		assertTrue(XX.equalState(this.localModel, this.remoteModel));
		
	}
	
	@Override
	@After
	public void tearDown() {
		this.localRepo.removeModel(ACTOR_ID, DemoModelUtil.PHONEBOOK_ID);
	}
	
	@Test
	public void testModelRollback() {
		
		// make some additional changes to the local model
		assertNotNull(this.localModel.createObject(ACTOR_ID, X.getIDProvider().createUniqueID()));
		
		assertTrue(this.localModel.removeObject(ACTOR_ID, DemoModelUtil.JOHN_ID));
		
		assertNotNull(this.localModel.getObject(DemoModelUtil.PETER_ID).createField(ACTOR_ID,
		        X.getIDProvider().createUniqueID()));
		
		XTransactionBuilder tb = new XTransactionBuilder(this.localModel.getAddress());
		XID objId = X.getIDProvider().createUniqueID();
		tb.addObject(this.localModel.getAddress(), XCommand.SAFE, objId);
		XAddress objAddr = XX.resolveObject(this.localModel.getAddress(), objId);
		tb.addField(objAddr, XCommand.SAFE, X.getIDProvider().createUniqueID());
		assertTrue(this.localModel.executeTransaction(ACTOR_ID, tb.build()) >= 0);
		
		assertTrue(this.localModel.removeObject(ACTOR_ID, DemoModelUtil.CLAUDIA_ID));
		
		// now rollback the second model and compare the models
		this.localModel.rollback(this.remoteModel.getRevisionNumber());
		
		assertEquals(this.remoteModel.getRevisionNumber(), this.localModel.getRevisionNumber());
		
		assertTrue(XX.equalState(this.remoteModel, this.localModel));
		
	}
	
	@Test
	public void testModelSynchronize() {
		
		// create two identical phonebook models
		XModel model1 = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		XModel model2 = new MemoryModel(DemoModelUtil.PHONEBOOK_ID);
		DemoModelUtil.setupPhonebook(model1);
		DemoModelUtil.setupPhonebook(model2);
		
		// make some additional changes to the second model and record them
		
		// TODO implement
		
	}
	
}
