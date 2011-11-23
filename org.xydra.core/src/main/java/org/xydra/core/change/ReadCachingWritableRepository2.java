package org.xydra.core.change;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * Always uses full-model prefetching directly from {@link XydraPersistence} via
 * snapshots.
 * 
 * Writes to models are lost.
 * 
 * @author xamde
 */
public class ReadCachingWritableRepository2 implements XWritableRepository {
	
	private static final Logger log = LoggerFactory.getLogger(ReadCachingWritableRepository2.class);
	
	@Override
	public XAddress getAddress() {
		return XX.toAddress(getID(), null, null, null);
	}
	
	@Override
	public XID getID() {
		return this.persistence.getRepositoryId();
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
	private boolean knowsAllModelsIds = false;
	
	/** Only used for fast prefetching */
	private XydraPersistence persistence;
	
	/**
	 * Loads state much faster directly from snapshots.
	 * 
	 * @param persistence where to load snapshots from
	 */
	public ReadCachingWritableRepository2(XydraPersistence persistence) {
		this.persistence = persistence;
	}
	
	protected Set<XID> knownExistingModelIds = new HashSet<XID>();
	
	protected Set<XID> knownNonExistingModelIds = new HashSet<XID>();
	
	protected Map<XID,XWritableModel> map = new HashMap<XID,XWritableModel>();
	
	@Override
	public XWritableModel createModel(XID modelId) {
		XWritableModel readCachingModel = getModel(modelId);
		if(readCachingModel == null) {
			throw new RuntimeException("This ReadCachingWritableRepository2 cannot create models");
		}
		return readCachingModel;
	}
	
	@Override
	public Iterator<XID> iterator() {
		return modelIds().iterator();
	}
	
	private Set<XID> modelIds() {
		if(!this.knowsAllModelsIds) {
			this.knownExistingModelIds = this.persistence.getManagedModelIds();
			this.knowsAllModelsIds = true;
		}
		return this.knownExistingModelIds;
	}
	
	@Override
	public boolean hasModel(XID modelId) {
		return getModel(modelId) != null;
		/*
		 * This impl triggers gae.changes.Utils:73 findChildren() = a query that
		 * is not cached.
		 * 
		 * return modelIds().contains(modelId);
		 */
	}
	
	@Override
	public boolean isEmpty() {
		return modelIds().isEmpty();
	}
	
	@Override
	public XWritableModel getModel(XID modelId) {
		XWritableModel model = this.map.get(modelId);
		if(model == null) {
			/* prevent asking base again for models that simply don't exists */
			if(this.knownNonExistingModelIds.contains(model)) {
				return null;
			}
			/* ask base */
			model = this.persistence.getModelSnapshot(XX.toAddress(getID(), modelId, null, null));
			if(model != null) {
				log.info("Model '" + modelId + "' not found in cache. Loaded snapshot rev "
				        + model.getRevisionNumber() + ".");
				this.map.put(modelId, model);
				return model;
			} else {
				this.knownNonExistingModelIds.add(modelId);
				log.info("Model '" + modelId + "' not found in cache & no snapshot.");
			}
		} else {
			log.info("Loaded model from cache in rev " + model.getRevisionNumber()
			        + ", current rev in persistence is "
			        + this.persistence.getModelRevision(XX.toAddress(getID(), modelId, null, null)));
		}
		return model;
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		throw new RuntimeException("This repo cannot do this");
	}
	
}
