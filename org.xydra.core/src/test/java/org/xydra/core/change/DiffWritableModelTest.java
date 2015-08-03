package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XStateWritableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class DiffWritableModelTest {

	private static final Logger log = LoggerFactory.getLogger(DiffWritableModelTest.class);

	private SimpleModel base;
	private DiffWritableModel diffModel;

	public static final XId phonebook = XX.toId("phonebook");
	public static final XId adam = XX.toId("adam");
	public static final XId bert = XX.toId("bert");
	public static final XId mail = XX.toId("mail");
	public static final XId fon = XX.toId("fon");

	@Before
	public void setUp() throws Exception {
		this.base = new SimpleModel(Base.toAddress(Base.toId("repo1"), phonebook, null, null));
		this.diffModel = new DiffWritableModel(this.base);
	}

	@Test
	public void test1() {
		final XStateWritableObject adamObject = this.diffModel.createObject(adam);
		assertTrue(this.diffModel.hasObject(adam));
		final XStateWritableField adamFon = adamObject.createField(fon);
		adamFon.setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		final XStateWritableObject bertObject = this.diffModel.createObject(bert);
		bertObject.createField(fon).setValue(XV.toValue("456"));
		bertObject.createField(mail).setValue(XV.toValue("b@ex.com"));

		final List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		for(final XAtomicCommand ac : list) {
			log.debug(ac.toString());
		}
		assertEquals(10, list.size());
	}

	@Test
	public void test2() {
		final XStateWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		this.diffModel.removeObject(adam);

		final List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(0, list.size());
	}

	@Test
	public void test3() {
		final XStateWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(fon).setValue(XV.toValue("1234"));
		adamObject.createField(fon).setValue(XV.toValue("12345"));

		final List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(3, list.size());
	}

	@Test
	public void testDeleteNonexistingField() {
		final XStateWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.removeField(fon);
		final List<XAtomicCommand> list = this.diffModel.toCommandList(true);
		assertEquals(1, list.size());
	}

	@Test
	public void testDiffWritableRepositoryOnBase() {
		// create repo with minimal content
		final SimpleRepository srepo = new SimpleRepository(Base.toAddress("/srepo/-/-/-"));
		srepo.createModel(Base.toId("smodel")).createObject(Base.toId("sobject"))
		        .createField(Base.toId("sfield")).setValue(XV.toValue("svalue"));
		// verify simpemodel
		assertTrue(srepo.getModel(Base.toId("smodel")).hasObject(Base.toId("sobject")));

		final DiffWritableRepository drepo = new DiffWritableRepository(srepo);
		// add some content here
		final XStateWritableModel dmodel = drepo.createModel(Base.toId("dmodel"));
		dmodel.createObject(Base.toId("dobject")).createField(Base.toId("dfield"))
		        .setValue(XV.toValue("dvalue"));

		// check txn & content
		assertTrue(drepo.hasModel(Base.toId("smodel")));
		assertTrue(drepo.hasModel(Base.toId("dmodel")));
		final XStateWritableModel smodel = drepo.getModel(Base.toId("smodel"));
		assertTrue(smodel.hasObject(Base.toId("sobject")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).hasObject(Base.toId("dobject")));
		assertTrue(drepo.getModel(Base.toId("smodel")).getObject(Base.toId("sobject"))
		        .hasField(Base.toId("sfield")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).getObject(Base.toId("dobject"))
		        .hasField(Base.toId("dfield")));
		assertTrue(drepo.getModel(Base.toId("smodel")).getObject(Base.toId("sobject"))
		        .getField(Base.toId("sfield")).getValue().equals(XV.toValue("svalue")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).getObject(Base.toId("dobject"))
		        .getField(Base.toId("dfield")).getValue().equals(XV.toValue("dvalue")));
		final DiffWritableModel diffModel = (DiffWritableModel)drepo.getModel(Base.toId("dmodel"));
		final List<XAtomicCommand> list = diffModel.toCommandList(true);
		assertEquals("add dobject, dfield, dvalue", 3, list.size());
	}

	@Test
	public void testChangedRepositoryOnBase() {
		// create repo with minimal content
		final SimpleRepository srepo = new SimpleRepository(Base.toAddress("/srepo/-/-/-"));
		srepo.createModel(Base.toId("smodel")).createObject(Base.toId("sobject"))
		        .createField(Base.toId("sfield")).setValue(XV.toValue("svalue"));
		// verify simpemodel
		assertTrue(srepo.getModel(Base.toId("smodel")).hasObject(Base.toId("sobject")));

		final ChangedRepository drepo = new ChangedRepository(srepo);
		// add some content here
		final XWritableModel dmodel = drepo.createModel(Base.toId("dmodel"));
		dmodel.createObject(Base.toId("dobject")).createField(Base.toId("dfield"))
		        .setValue(XV.toValue("dvalue"));

		// check txn & content
		assertTrue(drepo.hasModel(Base.toId("smodel")));
		assertTrue(drepo.hasModel(Base.toId("dmodel")));
		final XWritableModel smodel = drepo.getModel(Base.toId("smodel"));
		assertTrue(smodel.hasObject(Base.toId("sobject")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).hasObject(Base.toId("dobject")));
		assertTrue(drepo.getModel(Base.toId("smodel")).getObject(Base.toId("sobject"))
		        .hasField(Base.toId("sfield")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).getObject(Base.toId("dobject"))
		        .hasField(Base.toId("dfield")));
		assertTrue(drepo.getModel(Base.toId("smodel")).getObject(Base.toId("sobject"))
		        .getField(Base.toId("sfield")).getValue().equals(XV.toValue("svalue")));
		assertTrue(drepo.getModel(Base.toId("dmodel")).getObject(Base.toId("dobject"))
		        .getField(Base.toId("dfield")).getValue().equals(XV.toValue("dvalue")));
		final ChangedModel diffModel = (ChangedModel)drepo.getModel(Base.toId("dmodel"));

		final XTransactionBuilder xtb = new XTransactionBuilder(diffModel.getAddress());
		xtb.applyChanges(diffModel);
		final XTransaction txn = xtb.build();
		final List<XAtomicCommand> list = new ArrayList<XAtomicCommand>();
		for(final XAtomicCommand xc : txn) {
			list.add(xc);
		}
		assertEquals("add dobject, dfield, dvalue", 3, list.size());
	}
}
