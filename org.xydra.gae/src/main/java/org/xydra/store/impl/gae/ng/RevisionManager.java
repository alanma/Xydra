package org.xydra.store.impl.gae.ng;

import org.xydra.base.XAddress;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.ng.Algorithms.Effect;


/**
 * TODO make thread-safe
 * 
 * TODO share among threads
 * 
 * 
 * 
 * @author xamde
 */
public class RevisionManager {
	
	private static final String KEY_VERSION_ID = "NG";
	
	private XAddress modelAddress;
	
	private GaeModelRevInfo revision;
	
	private UniCache<GaeModelRevInfo> unicache = new UniCache<GaeModelRevInfo>(
	        UniCacheRevisionInfoEntryHandler.instance());
	
	public RevisionManager(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.revision = null;
	}
	
	public void foundNewHigherCommitedChange(GaeChange change) {
		XyAssert.xyAssert(change.getStatus().isCommitted());
		long rev = change.rev;
		Effect modelExistsEffect = Algorithms.effectOnModelExists(change);
		
		if(change.getStatus() == Status.SuccessExecuted) {
			if(this.revision.getLastStableCommitted() + 1 == rev) {
				this.revision.incrementLastStableSuccessChange(rev);
				XyAssert.xyAssert(modelExistsEffect != Effect.NoEffect);
				this.revision.setModelExists(Effect.modelExists(modelExistsEffect));
				writeToDatastoreAndMemcache();
			} else {
				this.revision.incrementLastSuccessChange(rev);
			}
		} else {
			if(this.revision.getLastCommitted() + 1 == rev) {
				this.revision.incrementLastStableCommitted(rev);
				writeToDatastoreAndMemcache();
			} else {
				this.revision.incrementLastTaken(rev);
			}
		}
	}
	
	public void foundNewLastTaken(long rev) {
		this.revision.incrementLastTaken(rev);
	}
	
	public GaeModelRevInfo getInfo() {
		if(this.revision == null) {
			readFromDatastoreAndMemcache();
		}
		if(this.revision == null) {
			this.revision = GaeModelRevInfo.createModelDoesNotExist();
		}
		XyAssert.xyAssert(this.revision != null);
		return this.revision;
	}
	
	public void readFromDatastoreAndMemcache() {
		boolean instance = false;
		boolean memcache = false;
		boolean datastore = true;
		GaeModelRevInfo value = this.unicache.get(this.modelAddress + "/" + KEY_VERSION_ID,
		        UniCache.StorageOptions.create(instance, memcache, datastore));
		if(value != null) {
			if(this.revision == null) {
				this.revision = value;
			} else {
				this.revision.incrementFrom(value);
			}
		}
	}
	
	public void writeToDatastoreAndMemcache() {
		/* dont overwrite good information: get first whats out there */
		if(this.revision == null) {
			readFromDatastoreAndMemcache();
		}
		boolean instance = false;
		boolean memcache = false;
		boolean datastore = this.revision.getLastStableSuccessChange() % 32 == 0;
		this.unicache.put(this.modelAddress + "/" + KEY_VERSION_ID, this.revision,
		        UniCache.StorageOptions.create(instance, memcache, datastore));
	}
	
}
