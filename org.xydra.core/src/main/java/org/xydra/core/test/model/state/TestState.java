package org.xydra.core.test.model.state;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;
import org.xydra.index.XI;


public abstract class TestState {
	
	TestStateStore store;
	XAddress address;
	int saveCount;
	long revision;
	Set<XID> children = new HashSet<XID>();
	XValue value;
	TestStateTransaction currentTrans;
	
	boolean saved;
	
	public TestState(TestStateStore store, XAddress address) {
		this.address = address;
		this.store = store;
		this.store.allStates.add(this);
		this.store.loadedStates.put(address, this);
		assert getID() != null;
	}
	
	void checkActive() {
		assert this.store.loadedStates.get(this.address) == this : "newer state loaded";
	}
	
	public Object beginTransaction() {
		checkActive();
		return new TestStateTransaction(this.store, this.address);
	}
	
	public void endTransaction(Object trans) {
		checkActive();
		assert trans instanceof TestStateTransaction : "unexpected transaction object";
		TestStateTransaction t = (TestStateTransaction)trans;
		assert XI.equals(t.base, this.address);
		t.commit();
	}
	
	TestStateTransaction getTrans(Object trans) {
		if(this.currentTrans != null) {
			assert this.currentTrans == trans : "each entity may only be part of one transaction at a time";
		}
		if(trans == null) {
			return new TestStateTransaction(this.store, this.address);
		}
		assert trans instanceof TestStateTransaction : "unexpected transaction object";
		TestStateTransaction t = (TestStateTransaction)trans;
		assert t.store == this.store;
		assert XX.equalsOrContains(t.base, this.address);
		return t;
	}
	
	public void delete(Object trans) {
		checkActive();
		TestStateTransaction t = getTrans(trans);
		this.currentTrans = t;
		t.saved.remove(this);
		t.deleted.add(this);
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
		t.deleted.remove(this);
		t.saved.add(this);
		this.saved = true;
		if(t != trans) {
			t.commit();
		}
		this.saveCount++;
	}
	
	public long getRevisionNumber() {
		checkActive();
		return this.revision;
	}
	
	public XValue getValue() {
		checkActive();
		return this.value;
	}
	
	public void setRevisionNumber(long revisionNumber) {
		checkActive();
		this.saved = false;
		this.revision = revisionNumber;
	}
	
	public void setValue(XValue value) {
		checkActive();
		this.saved = false;
		this.value = value;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public void add(XID childId) {
		checkActive();
		this.saved = false;
		this.children.add(childId);
	}
	
	public void remove(XID childId) {
		checkActive();
		this.saved = false;
		this.children.remove(childId);
	}
	
	public boolean isEmpty() {
		checkActive();
		return this.children.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		checkActive();
		return this.children.iterator();
	}
	
	boolean has(XID id) {
		checkActive();
		return this.children.contains(id);
	}
	
	public abstract XID getID();
	
	void load(TestStateStore.State state) {
		this.revision = state.revision;
		this.children.clear();
		this.children.addAll(state.children);
		this.value = state.value;
	}
	
}
