package org.xydra.core.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.value.XV;


public class DiffWritableModelTest {
	
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
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		XWritableObject bertObject = this.diffModel.createObject(bert);
		bertObject.createField(fon).setValue(XV.toValue("456"));
		bertObject.createField(mail).setValue(XV.toValue("b@ex.com"));
		
		List<XAtomicCommand> list = this.diffModel.toCommandList();
		for(XAtomicCommand ac : list) {
			System.out.println(ac);
		}
		assertEquals(10, list.size());
	}
	
	@Test
	public void test2() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		this.diffModel.removeObject(adam);
		
		List<XAtomicCommand> list = this.diffModel.toCommandList();
		assertEquals(0, list.size());
	}
	
	@Test
	public void test3() {
		XWritableObject adamObject = this.diffModel.createObject(adam);
		adamObject.createField(fon).setValue(XV.toValue("123"));
		adamObject.createField(fon).setValue(XV.toValue("1234"));
		adamObject.createField(fon).setValue(XV.toValue("12345"));
		
		List<XAtomicCommand> list = this.diffModel.toCommandList();
		assertEquals(3, list.size());
	}
}
