package org.xydra.core.model.state;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XValue;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.index.XI;


public abstract class TestState implements Serializable {
	
	private static final long serialVersionUID = 7707154420739741167L;
	
	XAddress address;
	Set<XID> children = new HashSet<XID>();
	TestStateTransaction currentTrans;
	long revision;
	int saveCount;
	boolean saved;
	TestStateStore store;
	
	XValue value;
	
	public TestState(TestStateStore store, XAddress address) {
		this.address = address;
		this.store = store;
		this.store.allStates.add(this);
		this.store.loadedStates.put(address, this);
		assert getID() != null;
	}
	
	public void add(XID childId) {
		checkActive();
		this.saved = false;
		this.children.add(childId);
	}
	
	public XStateTransaction beginTransaction() {
		checkActive();
		return new TestStateTransaction(this.store, this.address);
	}
	
	void checkActive() {
		assert this.store.loadedStates.get(this.address) == this : "newer state loaded";
	}
	
	public void delete(XStateTransaction trans) {
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
	
	public void endTransaction(XStateTransaction trans) {
		checkActive();
		assert trans instanceof TestStateTransaction : "unexpected transaction object";
		TestStateTransaction t = (TestStateTransaction)trans;
		assert XI.equals(t.base, this.address);
		t.commit();
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
	public abstract XID getID();
	
	public long getRevisionNumber() {
		checkActive();
		return this.revision;
	}
	
	TestStateTransaction getTrans(XStateTransaction trans) {
		if(this.currentTrans != null) {
			assert this.currentTrans == trans : "each entity may only be part of one transaction at a time";
		}
		if(trans == null) {
			return new TestStateTransaction(this.store, this.address);
		}
		assert trans instanceof TestStateTransaction : "unexpected transaction object";
		TestStateTransaction t = (TestStateTransaction)trans;
		assert t.store == this.store;
		assert t.base.equalsOrContains(this.address);
		return t;
	}
	
	public XValue getValue() {
		checkActive();
		return this.value;
	}
	
	boolean has(XID id) {
		checkActive();
		return this.children.contains(id);
	}
	
	public boolean isEmpty() {
		checkActive();
		return this.children.isEmpty();
	}
	
	public Iterator<XID> iterator() {
		checkActive();
		return this.children.iterator();
	}
	
	void load(TestStateStore.State state) {
		this.revision = state.revision;
		this.children.clear();
		this.children.addAll(state.children);
		this.value = state.value;
	}
	
	public void remove(XID childId) {
		checkActive();
		this.saved = false;
		this.children.remove(childId);
	}
	
	public void save(XStateTransaction trans) {
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
	
}
