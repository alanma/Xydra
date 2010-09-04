package org.xydra.core.ext.index;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.index.IObjectIndex;
import org.xydra.core.index.IUniqueObjectIndex;
import org.xydra.core.index.impl.memory.IndexFactoryImpl;
import org.xydra.core.index.impl.memory.ObjectIndex;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XValue;


public class TestObjectIndex {
	
	private XRepository repo;
	private XID actor;
	private XModel model;
	private XObject indexObject;
	private XID emailFieldId;
	private XObject user1;
	private XObject user2;
	private XObject user3;
	private IndexFactoryImpl indexFactory;
	
	@Before
	public void before() {
		this.indexFactory = new IndexFactoryImpl();
		this.repo = X.createMemoryRepository();
		this.actor = X.getIDProvider().fromString("testIndexXObject");
		this.model = this.repo.createModel(this.actor, X.getIDProvider().createUniqueID());
		this.indexObject = this.model.createObject(this.actor, X.getIDProvider().fromString(
		        "index-email"));
		
		this.emailFieldId = X.getIDProvider().fromString("email");
		this.user1 = this.model.createObject(this.actor, X.getIDProvider().fromString("user1"));
		this.user1.createField(this.actor, this.emailFieldId).setValue(this.actor,
		        X.getValueFactory().createStringValue("john@doe.com"));
		this.user2 = this.model.createObject(this.actor, X.getIDProvider().fromString("user2"));
		this.user2.createField(this.actor, this.emailFieldId).setValue(this.actor,
		        X.getValueFactory().createStringValue("mary@jane.com"));
		this.user3 = this.model.createObject(this.actor, X.getIDProvider().fromString("user3"));
		this.user3.createField(this.actor, this.emailFieldId).setValue(this.actor,
		        X.getValueFactory().createStringValue("some@one.com"));
		
	}
	
	@Test
	public void testObjectIndex() {
		IObjectIndex oi = this.indexFactory.createObjectIndex(this.emailFieldId, this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		Set<XObject> user3_again_set = oi.lookup(this.model, X.getValueFactory().createStringValue(
		        "some@one.com"));
		assertEquals(1, user3_again_set.size());
		XObject user3_again = user3_again_set.iterator().next();
		assertEquals(this.user3.getID(), user3_again.getID());
		assertEquals(this.user3, user3_again);
	}
	
	@Test
	public void testUniqueObjectIndex() {
		IUniqueObjectIndex oi = this.indexFactory.createUniqueObjectIndex(this.emailFieldId,
		        this.indexObject);
		oi.index(this.user1);
		oi.index(this.user2);
		oi.index(this.user3);
		XObject user3_again = oi.lookup(this.model, X.getValueFactory().createStringValue(
		        "some@one.com"));
		assertEquals(this.user3.getID(), user3_again.getID());
		assertEquals(this.user3, user3_again);
	}
	
	@Test
	public void testValueToXID() {
		for(XValue value : allSingleValueTypes()) {
			XID id = ObjectIndex.valueToXID(value);
			id.toURI();
		}
	}
	
	/**
	 * @return each kind of value at least once
	 */
	public static final List<XValue> allKindsOfValues() {
		String a = "John Doe";
		String b = "New York";
		String c = "Sven Väth";
		XID id1 = X.getIDProvider().fromString("test-id1");
		XID id2 = X.getIDProvider().fromString("test-id2");
		XID id3 = X.getIDProvider().createUniqueID();
		LinkedList<XValue> list = new LinkedList<XValue>();
		list.addAll(allSingleValueTypes());
		list.add(X.getValueFactory().createBooleanListValue(new boolean[] { true, false, true }));
		list.add(X.getValueFactory().createByteListValue(new byte[] { 42, 23, 15 }));
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
		list.add(X.getValueFactory().createIDValue(id1));
		list.add(X.getValueFactory().createIntegerValue(42));
		list.add(X.getValueFactory().createLongValue(1234567));
		list.add(X.getValueFactory().createStringValue(a));
		return list;
	}
	
}
