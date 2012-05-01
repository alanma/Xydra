package org.xydra.webadmin;

import java.io.Serializable;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.gae.AboutAppEngine;
import org.xydra.gae.UniversalTaskQueue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeConstants;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.webadmin.ModelResource.MStyle;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


public class SerialisationCache {
	
	public static final Logger log = LoggerFactory.getLogger(SerialisationCache.class);
	
	protected static final String PREFIX = "Serialised-";
	
	public static UniCache<SerialisationCache.ModelEntry> MODELS = new UniCache<SerialisationCache.ModelEntry>(
	        new SerialisationCache.ModelEntryCacheHandler());
	
	/**
	 * A cached, serialised model
	 * 
	 * Key = ModelAddress
	 */
	public static class ModelEntry implements Serializable {
		private static final long serialVersionUID = -5945996390176658690L;
		
		public void init(long rev, String serialisation) {
			this.rev = rev;
			this.serialisation = serialisation;
		}
		
		public long getRev() {
			return this.rev;
		}
		
		public String getSerialisation() {
			return this.serialisation;
		}
		
		private long rev;
		private String serialisation;
	}
	
	public static class ModelEntryCacheHandler implements UniCache.CacheEntryHandler<ModelEntry> {
		
		private static final String PROP_REV = "rev";
		private static final String PROP_SERIALISATION = "serial";
		
		@Override
		public Entity toEntity(Key datastoreKey, ModelEntry entry) {
			Entity e = new Entity(datastoreKey);
			e.setUnindexedProperty(PROP_REV, entry.rev);
			Text text = new Text(entry.serialisation);
			e.setUnindexedProperty(PROP_SERIALISATION, text);
			return e;
		}
		
		@Override
		public ModelEntry fromEntity(Entity entity) {
			ModelEntry e = new ModelEntry();
			e.rev = (Long)entity.getProperty(PROP_REV);
			e.serialisation = ((Text)entity.getProperty(PROP_SERIALISATION)).getValue();
			// TODO this fix required?
			if(e.serialisation == null) {
				e.serialisation = "";
			}
			return e;
		}
		
		@Override
		public Serializable toSerializable(ModelEntry entry) {
			return entry;
		}
		
		@Override
		public ModelEntry fromSerializable(Serializable s) {
			return (ModelEntry)s;
		}
		
	}
	
	/**
	 * @param repoId ..
	 * @param modelIdList which models to compute a serialisation of
	 * @param style for export
	 * @param giveUp
	 * @param useTaskQueue if many models
	 * @param cacheInInstance ..
	 * @param cacheInMemcache ..
	 * @param cacheInDatastore ..
	 * @return true if all models are now up-to-date in cache and no task queue
	 *         is working on it
	 */
	public static boolean updateAllModels(final XID repoId, List<XID> modelIdList,
	        final MStyle style, boolean giveUp, boolean useTaskQueue,
	        final boolean cacheInInstance, final boolean cacheInMemcache,
	        final boolean cacheInDatastore) {
		log.info("Updating " + modelIdList.size() + " models. Options: useTaskQueue="
		        + useTaskQueue + "; cacheInInstance=" + cacheInInstance + "; cacheInMemcache="
		        + cacheInMemcache + "; cacheInDatastore=" + cacheInDatastore + "<br/>\n");
		Progress p = new Progress();
		p.startTime();
		/*
		 * TODO by adding a batch-get-current-model-rev method to xydra-gae this
		 * could be significantly speed-up
		 */
		for(final XID modelId : modelIdList) {
			log.info("Updating " + modelId);
			
			// first round?
			if(p.getProgress() > 3) {
				// check if enough time is left
				long msLeftToGo = p.willTakeMsUntilProgressIs(modelIdList.size());
				long msWeRanAlready = p.getMsSinceStart();
				long totalTimeRequired = msWeRanAlready + msLeftToGo;
				if(giveUp && totalTimeRequired + 2000 > GaeConstants.GAE_WEB_REQUEST_TIMEOUT) {
					log.warn("We won't make it if we continue like this. Total time required = "
					        + totalTimeRequired + " ms. Giving up.");
					return false;
				}
				if(!AboutAppEngine.onBackend()
				        && msWeRanAlready + 2000 > GaeConstants.GAE_WEB_REQUEST_TIMEOUT) {
					log.warn("Only 2 seconds left. Total time required = " + totalTimeRequired
					        + " ms. Giving up.");
					return false;
				}
			}
			// proceed as normal
			XAddress modelAddress = XX.resolveModel(repoId, modelId);
			String key = PREFIX + modelAddress;
			StorageOptions storeOpts = StorageOptions.create(cacheInInstance, cacheInMemcache,
			        cacheInDatastore);
			
			log.info("Inspecting serialisation of " + modelAddress);
			XydraPersistence persistence = Utils.getPersistence(repoId);
			ModelEntry cached = MODELS.get(key, storeOpts);
			if(cached != null
			        && cached.getRev() == persistence
			                .getModelRevision(
			                        new GetWithAddressRequest(modelAddress,
			                                ModelResource.INCLUDE_TENTATIVE)).revision()) {
				// cache is up-to-date
			} else {
				// re-compute
				UniversalTaskQueue.NamedDeferredTask task = new UniversalTaskQueue.NamedDeferredTask() {
					
					private static final long serialVersionUID = 1L;
					
					@Override
					public void run() {
						XAddress modelAddress = XX.resolveModel(repoId, modelId);
						String key = PREFIX + modelAddress;
						StorageOptions storeOpts = StorageOptions.create(cacheInInstance,
						        cacheInMemcache, cacheInDatastore);
						
						log.info("Computing serialisation of " + modelAddress);
						XydraPersistence persistence = Utils.getPersistence(repoId);
						XWritableModel model = persistence
						        .getModelSnapshot(new GetWithAddressRequest(modelAddress,
						                ModelResource.INCLUDE_TENTATIVE));
						long rev = model.getRevisionNumber();
						String ser = ModelResource.computeSerialisation(model, style);
						if(ser == null) {
							log.warn("Serialisation of model " + modelAddress + " is null");
						}
						ModelEntry modelEntry = new ModelEntry();
						modelEntry.init(rev, ser);
						MODELS.put(key, modelEntry, storeOpts);
					}
					
					@Override
					public String getId() {
						return modelId.toString();
					}
				};
				if(useTaskQueue) {
					UniversalTaskQueue.enqueueTask(task);
				} else {
					task.run();
				}
			}
			p.makeProgress(1);
		}
		log.info("Processed " + p.getProgress() + " models out of " + modelIdList.size());
		return !useTaskQueue;
	}
	
	public static String getSerialisation(XAddress modelAddress, StorageOptions storeOpts) {
		String key = PREFIX + modelAddress;
		ModelEntry modelEntry = SerialisationCache.MODELS.get(key, storeOpts);
		if(modelEntry == null) {
			log.debug("Cache was null for " + modelAddress + ". Options used: " + storeOpts);
			return null;
		}
		if(modelEntry.serialisation == null) {
			log.debug("Serialisation in cache was null for " + modelAddress + ". Options used: "
			        + storeOpts);
			return null;
		}
		return modelEntry.serialisation;
	}
}
