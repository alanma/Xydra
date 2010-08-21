package org.xydra.core.test.model.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.impl.memory.MemoryAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.model.state.XModelState;
import org.xydra.core.model.state.XObjectState;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XStateStore;
import org.xydra.core.value.XValue;
import org.xydra.index.IMapMapIndex;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Wildcard;


public class TestStateStore implements XStateStore, Serializable {
	
	private static final long serialVersionUID = 5790667556879966890L;
	
	public List<TestState> allStates = new ArrayList<TestState>();
	public List<TestChangeLogState> allLogs = new ArrayList<TestChangeLogState>();
	public List<TestStateTransaction> allTrans = new ArrayList<TestStateTransaction>();
	
	protected static class State {
		
		long revision;
		Set<XID> children = new HashSet<XID>();
		XValue value;
		
		public void save(TestState state) {
			this.revision = state.revision;
			this.children.clear();
			this.children.addAll(state.children);
			this.value = state.value;
		}
		
	}
	
	protected static class Log {
		
		long first;
		long last;
		
		public void save(TestChangeLogState state) {
			this.first = state.first;
			this.last = state.last;
		}
	}
	
	final Map<XAddress,State> states = new HashMap<XAddress,State>();
	final Map<XAddress,Log> logs = new HashMap<XAddress,Log>();
	final IMapMapIndex<XAddress,Long,XEvent> events = new MapMapIndex<XAddress,Long,XEvent>();
	
	final Map<XAddress,TestState> loadedStates = new HashMap<XAddress,TestState>();
	final Map<XAddress,TestChangeLogState> loadedLogs = new HashMap<XAddress,TestChangeLogState>();
	
	public TestFieldState createFieldState(XAddress fieldAddr) {
		assert MemoryAddress.getAddressedType(fieldAddr) == XType.XFIELD;
		return new TestFieldState(this, fieldAddr);
	}
	
	public TestModelState createModelState(XAddress modelAddr) {
		assert MemoryAddress.getAddressedType(modelAddr) == XType.XMODEL;
		XChangeLogState log = new TestChangeLogState(this, modelAddr);
		return new TestModelState(this, modelAddr, log);
	}
	
	public TestObjectState createObjectState(XAddress objectAddr) {
		assert MemoryAddress.getAddressedType(objectAddr) == XType.XOBJECT;
		XChangeLogState log = objectAddr.getModel() == null ? new TestChangeLogState(this,
		        objectAddr) : null;
		return new TestObjectState(this, objectAddr, log);
	}
	
	public TestRepositoryState createRepositoryState(XAddress repoAddr) {
		assert MemoryAddress.getAddressedType(repoAddr) == XType.XREPOSITORY;
		return new TestRepositoryState(this, repoAddr);
	}
	
	public XFieldState loadFieldState(XAddress fieldAddr) {
		assert MemoryAddress.getAddressedType(fieldAddr) == XType.XFIELD;
		State s = this.states.get(fieldAddr);
		if(s == null) {
			return null;
		}
		TestFieldState state = createFieldState(fieldAddr);
		state.load(s);
		return state;
	}
	
	public XModelState loadModelState(XAddress modelAddr) {
		assert MemoryAddress.getAddressedType(modelAddr) == XType.XMODEL;
		State s = this.states.get(modelAddr);
		if(s == null) {
			return null;
		}
		XChangeLogState log = loadLog(modelAddr);
		TestModelState state = new TestModelState(this, modelAddr, log);
		state.load(s);
		return state;
	}
	
	public XObjectState loadObjectState(XAddress objectAddr) {
		assert MemoryAddress.getAddressedType(objectAddr) == XType.XOBJECT;
		State s = this.states.get(objectAddr);
		if(s == null) {
			return null;
		}
		XChangeLogState log = loadLog(objectAddr);
		TestObjectState state = new TestObjectState(this, objectAddr, log);
		state.load(s);
		return state;
	}
	
	public XRepositoryState loadRepositoryState(XAddress repoAddr) {
		assert MemoryAddress.getAddressedType(repoAddr) == XType.XREPOSITORY;
		State s = this.states.get(repoAddr);
		if(s == null) {
			return null;
		}
		TestRepositoryState state = createRepositoryState(repoAddr);
		state.load(s);
		return state;
	}
	
