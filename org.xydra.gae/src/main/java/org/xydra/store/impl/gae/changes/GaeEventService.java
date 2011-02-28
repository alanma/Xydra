package org.xydra.store.impl.gae.changes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.XI;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.GaeUtils.AsyncEntity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


public class GaeEventService {
	
	/**
	 * GAE Property key for the number of events that are associated with this
	 * change.
	 * 
	 * Set when entering {@link #STATUS_EXECUTING}, never removed.
	 */
	private static final String PROP_EVENTCOUNT = "eventCount";
	
	// GAE Entity (type=XEVENT) property keys.
	
	/**
	 * GAE Property key for the content of an individual event.
	 */
	private static final String PROP_EVENTCONTENT = "eventContent";
	
	public static class AsyncAtomicEvent {
		
		private final XAddress modelAddr;
		private AsyncEntity future;
		private final long rev;
		private XAtomicEvent event;
		
		private AsyncAtomicEvent(XAddress modelAddr, AsyncEntity future, long rev) {
			this.modelAddr = modelAddr;
			this.future = future;
			this.rev = rev;
		}
		
		public XAtomicEvent get() {
			
			if(this.future != null) {
				
				Entity eventEntity = this.future.get();
				if(eventEntity == null) {
					return null;
				}
				Text eventData = (Text)eventEntity.getProperty(PROP_EVENTCONTENT);
				
				MiniElement eventElement = new MiniXMLParserImpl().parseXml(eventData.getValue());
				
				this.event = XmlEvent.toAtomicEvent(eventElement, this.modelAddr);
				
				assert this.event.getRevisionNumber() == this.rev;
				
				this.future = null;
			}
			
			return this.event;
		}
		
	}
	
	/**
	 * @param revisionNumber The revision number of the change the event is part
	 *            of.
	 * @param transindex The index of the event in the change.
	 * @return the {@link XAtomicEvent} with the given index in the change with
	 *         the given revisionNumber.
	 */
	protected static AsyncAtomicEvent getAtomicEvent(XAddress modelAddr, long revisionNumber,
	        int transindex) {
		
		Key changeKey = KeyStructure.createChangeKey(modelAddr, revisionNumber);
		Key eventKey = KeyStructure.createEventKey(changeKey, transindex);
		
		return new AsyncAtomicEvent(modelAddr, GaeUtils.getEntityAsync(eventKey), revisionNumber);
	}
	
	protected static void saveEvents(XAddress modelAddr, Entity changeEntity,
	        List<XAtomicEvent> events) {
		
		@SuppressWarnings("unchecked")
		Future<Key>[] futures = (Future<Key>[])new Future<?>[events.size()];
		
		Key baseKey = changeEntity.getKey();
		for(int i = 0; i < events.size(); i++) {
			XAtomicEvent ae = events.get(i);
			assert (events.size() == 1) ^ ae.inTransaction();
			Entity eventEntity = new Entity(KeyStructure.createEventKey(baseKey, i));
			
			// IMPROVE save event in a GAE-specific format:
			// - don't save the "oldValue" again
			// - don't save the actor again, as it's already in the change
			// entity
			// - don't save the model rev, as it is already in the key
			XmlOutStringBuffer out = new XmlOutStringBuffer();
			XmlEvent.toXml(ae, out, modelAddr);
			Text text = new Text(out.getXml());
			eventEntity.setUnindexedProperty(PROP_EVENTCONTENT, text);
			
			/*
			 * Ignore if we are in danger of timing out (by not calling
			 * giveUpIfTimeoutCritical()), as the events won't be read by anyone
			 * until the change's status is set to STATUS_EXECUTING (or
			 * STATUS_SUCCESS_EXECUTED)
			 */
			futures[i] = GaeUtils.putEntityAsync(eventEntity);
			
		}
		
		for(Future<Key> future : futures) {
			GaeUtils.waitFor(future);
		}
		
		Integer eventCount = events.size();
		changeEntity.setUnindexedProperty(PROP_EVENTCOUNT, eventCount);
	}
	
	/**
	 * Load the individual events associated with the given change.
	 * 
	 * @param change The change whose events should be loaded.
	 * @return a List of {@link XAtomicEvent} which is stored as a number of GAE
	 *         entities
	 */
	protected static List<XAtomicEvent> loadEvents(XAddress modelAddr, Entity changeEntity, long rev) {
		
		assert changeEntity.getProperty(PROP_EVENTCOUNT) != null;
		
		List<XAtomicEvent> events = new ArrayList<XAtomicEvent>();
		
		int eventCount = getEventCount(changeEntity);
		
		for(int i = 0; i < eventCount; i++) {
			events.add(getAtomicEvent(modelAddr, rev, i).get());
		}
		
		return events;
	}
	
	protected static XEvent asEvent(XAddress modelAddr, long rev, XID actor, Entity changeEntity) {
		
		int eventCount = getEventCount(changeEntity);
		assert eventCount > 0 : "executed changes should have at least one event";
		
		if(eventCount > 1) {
			
			AsyncAtomicEvent[] events = new AsyncAtomicEvent[eventCount];
			for(int i = 0; i < eventCount; i++) {
				events[i] = getAtomicEvent(modelAddr, rev, i);
			}
			
			return new GaeTransactionEvent(modelAddr, events, actor, rev);
			
		} else {
			
			XAtomicEvent ae = getAtomicEvent(modelAddr, rev, 0).get();
			assert ae != null;
			assert XI.equals(actor, ae.getActor());
			assert modelAddr.equalsOrContains(ae.getChangedEntity());
			assert ae.getChangeType() != ChangeType.TRANSACTION;
			assert !ae.inTransaction();
			return ae;
			
		}
		
	}
	
	/**
	 * @return the number of {@link XAtomicEvent}s associated with the given
	 *         change {@link Entity}.
	 */
	private static int getEventCount(Entity changeEntity) {
		Number n = (Number)changeEntity.getProperty(PROP_EVENTCOUNT);
		if(n == null) {
			return 0;
		}
		return n.intValue();
	}
	
}
