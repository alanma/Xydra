package org.xydra.core.test.model.state;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.index.IPairIndex;
import org.xydra.index.impl.MapPairIndex;
import org.xydra.index.query.Pair;


public class TestStateTransaction implements Serializable, XStateTransaction {
	
	private static final long serialVersionUID = 2872941127240669775L;
	
	final XAddress base;
	final Set<TestState> saved = new HashSet<TestState>();
	final Set<TestState> deleted = new HashSet<TestState>();
	final Set<TestChangeLogState> savedLogs = new HashSet<TestChangeLogState>();
	final Set<TestChangeLogState> deletedLogs = new HashSet<TestChangeLogState>();
	final IPairIndex<TestChangeLogState,Long> savedEvents = new MapPairIndex<TestChangeLogState,Long>();
	final IPairIndex<TestChangeLogState,Long> deletedEvents = new MapPairIndex<TestChangeLogState,Long>();
	final TestStateStore store;
	boolean committed;
	
	public TestStateTransaction(TestStateStore store, XAddress base) {
		this.store = store;
		this.base = base;
		this.store.allTrans.add(this);
	}
	
	public void commit() {
		
		assert !this.committed : "double commit detected";
		
		for(TestState s : this.saved) {
			assert s.saved : "unsaved changes to entity in transaction";
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.save(s);
		}
		
		for(TestState s : this.deleted) {
			assert s.saved : "unsaved changes to entity in transaction";
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.delete(s);
		}
		
		for(TestChangeLogState s : this.savedLogs) {
			assert s.saved : "unsaved changes to entity in transaction";
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.save(s);
		}
		
		for(TestChangeLogState s : this.deletedLogs) {
			assert s.saved : "unsaved changes to entity in transaction";
			assert s.currentTrans == this;
			s.currentTrans = null;
			this.store.delete(s);
		}
		
		for(Pair<TestChangeLogState,Long> s : this.savedEvents) {
			assert this.savedLogs.contains(s.getFirst())
			        || this.store.logs.containsKey(s.getFirst().getBaseAddress()) : "must save log in same transaction or before adding events";
			this.store.save(s.getFirst(), s.getSecond());
			s.getFirst().currentTrans = null;
		}
		
		for(Pair<TestChangeLogState,Long> s : this.deletedEvents) {
			assert this.deletedLogs.contains(s.getFirst())
			        || this.store.logs.containsKey(s.getFirst().getBaseAddress()) : "must delete or save log in same transaction or before removing events";
			this.store.delete(s.getFirst(), s.getSecond());
			s.getFirst().currentTrans = null;
		}
		
		this.committed = true;
	}
}
