package com.sonicmetrics.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.impl.log4j.Log4jLoggerFactory;

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
    
    static {
        LoggerFactory.setLoggerFactorySPI(new Log4jLoggerFactory(), "SomeTest");
    }
    
    @Test
    public void testHashcodeAndEquals() {
        ISonicEvent se1a = SonicEvent.create(120).subject("@xamde").category("cat1").action("act")
                .label("lab").source("test").build();
        
        ISonicEvent se1b = SonicEvent.create(120).subject("@xamde").category("cat1").action("act")
                .label("lab").source("test").build();
        
        assertTrue(se1a.equals(se1b));
        assertEquals(se1a.hashCode(), se1b.hashCode());
    }
    
    @Test
    public void testPutAndGet() {
        assertTrue(dbIsEmpty());
        
        ISonicEvent se1 = SonicEvent.create(120).subject("@xamde").category("cat1").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se1);
        assertFalse(dbIsEmpty());
        
        ISonicEvent se2 = SonicEvent.create(120).subject("@xamde").category("cat2").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se2);
        ISonicEvent se3 = SonicEvent.create(120).subject("@xamde").category("cat3").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se3);
        ISonicEvent se4 = SonicEvent.create(120).subject("@xamde").category("cat4").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se4);
        ISonicEvent se5 = SonicEvent.create(120).subject("@xamde").category("cat5").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se5);
        
        ISonicQuery sq = SonicQuery.build(TimeConstraint.fromTo(120, 180)).done();
        Iterator<? extends ISonicEvent> it = this.db.query(sq).iterator();
        
        HashSet<ISonicEvent> set = new HashSet<ISonicEvent>();
        while(it.hasNext()) {
            ISonicEvent se = it.next();
            System.out.println(se + " equal to se1?" + se.equals(se1));
            set.add(se);
        }
        assertEquals(5, set.size());
        /*
         * it does <em>not</em> hold that the set contains the original events.
         * See definition of equals in SonicEvent.
         */
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
                .label("lab").source("test").uniqueId("abc").build();
        this.db.receiveEvent(se1);
        ISonicEvent se2 = SonicEvent.create(120).subject("@xamde").category("cat2").action("act")
                .label("lab").source("test").build();
        this.db.receiveEvent(se2);
        
        assertEquals(2, getDbSize());
        
        @SuppressWarnings("unused")
        ISonicEvent se3 = SonicEvent.create(120).subject("@xamde").category("cat3").action("act")
                .label("lab").source("test").uniqueId("abc").build();
        
        assertEquals(2, getDbSize());
    }
    
    protected void dbClear() {
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
