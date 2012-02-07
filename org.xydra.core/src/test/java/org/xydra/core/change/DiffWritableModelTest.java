package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XV;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class DiffWritableModelTest {
	
	private static final Logger log = LoggerFactory.getLogger(DiffWritableModelTest.class);
	
	private SimpleModel base;
	private DiffWritableModel diffModel;
	
	public static final XID phonebook = XX.toId("phonebook");
	public static final XID adam = XX.toId("adam");
	public static final XID bert = XX.toId("bert");
	public static final XID mail = XX.toId("mail");
	public static final XID fon = XX.toId("fon");
	
	@Before
	public void setUp() throws Exception {
		this.base = new SimpleModel(XX.toAddress(XX.toId("repo1"), phonebook, null, null));
		this.diffModel = new DiffWritableModel(this.base);
	}
	
	@Test
	public void test1() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		assertTrue(this.diffModel.hasObject(adam));
		XWritableField adamFon = adamObject.createField(fon);
		adamFon.setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		XWritableObject bertObject = this.diffModel.createObject(bert);
		bertObject.createField(fon).setValue(XV.toValue("456"));
		bertObject.createField(mail).setValue(XV.toValue("b@ex.com"));
		
		List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		for(XAtomicCommand ac : list) {
			log.debug(ac.toString());
		}
		assertEquals(10, list.size());
	}
	
	@Test
	public void test2() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		this.diffModel.removeObject(adam);
		
		List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(0, list.size());
	}
	
	@Test
	public void test3() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(fon).setValue(XV.toValue("1234"));
		adamObject.createField(fon).setValue(XV.toValue("12345"));
		
		List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(3, list.size());
	}
	
	@Test
	public void testDeleteNonexistingField() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.removeField(fon);
		List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(1, list.size());
	}
	
	@Test
	public void testDiffWritableRepositoryOnBase() {
		// create repo with minimal content
		SimpleRepository srepo = new SimpleRepository(XX.toAddress("/srepo/-/-/-"));
		srepo.createModel(XX.toId("smodel")).createObject(XX.toId("sobject"))
		        .createField(XX.toId("sfield")).setValue(XV.toValue("svalue"));
		// verify simpemodel
		assertTrue(srepo.getModel(XX.toId("smodel")).hasObject(XX.toId("sobject")));
		
		DiffWritableRepository drepo = new DiffWritableRepository(srepo);
		// add some content here
		XWritableModel dmodel = drepo.createModel(XX.toId("dmodel"));
		dmodel.createObject(XX.toId("dobject")).createField(XX.toId("dfield"))
		        .setValue(XV.toValue("dvalue"));
		
		// check txn & content
		assertTrue(drepo.hasModel(XX.toId("smodel")));
		assertTrue(drepo.hasModel(XX.toId("dmodel")));
		XWritableModel smodel = drepo.getModel(XX.toId("smodel"));
		assertTrue(smodel.hasObject(XX.toId("sobject")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).hasObject(XX.toId("dobject")));
		assertTrue(drepo.getModel(XX.toId("smodel")).getObject(XX.toId("sobject"))
		        .hasField(XX.toId("sfield")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).getObject(XX.toId("dobject"))
		        .hasField(XX.toId("dfield")));
		assertTrue(drepo.getModel(XX.toId("smodel")).getObject(XX.toId("sobject"))
		        .getField(XX.toId("sfield")).getValue().equals(XV.toValue("svalue")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).getObject(XX.toId("dobject"))
		        .getField(XX.toId("dfield")).getValue().equals(XV.toValue("dvalue")));
		DiffWritableModel diffModel = (DiffWritableModel)drepo.getModel(XX.toId("dmodel"));
		List<XAtomicCommand> list = diffModel.toCommandList(true);
		assertEquals("add dobject, dfield, dvalue", 3, list.size());
	}
	
	@Test
	public void testChangedRepositoryOnBase() {
		// create repo with minimal content
		SimpleRepository srepo = new SimpleRepository(XX.toAddress("/srepo/-/-/-"));
		srepo.createModel(XX.toId("smodel")).createObject(XX.toId("sobject"))
		        .createField(XX.toId("sfield")).setValue(XV.toValue("svalue"));
		// verify simpemodel
		assertTrue(srepo.getModel(XX.toId("smodel")).hasObject(XX.toId("sobject")));
		
		ChangedRepository drepo = new ChangedRepository(srepo);
		// add some content here
		XWritableModel dmodel = drepo.createModel(XX.toId("dmodel"));
		dmodel.createObject(XX.toId("dobject")).createField(XX.toId("dfield"))
		        .setValue(XV.toValue("dvalue"));
		
		// check txn & content
		assertTrue(drepo.hasModel(XX.toId("smodel")));
		assertTrue(drepo.hasModel(XX.toId("dmodel")));
		XWritableModel smodel = drepo.getModel(XX.toId("smodel"));
		assertTrue(smodel.hasObject(XX.toId("sobject")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).hasObject(XX.toId("dobject")));
		assertTrue(drepo.getModel(XX.toId("smodel")).getObject(XX.toId("sobject"))
		        .hasField(XX.toId("sfield")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).getObject(XX.toId("dobject"))
		        .hasField(XX.toId("dfield")));
		assertTrue(drepo.getModel(XX.toId("smodel")).getObject(XX.toId("sobject"))
		        .getField(XX.toId("sfield")).getValue().equals(XV.toValue("svalue")));
		assertTrue(drepo.getModel(XX.toId("dmodel")).getObject(XX.toId("dobject"))
		        .getField(XX.toId("dfield")).getValue().equals(XV.toValue("dvalue")));
		ChangedModel diffModel = (ChangedModel)drepo.getModel(XX.toId("dmodel"));
		
		XTransactionBuilder xtb = new XTransactionBuilder(diffModel.getAddress());
		xtb.applyChanges(diffModel);
		XTransaction txn = xtb.build();
		List<XAtomicCommand> list = new ArrayList<XAtomicCommand>();
		for(XAtomicCommand xc : txn) {
			list.add(xc);
		}
		assertEquals("add dobject, dfield, dvalue", 3, list.size());
	}
}
