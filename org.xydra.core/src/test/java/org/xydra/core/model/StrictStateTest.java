package org.xydra.core.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.test.model.state.TestStateStore;


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
	
	@BeforeClass
	public static void init() {
		store = new TestStateStore();
		XSPI.setStateStore(store);
		repo = X.createMemoryRepository();
	}
	
	@Before
	public void setUp() {
		model = repo.createModel(null, X.getIDProvider().createUniqueID());
		object = model.createObject(null, X.getIDProvider().createUniqueID());
		field = object.createField(null, X.getIDProvider().createUniqueID());
		field.setValue(null, X.getValueFactory().createStringValue("Cookie Monster"));
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
				repo.removeModel(null, id);
			}
		}
	}
	
	@Test
	public void testCreateModel() {
		repo.createModel(null, X.getIDProvider().createUniqueID());
	}
	
	@Test
	public void testCreateObject() {
		model.createObject(null, X.getIDProvider().createUniqueID());
	}
	
	@Test
	public void testCreateField() {
		object.createField(null, X.getIDProvider().createUniqueID());
	}
	
	@Test
	public void testSetValue() {
		field.setValue(null, X.getValueFactory().createByteListValue(
		        new byte[] { 'C', 'O', 'O', 'K', 'I', 'E', '!' }));
	}
	
	@Test
	public void testRemoveValue() {
		field.setValue(null, null);
	}
	
	@Test
	public void testRemoveField() {
		object.removeField(null, field.getID());
	}
	
	@Test
	public void testReoveObject() {
		model.removeObject(null, object.getID());
	}
	
	@Test
	public void testRemoveModel() {
		repo.removeModel(null, model.getID());
	}
	
	@Test
	public void testTransaction() {
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		
		tb.removeObject(object);
		
		tb.addObject(model.getAddress(), XCommand.SAFE, X.getIDProvider().createUniqueID());
		
		model.executeTransaction(null, tb.build());
		
	}
	
}
