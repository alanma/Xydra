package org.xydra.core.change;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.rmof.XStateWritableField;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class DiffWritableObjectTest {

	private static final Logger log = LoggerFactory.getLogger(DiffWritableObjectTest.class);

	private SimpleObject base;
	private DiffWritableObject diffObject;

	public static final XId phonebook = XX.toId("phonebook");
	public static final XId adam = XX.toId("adam");
	public static final XId mail = XX.toId("mail");
	public static final XId fon = XX.toId("fon");

	@Before
	public void setUp() throws Exception {
		this.base = new SimpleObject(Base.toAddress(Base.toId("repo1"), phonebook, adam, null));
		this.diffObject = new DiffWritableObject(this.base);
	}

	@Test
	public void test1() {
		final XStateWritableField adamFon = this.diffObject.createField(fon);
		adamFon.setValue(XV.toValue("123"));
		this.diffObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		this.diffObject.createField(fon).setValue(XV.toValue("456"));
		this.diffObject.createField(mail).setValue(XV.toValue("b@ex.com"));

		final List<XAtomicCommand> list = this.diffObject.toCommandList(true);
		for (final XAtomicCommand ac : list) {
			log.debug(ac.toString());
		}
		assertEquals("4 (create 2 fields, set 2 values), list=" + Arrays.toString(list.toArray()),
				4, list.size());
	}

	@Test
	public void test2() {
		this.diffObject.createField(fon).setValue(XV.toValue("123"));
		this.diffObject.createField(mail).setValue(XV.toValue("a@ex.com"));
		this.diffObject.removeField(fon);
		this.diffObject.removeField(mail);

		final List<XAtomicCommand> list = this.diffObject.toCommandList(true);
		assertEquals("do nothing, list=" + Arrays.toString(list.toArray()), 0, list.size());
	}

	@Test
	public void test3() {
		this.diffObject.createField(fon).setValue(XV.toValue("123"));
		this.diffObject.createField(fon).setValue(XV.toValue("1234"));
		this.diffObject.createField(fon).setValue(XV.toValue("12345"));

		final List<XAtomicCommand> list = this.diffObject.toCommandList(true);
		assertEquals("create field, set value. list=" + Arrays.toString(list.toArray()), 2,
				list.size());
	}

	@Test
	public void testDeleteNonexistingField() {
		this.diffObject.removeField(fon);
		final List<XAtomicCommand> list = this.diffObject.toCommandList(true);
		assertEquals("delete 1 field, list=" + Arrays.toString(list.toArray()), 1, list.size());
	}

}
