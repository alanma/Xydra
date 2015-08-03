package org.xydra.store.access.impl.memory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
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
	private final ITransitivePairIndex<XId> index;
	private final Set<XGroupListener> listeners;

	public MemoryGroupDatabase() {
		this.index = new FastStoredTransitivePairIndex<XId>(new PairIndex<XId,XId>(),
		        new Factory<IPairIndex<XId,XId>>() {

			        @Override
			        public IPairIndex<XId,XId> createInstance() {
				        return new MapPairIndex<XId,XId>();
			        }

		        });
		this.listeners = new HashSet<XGroupListener>();
		this.hookListeners = new HashSet<IHookListener>();
	}

	@Override
	public void addHookListener(final IHookListener listener) {
		this.hookListeners.add(listener);
	}

	@Override
	synchronized public void addListener(final XGroupListener listener) {
		this.listeners.add(listener);
	}

	@Override
	synchronized public void addToGroup(final XId actor, final XId group) {
		hookBeforeWrite();
		if(XI.equals(actor, XA.GROUP_ALL) || hasGroup(actor, group)) {
			// nothing to do
			return;
		}
		try {
			this.index.index(actor, group);
		} catch(final ITransitivePairIndex.CycleException e) {
			throw new AssertionError("cannot happen");
		}
		dispatchEvent(new MemoryGroupEvent(ChangeType.ADD, actor, group));
	}

	private void dispatchEvent(final XGroupEvent event) {
		for(final XGroupListener listener : this.listeners) {
			listener.onGroupEvent(event);
		}
	}

	synchronized public Set<XId> getDirectGroups(final XId actor) {
		hookBeforeRead();
		return toSet(new AbstractTransformingIterator<Pair<XId,XId>,XId>(
		        this.index
		                .constraintIterator(new EqualsConstraint<XId>(actor), new Wildcard<XId>())) {

			@Override
			public XId transform(final Pair<XId,XId> in) {
				return in.getSecond();
			}

		});
	}

	synchronized public Set<XId> getDirectMembers(final XId group) {
		hookBeforeRead();
		return toSet(new AbstractTransformingIterator<Pair<XId,XId>,XId>(
		        this.index
		                .constraintIterator(new Wildcard<XId>(), new EqualsConstraint<XId>(group))) {

			@Override
			public XId transform(final Pair<XId,XId> in) {
				return in.getFirst();
			}

		});
	}

	@Override
	public Set<XId> getGroups() {
		hookBeforeRead();
		return toSet(this.index.key2Iterator());
	}

	@Override
	synchronized public Set<XId> getGroupsOf(final XId actor) {
		return toSet(new AbstractTransformingIterator<Pair<XId,XId>,XId>(
		        this.index
		                .transitiveIterator(new EqualsConstraint<XId>(actor), new Wildcard<XId>())) {

			@Override
			public XId transform(final Pair<XId,XId> in) {
				return in.getSecond();
			}

		});
	}

	@Override
	synchronized public Set<XId> getMembersOf(final XId group) {
		return toSet(new AbstractTransformingIterator<Pair<XId,XId>,XId>(
		        this.index
		                .transitiveIterator(new Wildcard<XId>(), new EqualsConstraint<XId>(group))) {

			@Override
			public XId transform(final Pair<XId,XId> in) {
				return in.getFirst();
			}

		});
	}

	synchronized public boolean hasDirectGroup(final XId actor, final XId group) {
		hookBeforeRead();
		return this.index.contains(new EqualsConstraint<XId>(actor), new EqualsConstraint<XId>(
		        group));
	}

	@Override
	synchronized public boolean hasGroup(final XId actor, final XId group) {
		hookBeforeRead();
		if(XI.equals(group, XA.GROUP_ALL)) {
			return true;
		}
		if(XI.equals(actor, XA.GROUP_ALL)) {
			return false;
		}
		return this.index.implies(new EqualsConstraint<XId>(actor),
		        new EqualsConstraint<XId>(group));
	}

	private void hookBeforeRead() {
		for(final IHookListener listener : this.hookListeners) {
			listener.beforeRead();
		}
	}

	private void hookBeforeWrite() {
		for(final IHookListener listener : this.hookListeners) {
			listener.beforeWrite();
		}
	}

	@Override
	synchronized public void removeFromGroup(final XId actor, final XId group) {
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
	public void removeGroup(final XId groupId) {
		hookBeforeWrite();
		/*
		 * TODO IMPROVE could also be its own event, but makes things just more
		 * complicated
		 */
		for(final XId member : getDirectMembers(groupId)) {
			removeFromGroup(member, groupId);
		}
	}

	@Override
	public void removeHookListener(final IHookListener listener) {
		this.hookListeners.remove(listener);
	}

	@Override
	synchronized public void removeListener(final XGroupListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * @param it
	 * @return a new Set instance with all items from 'it'
	 */
	private static Set<XId> toSet(final Iterator<XId> it) {
		final Set<XId> set = new HashSet<XId>();
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
