package org.xydra.store.impl.gae.changes;

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
	
	private final GaeChangesService gcs;
	private final AsyncEntity future;
	private final long rev;
	private GaeChange change;
	
	/**
	 * Get the change at the specified revision number.
	 */
	protected AsyncChange(GaeChangesService gcs, long rev) {
		
		this.gcs = gcs;
		this.rev = rev;
		
		Key key = KeyStructure.createChangeKey(gcs.getModelAddress(), rev);
		this.future = GaeUtils.getEntityAsync(key);
	}
	
	protected AsyncChange(GaeChange change) {
		assert change != null;
		this.change = change;
		this.future = null;
		this.rev = -1;
		this.gcs = null;
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
			this.change = new GaeChange(this.gcs.getModelAddress(), this.rev, changeEntity);
			if(this.change.getStatus().isCommitted()) {
				this.gcs.cacheCommittedChange(this.change);
			}
		}
		return this.change;
	}
	
}
