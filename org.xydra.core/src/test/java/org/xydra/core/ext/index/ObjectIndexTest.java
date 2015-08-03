package org.xydra.core.ext.index;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.index.impl.memory.AbstractObjectIndex;
import org.xydra.core.index.impl.memory.IndexFactoryImpl;
import org.xydra.core.index.impl.memory.ObjectIndex;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class ObjectIndexTest {

	/**
	 * @return each kind of value at least once
	 */
	public static final List<XValue> allKindsOfValues() {
		final String a = "John Doe";
		final String b = "New York";
		final String c = "Sven Väth";
		final XId id1 = BaseRuntime.getIDProvider().fromString("test-id1");
		final XId id2 = BaseRuntime.getIDProvider().fromString("test-id2");
		final XId id3 = BaseRuntime.getIDProvider().createUniqueId();
		final LinkedList<XValue> list = new LinkedList<XValue>();
		list.addAll(allSingleValueTypes());
		list.add(BaseRuntime.getValueFactory().createBooleanListValue(new boolean[] { true, false, true }));
		list.add(BaseRuntime.getValueFactory().createBinaryValue(new byte[] { 42, 23, 15 }));
		list.add(BaseRuntime.getValueFactory().createDoubleListValue(new double[] { 2.3, 4.2, 1.5 }));
		list.add(BaseRuntime.getValueFactory().createIdListValue(new XId[] { id1, id2, id3 }));
		list.add(BaseRuntime.getValueFactory().createIdSetValue(new XId[] { id1, id2, id3 }));
		list.add(BaseRuntime.getValueFactory().createIntegerListValue(new int[] { 11, 12, 13 }));
		list.add(BaseRuntime.getValueFactory().createLongListValue(new long[] { 1234567890, 12, 13 }));
		list.add(BaseRuntime.getValueFactory().createStringListValue(new String[] { a, b, c }));
		list.add(BaseRuntime.getValueFactory().createStringSetValue(new String[] { a, b, c }));
		return list;
	}

	/**
	 * @return each kind of value at least once
	 */
	public static final List<XValue> allSingleValueTypes() {
		final String a = "John Doe";
		final XId id1 = BaseRuntime.getIDProvider().fromString("test-id1");
		final LinkedList<XValue> list = new LinkedList<XValue>();
		list.add(BaseRuntime.getValueFactory().createBooleanValue(true));
		list.add(BaseRuntime.getValueFactory().createDoubleValue(3.1415));
		list.add(id1);
		list.add(BaseRuntime.getValueFactory().createIntegerValue(42));
		list.add(BaseRuntime.getValueFactory().createLongValue(1234567));
		list.add(BaseRuntime.getValueFactory().createStringValue(a));
		return list;
	}

	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}

	private XId actor;
	private XId emailFieldId;
	private IndexFactoryImpl indexFactory;
	private XObject indexObject;
	private XModel model;
	private XRepository repo;

	private XObject user1;

	private XObject user2;

	private XObject user3;

	@Before
	public void setUp() {
		this.actor = BaseRuntime.getIDProvider().fromString("testIndexXObject");
		this.indexFactory = new IndexFactoryImpl();
		this.repo = X.createMemoryRepository(this.actor);
		this.model = this.repo.createModel(BaseRuntime.getIDProvider().createUniqueId());
		this.indexObject = this.model.createObject(BaseRuntime.getIDProvider().fromString("index-email"));

		this.emailFieldId = BaseRuntime.getIDProvider().fromString("email");
		this.user1 = this.model.createObject(BaseRuntime.getIDProvider().fromString("user1"));
		this.user1.createField(this.emailFieldId).setValue(
		        BaseRuntime.getValueFactory().createStringValue("john@doe.com"));
		this.user2 = this.model.createObject(BaseRuntime.getIDProvider().fromString("user2"));
		this.user2.createField(this.emailFieldId).setValue(
		        BaseRuntime.getValueFactory().createStringValue("mary@jane.com"));
		this.user3 = this.model.createObject(BaseRuntime.getIDProvider().fromString("user3"));
		this.user3.createField(this.emailFieldId).setValue(
		        BaseRuntime.getValueFactory().createStringValue("some@one.com"));

	}

	@Test
	public void testObjectIndex() {
		final IObjectIndex oi = this.indexFactory.createObjectIndex(this.emailFieldId, this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		final Set<XWritableObject> user3_again_set = oi.lookup(this.model, BaseRuntime.getValueFactory()
		        .createStringValue("some@one.com"));
		assertEquals(1, user3_again_set.size());
		final XWritableObject user3_again = user3_again_set.iterator().next();
		assertEquals(this.user3.getId(), user3_again.getId());
		assertEquals(this.user3, user3_again);
	}

	@Test
	public void testUniqueObjectIndex() {
		final IUniqueObjectIndex oi = this.indexFactory.createUniqueObjectIndex(this.emailFieldId,
		        this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		final XWritableObject user3_again = oi.lookup(this.model,
		        BaseRuntime.getValueFactory().createStringValue("some@one.com"));
		assertEquals(this.user3.getId(), user3_again.getId());
		assertEquals(this.user3, user3_again);
	}

	@Test
	public void testValueToXId() {
		for(final XValue value : allSingleValueTypes()) {
			final XId id = AbstractObjectIndex.valueToXId(value);
			id.toString();
		}
	}

}
