package org.xydra.core.test.model.state;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.index.query.KeyKeyEntryTuple;


public class TestChangeLogState implements XChangeLogState {
	
	/** the ID of the model this change log refers to **/
	private XAddress baseAddr;
	
	long first = 0L;
	long last;
	
	int saveCount;
	
	Map<Long,XEvent> events = new HashMap<Long,XEvent>();
	
	public boolean saved;
	
	void load(TestStateStore.Log log, Iterator<KeyKeyEntryTuple<XAddress,Long,XEvent>> it) {
		this.first = log.first;
		this.last = log.last;
		this.events.clear();
		while(it.hasNext()) {
			KeyKeyEntryTuple<XAddress,Long,XEvent> t = it.next();
			this.events.put(t.getKey2(), t.getEntry());
		}
	}
	
	TestStateTransaction currentTrans;
	TestStateStore store;
	
	private static final long serialVersionUID = 4745987477215964499L;
	
	public TestChangeLogState(TestStateStore store, XAddress baseAddr) {
		this.baseAddr = baseAddr;
		this.store = store;
		this.store.allLogs.add(this);
		this.store.loadedLogs.put(baseAddr, this);
	}
	
	void checkActive() {
		assert this.store.loadedLogs.get(this.baseAddr) == this : "newer log state loaded";
	}
	
	TestStateTransaction getTrans(Object trans) {
		if(this.currentTrans != null) {
			assert this.currentTrans == trans : "each entity may only be part of one transaction at a time";
		}
		if(trans == null) {
			return new TestStateTransaction(this.store, this.baseAddr);
		}
		assert trans instanceof TestStateTransaction : "unexpected transaction object";
		TestStateTransaction t = (TestStateTransaction)trans;
		assert t.store == this.store;
		assert XX.equalsOrContains(t.base, this.baseAddr);
		return t;
	}
	
	public void delete(Object trans) {
		checkActive();
		TestStateTransaction t = getTrans(trans);
		this.currentTrans = t;
		this.truncateToRevision(this.first, t);
		t.savedLogs.remove(this);
		t.deletedLogs.add(this);
		this.saved = true;
		if(t != trans) {
			t.commit();
		}
		this.saveCount++;
	}
	
	public void save(Object trans) {
		checkActive();
		TestStateTransaction t = getTrans(trans);
		this.currentTrans = t;
		t.deletedLogs.remove(this);
		t.savedLogs.add(this);
		this.saved = true;
		if(t != trans) {
			t.commit();
		}
		this.saveCount++;
	}
	
	public long getCurrentRevisionNumber() {
		checkActive();
		return this.last;
	}
	
	public XEvent getEvent(long revisionNumber) {
		checkActive();
		return this.events.get(revisionNumber);
	}
	
	public long getFirstRevisionNumber() {
		checkActive();
		return this.first;
	}
	
	long getRevisionForEvent(XEvent event) {
		if(event == null) {
			return getCurrentRevisionNumber();
		}
		long rev = this.baseAddr.getObject() == null ? event.getModelRevisionNumber() : event
		        .getObjectRevisionNumber();
		assert rev >= 0;
		return rev;
	}
	
	public void appendEvent(XEvent event, Object transaction) {
		checkActive();
		
		assert getRevisionForEvent(event) == this.last;
		
		long i = this.last;
		
		this.saved = false;
		this.last++;
		
		if(event == null) {
			return;
		}
		
		TestStateTransaction t = getTrans(transaction);
		this.currentTrans = t;
		
		this.events.put(i, event);
		t.deletedEvents.deIndex(this, i);
		t.savedEvents.index(this, i);
		
		if(t != transaction) {
			t.commit();
		}
		
	}
	
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	public boolean truncateToRevision(long revisionNumber, Object transaction) {
		checkActive();
		if(revisionNumber > getCurrentRevisionNumber()) {
			return false;
		}
		
		if(revisionNumber < this.first) {
			return false;
		}
		
		TestStateTransaction t = getTrans(transaction);
		this.currentTrans = t;
		
		this.saved = false;
		while(revisionNumber < this.last) {
			this.last--;
			this.events.remove(this.last); // remove last element
			t.savedEvents.deIndex(this, this.last);
			t.deletedEvents.index(this, this.last);
		}
		
		if(t != transaction) {
			t.commit();
		}
		
		assert revisionNumber == this.last;
		
		return true;
	}
	
	public void setFirstRevisionNumber(long rev) {
		if(!this.events.isEmpty()) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.first = rev;
	}
	
}
