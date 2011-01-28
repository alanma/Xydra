package org.xydra.core.model.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.value.XValue;
import org.xydra.index.IMapMapIndex;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


public class MockStateStore implements XStateStore, Serializable {
	
	protected static class Log {
		
		long first;
		long last;
		
		public void save(MockChangeLogState state) {
			this.first = state.first;
			this.last = state.last;
		}
	}
	
	protected static class State {
		
		Set<XID> children = new HashSet<XID>();
		long revision;
		XValue value;
		
		public void save(MockState state) {
			this.revision = state.revision;
			this.children.clear();
			this.children.addAll(state.children);
			this.value = state.value;
		}
		
	}
	private static final long serialVersionUID = 5790667556879966890L;
	public List<MockChangeLogState> allLogs = new ArrayList<MockChangeLogState>();
	
	public List<MockState> allStates = new ArrayList<MockState>();
	
	public List<MockStateTransaction> allTrans = new ArrayList<MockStateTransaction>();
	
	final IMapMapIndex<XAddress,Long,XEvent> events = new MapMapIndex<XAddress,Long,XEvent>();
	final Map<XAddress,MockChangeLogState> loadedLogs = new HashMap<XAddress,MockChangeLogState>();
	final Map<XAddress,MockState> loadedStates = new HashMap<XAddress,MockState>();
	
	final Map<XAddress,Log> logs = new HashMap<XAddress,Log>();
	final Map<XAddress,State> states = new HashMap<XAddress,State>();
	
	public void checkConsistency() {
		
		for(MockState s : this.allStates) {
			assert s.saved : "unsaved entity state: " + s.address;
		}
		
		for(MockChangeLogState s : this.allLogs) {
			assert s.saved : "unsaved log state: " + s.getBaseAddress();
		}
		
		for(MockStateTransaction t : this.allTrans) {
			assert t.committed : "uncommitted transaction: " + t.base + " : " + t;
		}
		
		for(Map.Entry<XAddress,State> s : this.states.entrySet()) {
			XAddress a = s.getKey();
			for(XID id : s.getValue().children) {
				XAddress c = null;
				switch(a.getAddressedType()) {
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
			if(a.getAddressedType() == XType.XMODEL) {
				assert this.logs.containsKey(a) : "missing log for model: " + a;
			} else if(a.getAddressedType() == XType.XOBJECT && a.getModel() == null) {
				assert this.logs.containsKey(a) : "missing log for object: " + a;
			}
		}
		
		for(XAddress a : this.logs.keySet()) {
			assert this.states.containsKey(a) : "missing entity for log: " + a;
		}
		
		Iterator<XAddress> it = this.events.key1Iterator();
		while(it.hasNext()) {
			XAddress a = it.next();
			if(!this.logs.containsKey(a)) {
				Iterator<KeyKeyEntryTuple<XAddress,Long,XEvent>> it2 = this.events.tupleIterator(
				        new EqualsConstraint<XAddress>(a), new Wildcard<Long>());
				assert it.hasNext();
				assert this.logs.containsKey(a) : "missing log for event: " + it2.next();
			}
		}
		
	}
	
	public void checkOnlySavedOnce() {
		for(MockState s : this.allStates) {
			assert s.saveCount <= 1 : "double save for " + s.address;
		}
		for(MockChangeLogState s : this.allLogs) {
			assert s.saveCount <= 1 : "double save for log " + s.getBaseAddress();
		}
	}
	
	public MockFieldState createFieldState(XAddress fieldAddr) {
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		return new MockFieldState(this, fieldAddr);
	}
	
	public MockModelState createModelState(XAddress modelAddr) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		XChangeLogState log = new MockChangeLogState(this, modelAddr);
		return new MockModelState(this, modelAddr, log);
	}
	
	public MockObjectState createObjectState(XAddress objectAddr) {
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		XChangeLogState log = objectAddr.getModel() == null ? new MockChangeLogState(this,
		        objectAddr) : null;
		return new MockObjectState(this, objectAddr, log);
	}
	
	public MockRepositoryState createRepositoryState(XAddress repoAddr) {
		assert repoAddr.getAddressedType() == XType.XREPOSITORY;
		return new MockRepositoryState(this, repoAddr);
	}
	
	protected void delete(MockChangeLogState state) {
		this.logs.remove(state.getBaseAddress());
	}
	
	protected void delete(MockChangeLogState state, long rev) {
		assert state.getEvent(rev) == null;
		this.events.deIndex(state.getBaseAddress(), rev);
	}
	
	protected void delete(MockState state) {
		this.states.remove(state.getAddress());
	}
	
	public int getTransCount() {
		return this.allTrans.size();
	}
	
	public XFieldState loadFieldState(XAddress fieldAddr) {
		assert fieldAddr.getAddressedType() == XType.XFIELD;
		State s = this.states.get(fieldAddr);
		if(s == null) {
			return null;
		}
		MockFieldState state = createFieldState(fieldAddr);
		state.load(s);
		return state;
	}
	
	private XChangeLogState loadLog(XAddress addr) {
		Log l = this.logs.get(addr);
		if(l == null) {
			return null;
		}
		MockChangeLogState state = new MockChangeLogState(this, addr);
		state.load(l, this.events.tupleIterator(new EqualsConstraint<XAddress>(addr),
		        new Wildcard<Long>()));
		return state;
	}
	
	public XModelState loadModelState(XAddress modelAddr) {
		assert modelAddr.getAddressedType() == XType.XMODEL;
		State s = this.states.get(modelAddr);
		if(s == null) {
			return null;
		}
		XChangeLogState log = loadLog(modelAddr);
		MockModelState state = new MockModelState(this, modelAddr, log);
		state.load(s);
		return state;
	}
	
	public XObjectState loadObjectState(XAddress objectAddr) {
		assert objectAddr.getAddressedType() == XType.XOBJECT;
		State s = this.states.get(objectAddr);
		if(s == null) {
			return null;
		}
		XChangeLogState log = loadLog(objectAddr);
		MockObjectState state = new MockObjectState(this, objectAddr, log);
		state.load(s);
		return state;
	}
	
	public XRepositoryState loadRepositoryState(XAddress repoAddr) {
		assert repoAddr.getAddressedType() == XType.XREPOSITORY;
		State s = this.states.get(repoAddr);
		if(s == null) {
			return null;
		}
		MockRepositoryState state = createRepositoryState(repoAddr);
		state.load(s);
		return state;
	}
	
	public void resetSaves() {
		for(MockState s : this.allStates) {
			s.saveCount = 0;
		}
		for(MockChangeLogState l : this.allLogs) {
			l.saveCount = 0;
		}
	}
	
	public void resetTrans() {
		this.allTrans.clear();
	}
	
	protected void save(MockChangeLogState state) {
		Log s = this.logs.get(state.getBaseAddress());
		if(s == null) {
			s = new Log();
			this.logs.put(state.getBaseAddress(), s);
		}
		s.save(state);
	}
	
	protected void save(MockChangeLogState state, long rev) {
		assert state.getEvent(rev) != null;
		this.events.index(state.getBaseAddress(), rev, state.getEvent(rev));
	}
	
	protected void save(MockState state) {
		State s = this.states.get(state.getAddress());
		if(s == null) {
			s = new State();
			this.states.put(state.getAddress(), s);
		}
		s.save(state);
	}
	
}
