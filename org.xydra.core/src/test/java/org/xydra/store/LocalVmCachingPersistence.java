package org.xydra.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A wrapper around a {@link XydraPersistence} that caches in the local VM.
 * 
 * This caching has no measurable speed-up and should be considered more a
 * proof-of-concept. Speed-up will only occur if retrieving the current revision
 * number of a model is significantly faster than retrieving the models snapshot
 * itself.
 * 
 * @author xamde
 */
public class LocalVmCachingPersistence implements XydraPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(LocalVmCachingPersistence.class);
	
	/**
	 * Locally cache snapshots
	 * 
	 * FIXME concurrency: access to these maps needs to be synchronized (or need
	 * to use implementations that are already synchronized themselves)
	 */
	private static Map<String,XWritableModel> localVmCache_modelSnapshot = new HashMap<String,XWritableModel>();
	private static Map<String,XWritableObject> localVmCache_objectSnapshot = new HashMap<String,XWritableObject>();
	private static final boolean CACHE_OBJECT_SNAPSHOTS = false;
	
	private XydraPersistence persistence;
	
	/**
	 * @param persistence a {@link XydraPersistence}
	 */
	public LocalVmCachingPersistence(XydraPersistence persistence) {
		super();
		this.persistence = persistence;
	}
	
	private static String toKey(XAddress modelAddress, long rev) {
		assert modelAddress.getAddressedType() != XType.XFIELD;
		// IMPROVE separator needed if this needs to support field addresses
		return modelAddress.toString() + rev;
	}
	
	public void clear() {
		this.persistence.clear();
		localVmCache_modelSnapshot.clear();
	}
	
	public long executeCommand(XID actorId, XCommand command) {
		long result = this.persistence.executeCommand(actorId, command);
		return result;
	}
	
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		return this.persistence.getEvents(address, beginRevision, endRevision);
	}
	
	public Set<XID> getModelIds() {
		return this.persistence.getModelIds();
	}
	
	public long getModelRevision(XAddress address) {
		return this.persistence.getModelRevision(address);
	}
	
	public XWritableModel getModelSnapshot(XAddress address) {
		long currentRevision = getModelRevision(address);
		XWritableModel cachedSnapshot = localVmCache_modelSnapshot.get(toKey(address,
		        currentRevision));
		if(cachedSnapshot != null) {
			log.info("Return cached model snapshot");
			return cachedSnapshot;
		} else {
			XWritableModel loadedSnapshot = this.persistence.getModelSnapshot(address);
			if(loadedSnapshot != null) {
				localVmCache_modelSnapshot.put(toKey(address, loadedSnapshot.getRevisionNumber()),
				        loadedSnapshot);
			}
			return loadedSnapshot;
		}
	}
	
	public XWritableObject getObjectSnapshot(XAddress address) {
		long currentRevision;
		if(CACHE_OBJECT_SNAPSHOTS) {
			currentRevision = getModelSnapshot(address).getObject(address.getObject())
			        .getRevisionNumber();
			XWritableObject cachedSnapshot = localVmCache_objectSnapshot.get(toKey(address,
			        currentRevision));
			if(cachedSnapshot != null) {
				log.trace("Return cached object snapshot");
				return cachedSnapshot;
			}
		}
		XWritableObject loadedSnapshot = this.persistence.getObjectSnapshot(address);
		if(CACHE_OBJECT_SNAPSHOTS) {
			// FIXME snapshot may be for a model revision number newer than
			// currentRevision, but there is no way to get the real revision
			// number
			localVmCache_objectSnapshot.put(toKey(address, currentRevision), loadedSnapshot);
		}
		return loadedSnapshot;
	}
	
	public XID getRepositoryId() {
		XID result = this.persistence.getRepositoryId();
		return result;
	}
	
	public boolean hasModel(XID modelId) {
		return this.persistence.hasModel(modelId);
	}
	
}
