package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.GaeUtils.AsyncEntity;
import org.xydra.store.impl.gae.changes.GaeChange.Status;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Helper class to allow to load events asynchronously.
 * 
 * @author dscharrer
 * 
 */
public class AsyncEvent {
	
	private final XAddress modelAddr;
	private final AsyncEntity future;
	private final long rev;
	private XEvent event;
	
	protected AsyncEvent(XAddress modelAddr, long rev) {
		
		this.modelAddr = modelAddr;
		this.rev = rev;
		
		Key key = KeyStructure.createChangeKey(modelAddr, rev);
		this.future = GaeUtils.getEntityAsync(key);
	}
	
	/**
	 * @return the retrieved change entity.
	 */
	public Entity getEntity() {
		return this.future.get();
	}
	
	/**
	 * @return the XEvent represented by the retrieved change entity or null of
	 *         nothing changed.
	 */
	public XEvent get() {
		
		Entity changeEntity = this.future.get();
		if(changeEntity == null) {
			return null;
		}
		
		if(!Status.hasEvents(GaeChange.getStatus(changeEntity))) {
			// no events available (or not yet) for this revision.
			return null;
		}
		
		XID actor = GaeChange.getActor(changeEntity);
		
		this.event = GaeEventService.asEvent(this.modelAddr, this.rev, actor, changeEntity);
		assert this.event != null;
		
		return this.event;
	}
	
}
