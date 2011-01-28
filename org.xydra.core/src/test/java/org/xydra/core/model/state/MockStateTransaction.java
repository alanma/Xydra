package org.xydra.core.model.state;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.index.IPairIndex;
import org.xydra.index.impl.MapPairIndex;
import org.xydra.index.query.Pair;


public class MockStateTransaction implements Serializable, XStateTransaction {
	
	private static final long serialVersionUID = 2872941127240669775L;
	
	final XAddress base;
	boolean committed;
	final Set<MockState> deleted = new HashSet<MockState>();
	final IPairIndex<MockChangeLogState,Long> deletedEvents = new MapPairIndex<MockChangeLogState,Long>();
	final Set<MockChangeLogState> deletedLogs = new HashSet<MockChangeLogState>();
	final Set<MockState> saved = new HashSet<MockState>();
	final IPairIndex<MockChangeLogState,Long> savedEvents = new MapPairIndex<MockChangeLogState,Long>();
	final Set<MockChangeLogState> savedLogs = new HashSet<MockChangeLogState>();
	final MockStateStore store;
	
	public MockStateTransaction(MockStateStore store, XAddress base) {
		this.store = store;
		this.base = base;
		this.store.allTrans.add(this);
	}
	
	public void commit() {
		
		assert !this.committed : "double commit detected";
		
		for(MockState s : this.saved) {
			assert s.saved : "unsaved changes to entity in transaction" + s;
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.save(s);
		}
		
		for(MockState s : this.deleted) {
			assert s.saved : "unsaved changes to entity in transaction" + s;
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.delete(s);
		}
		
		for(MockChangeLogState s : this.savedLogs) {
			assert s.saved : "unsaved changes to entity in transaction" + s;
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.save(s);
		}
		
		for(MockChangeLogState s : this.deletedLogs) {
			assert s.saved : "unsaved changes to entity in transaction" + s;
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.delete(s);
		}
		
		for(Pair<MockChangeLogState,Long> s : this.savedEvents) {
			assert this.savedLogs.contains(s.getFirst())
			        || this.store.logs.containsKey(s.getFirst().getBaseAddress()) : "must save log in same transaction or before adding events";
			this.store.save(s.getFirst(), s.getSecond());
			s.getFirst().currentTrans = null;
		}
		
		for(Pair<MockChangeLogState,Long> s : this.deletedEvents) {
			assert this.deletedLogs.contains(s.getFirst())
			        || this.store.logs.containsKey(s.getFirst().getBaseAddress()) : "must delete or save log in same transaction or before removing events";
			this.store.delete(s.getFirst(), s.getSecond());
			s.getFirst().currentTrans = null;
		}
		
		this.committed = true;
	}
}
