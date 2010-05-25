package org.xydra.core.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.access.XA;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.XGroupEvent;
import org.xydra.core.access.XGroupListener;
import org.xydra.core.change.ChangeType;
import org.xydra.core.model.XID;
import org.xydra.index.Factory;
import org.xydra.index.IPairIndex;
import org.xydra.index.ITransitivePairIndex;
import org.xydra.index.impl.FastStoredTransitivePairIndex;
import org.xydra.index.impl.MapPairIndex;
import org.xydra.index.impl.PairIndex;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;
import org.xydra.index.query.Wildcard;


/**
 * In-memory implementation of {@link XGroupDatabase}.
 * 
 * IMPROVE using standard java monitor for now, reader-writer lock may be more
 * appropriate
 * 
 * @author dscharrer
 * 
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class MemoryGroupDatabase implements XGroupDatabase {
	
	// map of actor->group relationships
	private final ITransitivePairIndex<XID> index;
	private final Set<XGroupListener> listeners;
	
	public MemoryGroupDatabase() {
		this.index = new FastStoredTransitivePairIndex<XID>(new PairIndex<XID,XID>(),
		        new Factory<IPairIndex<XID,XID>>() {
			        
			        public IPairIndex<XID,XID> createInstance() {
				        return new MapPairIndex<XID,XID>();
			        }
			        
		        });
		this.listeners = new HashSet<XGroupListener>();
	}
	
	synchronized public Iterator<XID> getAllGroups(XID actor) {
		return new AbstractTransformingIterator<Pair<XID,XID>,XID>(this.index.transitiveIterator(
		        new EqualsConstraint<XID>(actor), new Wildcard<XID>())) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getSecond();
			}
			
		};
	}
	
	synchronized public Iterator<XID> getAllMembers(XID group) {
		return new AbstractTransformingIterator<Pair<XID,XID>,XID>(this.index.transitiveIterator(
		        new Wildcard<XID>(), new EqualsConstraint<XID>(group))) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getFirst();
			}
			
		};
	}
	
	synchronized public Iterator<XID> getGroups(XID actor) {
		return new AbstractTransformingIterator<Pair<XID,XID>,XID>(this.index.constraintIterator(
		        new EqualsConstraint<XID>(actor), new Wildcard<XID>())) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getSecond();
			}
			
		};
	}
	
	synchronized public Iterator<XID> getMembers(XID group) {
		return new AbstractTransformingIterator<Pair<XID,XID>,XID>(this.index.constraintIterator(
		        new Wildcard<XID>(), new EqualsConstraint<XID>(group))) {
			
			@Override
			public XID transform(Pair<XID,XID> in) {
				return in.getFirst();
			}
			
		};
	}
	
	synchronized public boolean hasDirectGroup(XID actor, XID group) {
		return this.index.contains(new EqualsConstraint<XID>(actor), new EqualsConstraint<XID>(
		        group));
	}
	
	synchronized public boolean hasGroup(XID actor, XID group) {
		if(XX.equals(group, XA.GROUP_ALL)) {
			return true;
		}
		if(XX.equals(actor, XA.GROUP_ALL)) {
			return false;
		}
		return this.index.implies(new EqualsConstraint<XID>(actor),
		        new EqualsConstraint<XID>(group));
	}
	
	synchronized public void addToGroup(XID actor, XID group) throws CycleException {
		if(XX.equals(actor, XA.GROUP_ALL) || hasGroup(actor, group)) {
			// nothing to do
			return;
		}
		try {
			this.index.index(actor, group);
		} catch(ITransitivePairIndex.CycleException e) {
			throw new CycleException();
		}
		dispatchEvent(new MemoryGroupEvent(ChangeType.ADD, actor, group));
	}
	
	synchronized public void removeFromGroup(XID actor, XID group) {
		if(XX.equals(group, XA.GROUP_ALL) || !hasGroup(actor, group)) {
			// nothing to do
			return;
		}
		this.index.deIndex(actor, group);
		dispatchEvent(new MemoryGroupEvent(ChangeType.REMOVE, actor, group));
	}
	
	@Override
	synchronized public String toString() {
		return this.index.toString();
	}
	
	private void dispatchEvent(XGroupEvent event) {
		for(XGroupListener listener : this.listeners) {
			listener.onGroupEvent(event);
		}
	}
	
	synchronized public void addListener(XGroupListener listener) {
		this.listeners.add(listener);
	}
	
	synchronized public void removeListener(XGroupListener listener) {
		this.listeners.remove(listener);
	}
	
	public Iterator<XID> getGroups() {
		return this.index.key2Iterator();
	}
}