	private XChangeLogState loadLog(XAddress addr) {
		Log l = this.logs.get(addr);
		if(l == null) {
			return null;
		}
		TestChangeLogState state = new TestChangeLogState(this, addr);
		state.load(l, this.events.tupleIterator(new EqualsConstraint<XAddress>(addr),
		        new Wildcard<Long>()));
		return state;
	}
	
	protected void delete(TestState state) {
		this.states.remove(state.getAddress());
	}
	
	protected void save(TestState state) {
		State s = this.states.get(state.getAddress());
		if(s == null) {
			s = new State();
			this.states.put(state.getAddress(), s);
		}
		s.save(state);
	}
	
	protected void delete(TestChangeLogState state) {
		this.logs.remove(state.getBaseAddress());
	}
	
	protected void save(TestChangeLogState state) {
		Log s = this.logs.get(state.getBaseAddress());
		if(s == null) {
			s = new Log();
			this.logs.put(state.getBaseAddress(), s);
		}
		s.save(state);
	}
	
	protected void delete(TestChangeLogState state, long rev) {
		assert state.getEvent(rev) == null;
		this.events.deIndex(state.getBaseAddress(), rev);
	}
	
	protected void save(TestChangeLogState state, long rev) {
		assert state.getEvent(rev) != null;
		this.events.index(state.getBaseAddress(), rev, state.getEvent(rev));
	}
	
	public void checkConsistency() {
		
		for(TestState s : this.allStates) {
			assert s.saved : "unsaved entity state: " + s.address;
		}
		
		for(TestChangeLogState s : this.allLogs) {
			assert s.saved : "unsaved log state: " + s.getBaseAddress();
		}
		
		for(TestStateTransaction t : this.allTrans) {
			assert t.committed : "uncommitted transaction: " + t.base + " : " + t;
		}
		
		for(Map.Entry<XAddress,State> s : this.states.entrySet()) {
			XAddress a = s.getKey();
			for(XID id : s.getValue().children) {
				XAddress c = null;
				switch(MemoryAddress.getAddressedType(a)) {
				case XREPOSITORY:
					c = XX.resolveModel(a, id);
					break;
				case XMODEL:
					c = XX.resolveObject(a, id);
					break;
				case XOBJECT:
					c = XX.resolveField(a, id);
					break;
				default:
					assert false : "fields cannot have children";
				}
				assert this.states.containsKey(c) : "missing child: " + c;
			}
			XAddress p = a.getParent();
			assert p == null || this.states.containsKey(p) : "missing parent for: " + a;
			if(MemoryAddress.getAddressedType(a) == XType.XMODEL) {
				assert this.logs.containsKey(a) : "missing log for model: " + a;
			} else if(MemoryAddress.getAddressedType(a) == XType.XOBJECT && a.getModel() == null) {
				assert this.logs.containsKey(a) : "missing log for object: " + a;
			}
		}
		
		for(XAddress a : this.logs.keySet()) {
			assert this.states.containsKey(a) : "missing entity for log: " + a;
		}
		
		Iterator<XAddress> it = this.events.key1Iterator();
		while(it.hasNext()) {
			XAddress a = it.next();
			assert this.logs.containsKey(a) : "missing log for event: " + a;
		}
		
	}
	
	public void resetSaves() {
		for(TestState s : this.allStates) {
			s.saveCount = 0;
		}
		for(TestChangeLogState l : this.allLogs) {
			l.saveCount = 0;
		}
	}
	
	public void checkOnlySavedOnce() {
		for(TestState s : this.allStates) {
			assert s.saveCount <= 1 : "double save for " + s.address;
		}
		for(TestChangeLogState s : this.allLogs) {
			assert s.saveCount <= 1 : "double save for log " + s.getBaseAddress();
		}
	}
	
	public void resetTrans() {
		this.allTrans.clear();
	}
	
	public int getTransCount() {
		return this.allTrans.size();
	}
	
}
