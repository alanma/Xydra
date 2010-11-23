package org.xydra.core.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.test.model.state.TestStateStore;
import org.xydra.core.value.XV;


/**
 * Test that each XModel operation is mapped to a single state transaction.
 * 
 * @author dscharrer
 * 
 */
public class StrictStateTest {
	
	static TestStateStore store;
	static XRepository repo;
	static XModel model;
	static XObject object;
	static XField field;
	static XID actorId;
	
	@BeforeClass
	public static void init() {
		store = new TestStateStore();
		XSPI.setStateStore(store);
		actorId = XX.toId("StrictStateTest");
		repo = X.createMemoryRepository(actorId);
	}
	
	@Before
	public void setUp() {
		model = repo.createModel(XX.createUniqueID());
		object = model.createObject(XX.createUniqueID());
		field = object.createField(XX.createUniqueID());
		field.setValue(XV.toValue("Cookie Monster"));
		try {
			store.checkConsistency();
		} finally {
			store.resetTrans();
			store.resetSaves();
		}
	}
	
	@After
	public void tearDown() {
		try {
			store.checkConsistency();
			store.checkOnlySavedOnce();
			assert store.getTransCount() == 1;
		} finally {
			store.resetTrans();
			store.resetSaves();
			Set<XID> s = new HashSet<XID>();
			for(XID id : repo) {
				s.add(id);
			}
			for(XID id : s) {
				repo.removeModel(id);
			}
		}
	}
	
	@Test
	public void testCreateModel() {
		repo.createModel(XX.createUniqueID());
	}
	
	@Test
	public void testCreateObject() {
		model.createObject(XX.createUniqueID());
	}
	
	@Test
	public void testCreateField() {
		object.createField(XX.createUniqueID());
	}
	
	@Test
	public void testSetValue() {
		field.setValue(XV.toValue(new byte[] { 'C', 'O', 'O', 'K', 'I', 'E', '!' }));
	}
	
	@Test
	public void testRemoveValue() {
		field.setValue(null);
	}
	
	@Test
	public void testRemoveField() {
		object.removeField(field.getID());
	}
	
	@Test
	public void testReoveObject() {
		model.removeObject(object.getID());
	}
	
	@Test
	public void testRemoveModel() {
		repo.removeModel(model.getID());
	}
	
	@Test
	public void testTransaction() {
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		
		tb.removeObject(object);
		
		tb.addObject(model.getAddress(), XCommand.SAFE, XX.createUniqueID());
		
		model.executeTransaction(tb.build());
		
	}
	
}
