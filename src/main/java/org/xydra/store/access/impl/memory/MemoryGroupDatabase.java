package org.xydra.store.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;
import org.xydra.index.ITransitivePairIndex;
import org.xydra.index.XI;
import org.xydra.index.impl.FastStoredTransitivePairIndex;
import org.xydra.index.impl.MapPairIndex;
import org.xydra.index.impl.PairIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;
import org.xydra.store.access.XA;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.XGroupEvent;
import org.xydra.store.access.XGroupListener;
import org.xydra.store.access.impl.delegate.IHookListener;
import org.xydra.store.access.impl.delegate.ISendHookEvents;


/**
 * In-memory implementation of {@link XGroupDatabaseWithListeners}.
 * 
 * IMPROVE using standard java monitor for now, reader-writer lock may be more
 * appropriate
 * 
 * @author dscharrer
 * 
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryGroupDatabase implements XGroupDatabaseWithListeners, ISendHookEvents {
	
	public static final long serialVersionUID = 4404147651476087029L;
	
	private final Set<IHookListener> hookListeners;
	// map of actor->group relationships
	private final ITransitivePairIndex<XID> index;
	private final Set<XGroupListener> listeners;
	
	public MemoryGroupDatabase() {
		this.index = new FastStoredTransitivePairIndex<XID>(new PairIndex<XID,XID>(),
		        new Factory<IPairIndex<XID,XID>>() {
			        
			        @Override
			        public IPairIndex<XID,XID> createInstance() {
				        return new MapPairIndex<XID,XID>();
			        }
			        
		        });
		this.listeners = new HashSet<XGroupListener>();
		this.hookListeners = new HashSet<IHookListener>();
	}
	
	@Override
	public void addHookListener(IHookListener listener) {
		this.hookListeners.add(listener);
	}
	
	@Override
	synchronized public void addListener(XGroupListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	synchronized public void addToGroup(XID actor, XID group) {
		hookBeforeWrite();
		if(XI.equals(actor, XA.GROUP_ALL) || hasGroup(actor, group)) {
			// nothing to do
			return;
		}
		try {
			this.index.index(actor, group);
		} catch(ITransitivePairIndex.CycleException e) {
			throw new AssertionError("cannot happen");
		}
		dispatchEvent(new MemoryGroupEvent(ChangeType.ADD, actor, group));
	}
	
	private void dispatchEvent(XGroupEvent event) {
		for(XGroupListener listener : this.listeners) {
			listener.onGroupEvent(event);
		}
	}
	
	synchronized public Set<XID> getDirectGroups(XID actor) {
		hookBeforeRead();
		return toSet(new AbstractTransformingIterator<Pair<XID,XID>,XID>(
		        this.index
		                .constraintIterator(new EqualsConstraint<XID>(actor), new Wildcard<XID>())) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getSecond();
			}
			
		});
	}
	
	synchronized public Set<XID> getDirectMembers(XID group) {
		hookBeforeRead();
		return toSet(new AbstractTransformingIterator<Pair<XID,XID>,XID>(
		        this.index
		                .constraintIterator(new Wildcard<XID>(), new EqualsConstraint<XID>(group))) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getFirst();
			}
			
		});
	}
	
	@Override
	public Set<XID> getGroups() {
		hookBeforeRead();
		return toSet(this.index.key2Iterator());
	}
	
	@Override
	synchronized public Set<XID> getGroupsOf(XID actor) {
		return toSet(new AbstractTransformingIterator<Pair<XID,XID>,XID>(
		        this.index
		                .transitiveIterator(new EqualsConstraint<XID>(actor), new Wildcard<XID>())) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getSecond();
			}
			
		});
	}
	
	@Override
	synchronized public Set<XID> getMembersOf(XID group) {
		return toSet(new AbstractTransformingIterator<Pair<XID,XID>,XID>(
		        this.index
		                .transitiveIterator(new Wildcard<XID>(), new EqualsConstraint<XID>(group))) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getFirst();
			}
			
		});
	}
	
	synchronized public boolean hasDirectGroup(XID actor, XID group) {
		hookBeforeRead();
		return this.index.contains(new EqualsConstraint<XID>(actor), new EqualsConstraint<XID>(
		        group));
	}
	
	@Override
	synchronized public boolean hasGroup(XID actor, XID group) {
		hookBeforeRead();
		if(XI.equals(group, XA.GROUP_ALL)) {
			return true;
		}
		if(XI.equals(actor, XA.GROUP_ALL)) {
			return false;
		}
		return this.index.implies(new EqualsConstraint<XID>(actor),
		        new EqualsConstraint<XID>(group));
	}
	
	private void hookBeforeRead() {
		for(IHookListener listener : this.hookListeners) {
			listener.beforeRead();
		}
	}
	
	private void hookBeforeWrite() {
		for(IHookListener listener : this.hookListeners) {
			listener.beforeWrite();
		}
	}
	
	@Override
	synchronized public void removeFromGroup(XID actor, XID group) {
		hookBeforeWrite();
		/* don't remove from built-in ALL-group */
		if(XI.equals(group, XA.GROUP_ALL) || !hasGroup(actor, group)) {
			// nothing to do
			return;
		}
		this.index.deIndex(actor, group);
		dispatchEvent(new MemoryGroupEvent(ChangeType.REMOVE, actor, group));
	}
	
	@Override
	public void removeGroup(XID groupId) {
		hookBeforeWrite();
		/*
		 * TODO IMPROVE could also be its own event, but makes things just more
		 * complicated
		 */
		for(XID member : getDirectMembers(groupId)) {
			this.removeFromGroup(member, groupId);
		}
	}
	
	@Override
	public void removeHookListener(IHookListener listener) {
		this.hookListeners.remove(listener);
	}
	
	@Override
	synchronized public void removeListener(XGroupListener listener) {
		this.listeners.remove(listener);
	}
	
	/**
	 * @param it
	 * @return a new Set instance with all items from 'it'
	 */
	private static Set<XID> toSet(Iterator<XID> it) {
		Set<XID> set = new HashSet<XID>();
		while(it.hasNext()) {
			set.add(it.next());
		}
		return set;
	}
	
	@Override
	synchronized public String toString() {
		return this.index.toString();
	}
	
}
