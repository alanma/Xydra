package org.xydra.store.impl.gae.changes;

import java.util.concurrent.Future;

import org.xydra.store.impl.gae.AsyncDatastore;
import org.xydra.store.impl.gae.FutureUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Helper class to allow to load events asynchronously.
 * 
 * Used exclusively with {@link GaeChangesServiceImpl1}.
 * 
 * @author dscharrer
 * 
 */
public class AsyncChange {
	
	private final GaeChangesServiceImpl1 gcs;
	private final Future<Entity> future;
	private final long rev;
	private GaeChange change;
	
	/**
	 * Get the change at the specified revision number.
	 */
	protected AsyncChange(GaeChangesServiceImpl1 gcs, long rev) {
		
		this.gcs = gcs;
		this.rev = rev;
		
		Key key = KeyStructure.createChangeKey(gcs.getModelAddress(), rev);
		this.future = AsyncDatastore.getEntity(key);
	}
	
	protected AsyncChange(GaeChange change) {
		assert change != null;
		this.change = change;
		this.future = null;
		this.rev = -1;
		this.gcs = null;
	}
	
	/**
	 * @return the XEvent represented by the retrieved change entity or null if
	 *         nothing changed.
	 */
	public GaeChange get() {
		
		if(this.change == null) {
			Entity changeEntity = FutureUtils.waitFor(this.future);
			if(changeEntity == null) {
				return null;
			}
			this.change = new GaeChange(this.gcs.getModelAddress(), this.rev, changeEntity);
			this.gcs.cacheCommittedChange(this.change);
		}
		return this.change;
	}
	
}
