package org.xydra.core.ext.index;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;
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
		String a = "John Doe";
		String b = "New York";
		String c = "Sven Väth";
		XID id1 = X.getIDProvider().fromString("test-id1");
		XID id2 = X.getIDProvider().fromString("test-id2");
		XID id3 = X.getIDProvider().createUniqueId();
		LinkedList<XValue> list = new LinkedList<XValue>();
		list.addAll(allSingleValueTypes());
		list.add(X.getValueFactory().createBooleanListValue(new boolean[] { true, false, true }));
		list.add(X.getValueFactory().createBinaryValue(new byte[] { 42, 23, 15 }));
		list.add(X.getValueFactory().createDoubleListValue(new double[] { 2.3, 4.2, 1.5 }));
		list.add(X.getValueFactory().createIDListValue(new XID[] { id1, id2, id3 }));
		list.add(X.getValueFactory().createIDSetValue(new XID[] { id1, id2, id3 }));
		list.add(X.getValueFactory().createIntegerListValue(new int[] { 11, 12, 13 }));
		list.add(X.getValueFactory().createLongListValue(new long[] { 1234567890, 12, 13 }));
		list.add(X.getValueFactory().createStringListValue(new String[] { a, b, c }));
		list.add(X.getValueFactory().createStringSetValue(new String[] { a, b, c }));
		return list;
	}
	
	/**
	 * @return each kind of value at least once
	 */
	public static final List<XValue> allSingleValueTypes() {
		String a = "John Doe";
		XID id1 = X.getIDProvider().fromString("test-id1");
		LinkedList<XValue> list = new LinkedList<XValue>();
		list.add(X.getValueFactory().createBooleanValue(true));
		list.add(X.getValueFactory().createDoubleValue(3.1415));
		list.add(id1);
		list.add(X.getValueFactory().createIntegerValue(42));
		list.add(X.getValueFactory().createLongValue(1234567));
		list.add(X.getValueFactory().createStringValue(a));
		return list;
	}
	
	@BeforeClass
	public static void init() {
		LoggerTestHelper.init();
	}
	
	private XID actor;
	private XID emailFieldId;
	private IndexFactoryImpl indexFactory;
	private XObject indexObject;
	private XModel model;
	private XRepository repo;
	
	private XObject user1;
	
	private XObject user2;
	
	private XObject user3;
	
	@Before
	public void before() {
		this.actor = X.getIDProvider().fromString("testIndexXObject");
		this.indexFactory = new IndexFactoryImpl();
		this.repo = X.createMemoryRepository(this.actor);
		this.model = this.repo.createModel(X.getIDProvider().createUniqueId());
		this.indexObject = this.model.createObject(X.getIDProvider().fromString("index-email"));
		
		this.emailFieldId = X.getIDProvider().fromString("email");
		this.user1 = this.model.createObject(X.getIDProvider().fromString("user1"));
		this.user1.createField(this.emailFieldId).setValue(
		        X.getValueFactory().createStringValue("john@doe.com"));
		this.user2 = this.model.createObject(X.getIDProvider().fromString("user2"));
		this.user2.createField(this.emailFieldId).setValue(
		        X.getValueFactory().createStringValue("mary@jane.com"));
		this.user3 = this.model.createObject(X.getIDProvider().fromString("user3"));
		this.user3.createField(this.emailFieldId).setValue(
		        X.getValueFactory().createStringValue("some@one.com"));
		
	}
	
	@Test
	public void testObjectIndex() {
		IObjectIndex oi = this.indexFactory.createObjectIndex(this.emailFieldId, this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		Set<XWritableObject> user3_again_set = oi.lookup(this.model, X.getValueFactory()
		        .createStringValue("some@one.com"));
		assertEquals(1, user3_again_set.size());
		XWritableObject user3_again = user3_again_set.iterator().next();
		assertEquals(this.user3.getId(), user3_again.getId());
		assertEquals(this.user3, user3_again);
	}
	
	@Test
	public void testUniqueObjectIndex() {
		IUniqueObjectIndex oi = this.indexFactory.createUniqueObjectIndex(this.emailFieldId,
		        this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		XWritableObject user3_again = oi.lookup(this.model,
		        X.getValueFactory().createStringValue("some@one.com"));
		assertEquals(this.user3.getId(), user3_again.getId());
		assertEquals(this.user3, user3_again);
	}
	
	@Test
	public void testValueToXID() {
		for(XValue value : allSingleValueTypes()) {
			XID id = ObjectIndex.valueToXID(value);
			id.toString();
		}
	}
	
}
