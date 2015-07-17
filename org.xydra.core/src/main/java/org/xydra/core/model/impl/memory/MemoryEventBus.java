/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.xydra.core.model.impl.memory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.annotations.LicenseApache;
import org.xydra.base.XAddress;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XSyncEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.impl.MapMapSetIndex;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.ITriple;
import org.xydra.index.query.Wildcard;

/**
 * Can register listeners and send events for all kinds of Xydra events.
 *
 * Supports de-/registering old/new listeners while event sending is running
 * with an internal command queue.
 *
 * @author xamde
 */
@LicenseApache(copyright = "Copyright 2011 Google Inc.", modified = true)
public class MemoryEventBus {

	public static enum EventType {
		FieldChange, ModelChange, ObjectChange, RepositoryChange,

		TransactionChange, Sync,
	}

	/**
	 * @param eventType
	 * @NeverNull
	 * @param event
	 * @NeverNull
	 * @param listener
	 * @NeverNull
	 */
	private static void fireEventToListener(final EventType eventType, final Object event, final Object listener) {
		switch (eventType) {
		case FieldChange:
			((XFieldEventListener) listener).onChangeEvent((XFieldEvent) event);
			break;
		case ObjectChange:
			((XObjectEventListener) listener).onChangeEvent((XObjectEvent) event);
			break;
		case ModelChange:
			((XModelEventListener) listener).onChangeEvent((XModelEvent) event);
			break;
		case RepositoryChange:
			((XRepositoryEventListener) listener).onChangeEvent((XRepositoryEvent) event);
			break;
		case TransactionChange:
			((XTransactionEventListener) listener).onChangeEvent((XTransactionEvent) event);
			break;
		case Sync:
			((XSyncEventListener) listener).onSynced((XSyncEvent) event);
		}
	}

	/**
	 * Add and remove operations received during dispatch.
	 */
	private final List<Runnable> deferredDeltas = new LinkedList<Runnable>();

	private int fireCalls = 0;

	IMapMapSetIndex<EventType, XAddress, Object> map = new MapMapSetIndex<EventType, XAddress, Object>(
			new SmallEntrySetFactory<Object>());

	/**
	 * @param eventType
	 * @param sourceAddress
	 * @param listener
	 * @return
	 */
	public boolean addListener(final EventType eventType, final XAddress sourceAddress,
			final Object listener) {
		if (this.fireCalls > 0) {
			this.deferredDeltas.add(new Runnable() {

				@Override
				public void run() {
					addListener(eventType, sourceAddress, listener);
				}
			});
			// we can just hope it will work
			return true;
		} else {
			return this.map.index(eventType, sourceAddress, listener);
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * events of type EventType happening on source-entities.
	 *
	 * @param eventType
	 * @NeverNull
	 * @param source
	 * @NeverNull
	 * @param event The, e.g., {@link XFieldEvent} which will be propagated to
	 *            the registered listeners.
	 */
	public void fireEvent(final EventType eventType, final XAddress source, final Object event) {
		assert eventType != null;
		if (event == null) {
			throw new NullPointerException("Cannot fire null event");
		}

		synchronized (this.map) {
			this.fireCalls++;
			try {
				final Iterator<ITriple<EventType, XAddress, Object>> it = this.map.tupleIterator(

						new EqualsConstraint<MemoryEventBus.EventType>(eventType),

						source == null ? new Wildcard<XAddress>() : new EqualsConstraint<XAddress>(source),

								new Wildcard<Object>()

						);
				while (it.hasNext()) {
					final Object o = it.next().getEntry();
					fireEventToListener(eventType, event, o);
				}
			} finally {
				this.fireCalls--;
				if (this.fireCalls == 0) {
					handleQueuedAddsAndRemoves();
				}
			}
		}
	}

	private void handleQueuedAddsAndRemoves() {
		try {
			for (final Runnable r : this.deferredDeltas) {
				r.run();
			}
		} finally {
			this.deferredDeltas.clear();
		}
	}

	public boolean removeListener(final EventType eventType, final XAddress sourceAddress,
			final Object listener) {
		if (this.fireCalls > 0) {
			this.deferredDeltas.add(new Runnable() {

				@Override
				public void run() {
					removeListener(eventType, sourceAddress, listener);
				}
			});
			// we can just assume it will work
			return true;
		} else {
			return this.map.deIndex(eventType, sourceAddress, listener);
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		final Iterator<ITriple<EventType, XAddress, Object>> tupleIt = this.map.tupleIterator(
				(EventType) null, null, null);
		while (tupleIt.hasNext()) {
			final ITriple<MemoryEventBus.EventType, XAddress, Object> tuple = tupleIt.next();
			b.append("EventType=" + tuple.getKey1() + ".Address=" + tuple.getKey2() + " => "
					+ tuple.getEntry().getClass() + "\n");
		}
		return b.toString();
	}

}
