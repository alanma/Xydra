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

import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XEntity;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XFieldSyncEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XModelSyncEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XObjectSyncEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.index.IMapMapSetIndex;
import org.xydra.index.impl.MapMapSetIndex;
import org.xydra.index.impl.SmallEntrySetFactory;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;


/**
 * Can register listeners and send events for all kinds of Xydra events.
 * 
 * Supports de-/registering old/new listeners while event sending is running
 * with an internal command queue.
 * 
 * @author xamde
 */
public class MemoryEventBus {
    
    public static enum EventType {
        FieldChange, FieldSync, ModelChange, ModelSync, ObjectChange, ObjectSync, RepositoryChange, TransactionChange
    }
    
    private static void fireEvent(EventType eventType, XEntity source, XEvent event, Object o) {
        switch(eventType) {
        case FieldChange:
            ((XFieldEventListener)o).onChangeEvent((XFieldEvent)event);
            break;
        case FieldSync:
            ((XFieldSyncEventListener)o).onSynced((XFieldEvent)event);
            break;
        case ObjectChange:
            ((XObjectEventListener)o).onChangeEvent((XObjectEvent)event);
            break;
        case ObjectSync:
            ((XObjectSyncEventListener)o).onSynced((XObjectEvent)event);
            break;
        case ModelChange:
            ((XModelEventListener)o).onChangeEvent((XModelEvent)event);
            break;
        case ModelSync:
            ((XModelSyncEventListener)o).onSynced((XModelEvent)event);
            break;
        case RepositoryChange:
            ((XRepositoryEventListener)o).onChangeEvent((XRepositoryEvent)event);
            break;
        case TransactionChange:
            ((XTransactionEventListener)o).onChangeEvent((XTransactionEvent)event);
            break;
        }
    }
    
    /**
     * Add and remove operations received during dispatch.
     */
    private List<Runnable> deferredDeltas = new LinkedList<Runnable>();
    
    private int fireCalls = 0;
    
    IMapMapSetIndex<EventType,XEntity,Object> map = new MapMapSetIndex<EventType,XEntity,Object>(
            new SmallEntrySetFactory<Object>());
    
    public boolean addListener(final EventType eventType, final XEntity sourceEntity,
            final Object listener) {
        if(this.fireCalls > 0) {
            this.deferredDeltas.add(new Runnable() {
                
                @Override
                public void run() {
                    addListener(eventType, sourceEntity, listener);
                }
            });
            // we can just assume it will work
            return true;
        } else {
            return this.map.index(eventType, sourceEntity, listener);
        }
    }
    
    /**
     * Notifies all listeners that have registered interest for notification on
     * events of type EventType happening on source-entities.
     * 
     * @param eventType @NeverNull
     * @param source @NeverNull
     * @param event The {@link XFieldEvent} which will be propagated to the
     *            registered listeners.
     */
    public void fireEvent(EventType eventType, XEntity source, XEvent event) {
        assert eventType != null;
        if(event == null) {
            throw new NullPointerException("Cannot fire null event");
        }
        
        synchronized(this.map) {
            this.fireCalls++;
            try {
                Iterator<KeyKeyEntryTuple<EventType,XEntity,Object>> it = this.map.tupleIterator(
                        new EqualsConstraint<MemoryEventBus.EventType>(eventType),
                        new EqualsConstraint<XEntity>(source), new Wildcard<Object>());
                while(it.hasNext()) {
                    Object o = it.next().getEntry();
                    fireEvent(eventType, source, event, o);
                }
            } finally {
                this.fireCalls--;
                if(this.fireCalls == 0) {
                    handleQueuedAddsAndRemoves();
                }
            }
        }
    }
    
    private void handleQueuedAddsAndRemoves() {
        try {
            for(Runnable r : this.deferredDeltas) {
                r.run();
            }
        } finally {
            this.deferredDeltas.clear();
        }
    }
    
    public boolean removeListener(final EventType eventType, final XEntity sourceEntity,
            final Object listener) {
        if(this.fireCalls > 0) {
            this.deferredDeltas.add(new Runnable() {
                
                @Override
                public void run() {
                    removeListener(eventType, sourceEntity, listener);
                }
            });
            // we can just assume it will work
            return true;
        } else {
            return this.map.deIndex(eventType, sourceEntity, listener);
        }
    }
    
}
