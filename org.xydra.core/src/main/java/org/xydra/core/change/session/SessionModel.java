package org.xydra.core.change.session;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XSessionModel;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.core.util.Clock;
import org.xydra.core.util.DumpUtils;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.GetWithAddressRequest;


/**
 * Initially this wraps a ModelSnapshot. Changes can virtually be done, but are
 * kept in an internal {@link ChangedModel}. On commit, the internal changes are
 * written to the underlying persistence in a single transaction.
 * 
 * A readable session model can simply be represented by an
 * {@link XReadableModel}.
 * 
 * @author xamde
 * 
 */
public class SessionModel implements XSessionModel {
	
	private static final boolean INCLUDE_TENTATIVE_CHANGES = true;
	
	private static final Logger log = LoggerFactory.getLogger(SessionModel.class);
	
	/* for performance monitoring */
	private Clock clock;
	
	private boolean readonly;
	
	private ChangeSession session;
	
	/* where to buffer changes if not readonly */
	private SessionCachedModel sessionCacheModel = null;
	
	/* for debugging */
	protected final String traceid = UUID.uuid(4);
	
	/**
	 * @param sharedSession
	 * @param address
	 * @param readonly if read-only, model cannot be committed
	 */
	public SessionModel(ChangeSession sharedSession, XAddress address, boolean readonly) {
		log.trace("SessionModel " + this.traceid + " created for '" + address + "'");
		this.clock = new Clock().start();
		this.session = sharedSession;
		this.readonly = readonly;
		this.sessionCacheModel = new SessionCachedModel(address);
	}
	
	public String changesToString() {
		synchronized(this.sessionCacheModel) {
			return DumpUtils.changesToString(this.sessionCacheModel).toString();
		}
	}
	
	public long commitToSessionPersistence() {
		assert !isReadOnly() : "Called commit on model '" + this.getId() + "' that is readonly?"
		        + this.readonly;
		log.debug("Committing SessionModel '" + this.traceid + "' ...");
		synchronized(this.sessionCacheModel) {
			long l = this.getSessionPersistence().applyChangesAsTxn(this.sessionCacheModel,
			        getActorId());
			if(XCommandUtils.success(l)) {
				this.sessionCacheModel.markAsCommitted();
				assert !this.sessionCacheModel.hasChanges();
				// IMPROVE 2012-03 performance: put new snapshot right to
				// memcache?
			} else {
				log.warn("Error while committing session " + this.traceid + " result=" + l
				        + " GA?category=warn&action=commitFailed&label=result&value=" + l);
			}
			long durationInMs = this.clock.stopAndGetDuration("commited");
			if(durationInMs > 10000) {
				log.warn("SessionModel '" + this.traceid + "' ID '" + getId()
				        + "' was open very long: " + durationInMs
				        + " ms GA?category=stats&action=sessionOpenVeryLong&label=time&value="
				        + durationInMs);
			}
			log.info("SessionModel '" + this.traceid + "' committed " + durationInMs
			        + " ms after start.");
			this.clock.start();
			return l;
		}
	}
	
	@Override
	public XWritableObject createObject(XID id) {
		synchronized(this.sessionCacheModel) {
			return this.sessionCacheModel.createObject(id);
		}
	}
	
	/* id to record writes, if not readonly */
	public XID getActorId() {
		return this.session.getActorId();
	}
	
	@Override
	public XAddress getAddress() {
		return this.sessionCacheModel.getAddress();
	}
	
	@Override
	public XID getId() {
		return this.sessionCacheModel.getId();
	}
	
	@Override
	public XWritableObject getObject(XID objectId) {
		synchronized(this.sessionCacheModel) {
			return this.sessionCacheModel.getObject(objectId);
		}
	}
	
	/**
	 * Return undefined. The revision number does not increase with changes to
	 * this {@link SessionModel}.
	 * 
	 * @return undefined
	 */
	@Override
	public long getRevisionNumber() {
		return this.sessionCacheModel.getRevisionNumber();
	}
	
	/* where to commit, if not readonly */
	private ISessionPersistence getSessionPersistence() {
		return this.session.getSessionPersistence();
	}
	
	public String getTraceId() {
		return this.traceid;
	}
	
	@Override
	public XType getType() {
		return XType.XMODEL;
	}
	
	public boolean hasChanges() {
		synchronized(this.sessionCacheModel) {
			return this.sessionCacheModel.hasChanges();
		}
	}
	
	@Override
	public boolean hasObject(XID objectId) {
		return this.sessionCacheModel.hasObject(objectId);
	}
	
	@Override
	public boolean isEmpty() {
		return this.sessionCacheModel.isEmpty();
	}
	
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	@Override
	public Iterator<XID> iterator() {
		Collection<XID> set;
		synchronized(this.sessionCacheModel) {
			set = IteratorUtils.addAll(this.sessionCacheModel.iterator(), new HashSet<XID>());
		}
		return set.iterator();
	}
	
	/**
	 * @param baseModel can be null
	 * @throws IllegalArgumentException if baseModel has a an Id different from
	 *             this SessionModel.
	 */
	public void indexModel(XReadableModel baseModel) throws IllegalArgumentException {
		if(baseModel == null) {
			return;
		}
		if(!this.sessionCacheModel.getId().equals(baseModel.getId())) {
			throw new IllegalArgumentException("Basemodel has a different id '"
			        + baseModel.getAddress() + "' from sessionModel '"
			        + this.sessionCacheModel.getAddress() + "'");
		}
		this.sessionCacheModel.indexModel(baseModel);
	}
	
	/**
	 * @param baseObject can be null
	 * @throws IllegalArgumentException if baseModel has a an Id different from
	 *             this SessionModel.
	 */
	public void indexObject(XReadableObject baseObject) {
		if(baseObject == null) {
			return;
		}
		if(!this.sessionCacheModel.getId().equals(baseObject.getAddress().getModel())) {
			throw new IllegalArgumentException("BaseObject has a different model id '"
			        + baseObject.getAddress() + "' from sessionModel '"
			        + this.sessionCacheModel.getAddress() + "'");
		}
		this.sessionCacheModel.indexObject(baseObject);
	}
	
	@Override
	public boolean removeObject(XID objectId) {
		synchronized(this.sessionCacheModel) {
			return this.sessionCacheModel.removeObject(objectId);
		}
	}
	
	@Override
	public String toString() {
		return this.traceid + " " + this.getId();
	}
	
	@Override
	public SessionModel loadObject(XID objectId) {
		assert objectId != null;
		// load only if not already present
		if(this.sessionCacheModel.isKnownObject(objectId)) {
			return this;
		}
		log.info("Loading object '" + objectId + "' in " + this.getAddress());
		XReadableObject objectSnapshot = this.session.getSessionPersistence().getObjectSnapshot(
		        new GetWithAddressRequest(XX.resolveObject(getAddress(), objectId),
		                INCLUDE_TENTATIVE_CHANGES));
		indexObject(objectSnapshot);
		return this;
	}
	
	@Override
	public SessionModel loadAllObjects() {
		// load only if not already present
		if(this.sessionCacheModel.knowsAllObjects()) {
			return this;
		}
		log.info("Loading all objects in " + this.getAddress());
		XReadableModel baseModel = this.session.getSessionPersistence().getModelSnapshot(
		        new GetWithAddressRequest(getAddress(), INCLUDE_TENTATIVE_CHANGES));
		indexModel(baseModel);
		return this;
	}
	
}
