package org.xydra.store.impl.gae.ng;

import org.xydra.base.XAddress;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision;

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

	private static final Logger log = LoggerFactory.getLogger(RevisionManager.class);

	public static final long WRITE_REV_EVERY = 16;

	private XAddress modelAddress;

	private GaeModelRevInfo revision;

	private UniCache<GaeModelRevInfo> uniRevCache;

	public RevisionManager(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.revision = null;
		this.uniRevCache = new UniCache<GaeModelRevInfo>(
				UniCacheRevisionInfoEntryHandler.instance(), "CACHEDREV");
	}

	/**
	 * Takes cares of degrating precision
	 * 
	 * @param change
	 */
	public void foundNewHigherCommitedChange(GaeChange change) {
		XyAssert.xyAssert(!change.getStatus().canChange());

		if (change.getStatus().changedSomething()) {
			if (this.revision.getLastStableCommitted() + 1 == change.rev) {
				boolean modelExists = Algorithms.changeIndicatesModelExists(change);
				// IMPROVE use exact change read time instead of NOW
				this.revision.incrementLastStableSuccessChange(change.rev, modelExists,
						System.currentTimeMillis());
				this.revision.setPrecisionToImprecise();
				writeToDatastoreAndMemcache();
			} else {
				this.revision.incrementLastSuccessChange(change.rev);
				this.revision.setPrecisionToImprecise();
			}
		} else {
			if (this.revision.getLastStableCommitted() + 1 == change.rev) {
				this.revision.incrementLastStableCommitted(change.rev);
				this.revision.setPrecisionToImprecise();
				writeToDatastoreAndMemcache();
			} else {
				this.revision.incrementLastTaken(change.rev);
			}
		}
	}

	public void foundNewLastTaken(long rev) {
		this.revision.incrementLastTaken(rev);
	}

	/**
	 * @return a reference to <em>the</em> revision info of this model
	 */
	public GaeModelRevInfo getInfo() {
		if (this.revision == null) {
			readFromDatastoreAndMemcache();
		}
		if (this.revision == null) {
			this.revision = GaeModelRevInfo.createModelDoesNotExist();
		}
		XyAssert.xyAssert(this.revision != null);

		// log.debug("Return " + this.revision + " for " + this.modelAddress);

		// FIXME GAE !!!!!!!!!!!!!
		// check if still current
		// if(this.revision.getPrecision() == Precision.Precise) {
		// ChangeLogManager clm = new ChangeLogManager(this.modelAddress);
		// long rev = this.revision.getLastStableSuccessChange() + 1;
		// GaeChange chg = clm.getChange(rev);
		// XyAssert.xyAssert(chg == null || !chg.getStatus().changedSomething()
		// || chg.getStatus().canChange(),
		// "revMan say %s but change at %s is %s",
		// this.revision, rev, chg);
		// }

		return this.revision;
	}

	public void readFromDatastoreAndMemcache() {
		boolean memcache = true;
		boolean datastore = true;
		GaeModelRevInfo value = this.uniRevCache.get("" + this.modelAddress,
				UniCache.StorageOptions.create(0, memcache, datastore, false));
		if (value != null) {
			if (this.revision == null) {
				log.debug("Got rev " + value + " from unicache => using it");
				this.revision = value;
			} else {
				log.debug("Got rev " + value + " from unicache -> updating from " + this.revision);
				this.revision.incrementFrom(value);
			}
			value.setPrecision(Precision.Loaded);
		}
	}

	public void writeToDatastoreAndMemcache() {
		/* dont overwrite good information: get first whats out there */
		if (this.revision == null) {
			readFromDatastoreAndMemcache();
		}
		boolean memcache = true;
		boolean datastore = this.revision.getLastStableSuccessChange() % WRITE_REV_EVERY == 0;
		this.uniRevCache.put("" + this.modelAddress, this.revision,
				UniCache.StorageOptions.create(0, memcache, datastore, false));
	}

}
