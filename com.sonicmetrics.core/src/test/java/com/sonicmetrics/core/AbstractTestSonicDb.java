package com.sonicmetrics.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import com.sonicmetrics.core.shared.ISonicDB;
import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent;
import com.sonicmetrics.core.shared.query.ISonicQuery;
import com.sonicmetrics.core.shared.query.SonicQuery;
import com.sonicmetrics.core.shared.query.TimeConstraint;


/**
 * SUbclasses must set {@link #db}
 * 
 * @author xamde
 * 
 */
public abstract class AbstractTestSonicDb {
	
	protected ISonicDB db = null;
	
	@Test
	public void testPutAndGet() {
		assertTrue(dbIsEmpty());
		
		ISonicEvent se1 = SonicEvent.create(120).subject("@xamde").category("cat1").action("act")
		        .label("lab").source("test").done();
		System.out.println("Adding " + se1);
		this.db.receiveEvent(se1);
		assertFalse(dbIsEmpty());
		
		ISonicEvent se2 = SonicEvent.create(120).subject("@xamde").category("cat2").action("act")
		        .label("lab").source("test").done();
		ISonicEvent se3 = SonicEvent.create(120).subject("@xamde").category("cat3").action("act")
		        .label("lab").source("test").done();
		ISonicEvent se4 = SonicEvent.create(120).subject("@xamde").category("cat4").action("act")
		        .label("lab").source("test").done();
		ISonicEvent se5 = SonicEvent.create(120).subject("@xamde").category("cat5").action("act")
		        .label("lab").source("test").done();
		this.db.receiveEvent(se2);
		this.db.receiveEvent(se3);
		this.db.receiveEvent(se4);
		this.db.receiveEvent(se5);
		
		ISonicQuery sq = SonicQuery.build(TimeConstraint.fromTo(120, 180)).done();
		System.out.println("Asking for " + sq);
		Iterator<? extends ISonicEvent> it = this.db.query(sq).iterator();
		
		HashSet<ISonicEvent> set = new HashSet<ISonicEvent>();
		while(it.hasNext()) {
			ISonicEvent se = it.next();
			System.out.println(se);
			set.add(se);
		}
		assertEquals(5, set.size());
		assertTrue("Set did not contain " + se1, set.contains(se1));
		assertTrue(set.contains(se2));
		assertTrue(set.contains(se3));
		assertTrue(set.contains(se4));
		assertTrue(set.contains(se5));
	}
	
	@Test
	public void testSendEventViaRest() {
		@SuppressWarnings("unused")
		String url = "http://localhost:8765/post?callback=jQuery17105713681816123426_1340556402183&username=SonicMetrics&password=SonicMetrics&subject=nerdwg&category=wg&action=shopping&label=Vegetables&source=frontend-1.0&_=1340556416195";
	}
	
	@Test
	public void testUniqeId() {
		dbClear();
		assertTrue(dbIsEmpty());
		
		ISonicEvent se1 = SonicEvent.create(120).subject("@xamde").category("cat1").action("act")
		        .label("lab").source("test").uniqueId("abc").done();
		this.db.receiveEvent(se1);
		ISonicEvent se2 = SonicEvent.create(120).subject("@xamde").category("cat2").action("act")
		        .label("lab").source("test").done();
		this.db.receiveEvent(se2);
		
		assertEquals(2, getDbSize());
		
		@SuppressWarnings("unused")
		ISonicEvent se3 = SonicEvent.create(120).subject("@xamde").category("cat3").action("act")
		        .label("lab").source("test").uniqueId("abc").done();
		
		assertEquals(2, getDbSize());
	}
	
	private void dbClear() {
		this.db.delete(SonicQuery.build(TimeConstraint.ALL_UNTIL_NOW).done());
	}
	
	/**
	 * @return a maximum of 100
	 */
	private int getDbSize() {
		return org.apache.commons.collections.IteratorUtils.toList(
		        this.db.query(SonicQuery.build(TimeConstraint.ALL_UNTIL_NOW).limit(100).done())
		                .iterator()).size();
	}
	
	private boolean dbIsEmpty() {
		return !this.db.query(SonicQuery.build(TimeConstraint.ALL_UNTIL_NOW).limit(2).done())
		        .iterator().hasNext();
	}
}
