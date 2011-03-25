package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.store.impl.gae.GaeUtils;
import org.xydra.store.impl.gae.GaeUtils.AsyncEntity;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Helper class to allow to load events asynchronously.
 * 
 * @author dscharrer
 * 
 */
public class AsyncChange {
	
	private final XAddress modelAddr;
	private final AsyncEntity future;
	private final long rev;
	private GaeChange change;
	
	/**
	 * Get the change at the specified revision number.
	 */
	protected AsyncChange(XAddress modelAddr, long rev) {
		
		this.modelAddr = modelAddr;
		this.rev = rev;
		
		Key key = KeyStructure.createChangeKey(modelAddr, rev);
		this.future = GaeUtils.getEntityAsync(key);
	}
	
	/**
	 * @return the XEvent represented by the retrieved change entity or null of
	 *         nothing changed.
	 */
	public GaeChange get() {
		
		if(this.change == null) {
			Entity changeEntity = this.future.get();
			if(changeEntity == null) {
				return null;
			}
			this.change = new GaeChange(this.modelAddr, this.rev, changeEntity);
		}
		return this.change;
	}
	
}
