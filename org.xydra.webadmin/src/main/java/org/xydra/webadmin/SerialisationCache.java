package org.xydra.webadmin;

import java.io.Serializable;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.util.Clock;
import org.xydra.gae.UniversalTaskQueue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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
		
		@Override
		public Entity toEntity(Key datastoreKey, ModelEntry entry) {
			Entity e = new Entity(datastoreKey);
			e.setUnindexedProperty("rev", entry.rev);
			Text text = new Text(entry.serialisation);
			e.setUnindexedProperty("serialisation", text);
			return e;
		}
		
		@Override
		public ModelEntry fromEntity(Entity entity) {
			ModelEntry e = new ModelEntry();
			e.rev = (Long)entity.getProperty("rev");
			e.serialisation = ((Text)entity.getProperty("serialisation")).getValue();
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
	
	public static class Progress {
		private Clock clock = new Clock();
		private long count = 0;
		
		public void startTime() {
			this.clock.start();
		}
		
		public void makeProgress(long howMuch) {
			this.count += howMuch;
		}
		
		public double getMsPerProgress() {
			return ((double)this.clock.getDurationSinceStart()) / ((double)this.count);
		}
		
		public long willTakeMsUntilProgressIs(long howMuchTotal) {
			return (long)(howMuchTotal * getMsPerProgress());
		}
		
		public long getProgress() {
			return this.count;
		}
		
		public long getMsSinceStart() {
			return this.clock.getDurationSinceStart();
		}
		
		@Override
		public String toString() {
			return "Running for " + getMsSinceStart() + "ms and made " + this.count
			        + " progress; that's " + getMsPerProgress() + "ms for each.";
			
		}
	}
	
	/**
	 * @param repoId ..
	 * @param modelIdList which models to compute a serialisation of
	 * @param style for export
	 * @param useTaskQueue if many models
	 * @param cacheInInstance ..
	 * @param cacheInMemcache ..
	 * @param cacheInDatastore ..
	 * @return true if all models are now up-to-date in cache and no task queue
	 *         is working on it
	 */
	public static boolean updateAllModels(final XID repoId, List<XID> modelIdList,
	        final MStyle style, boolean useTaskQueue, final boolean cacheInInstance,
	        final boolean cacheInMemcache, final boolean cacheInDatastore) {
		log.info("Options: useTaskQueue=" + useTaskQueue + "; cacheInInstance=" + cacheInInstance
		        + "; cacheInMemcache=" + cacheInMemcache + "; cacheInDatastore=" + cacheInDatastore
		        + "<br/>\n");
		Progress p = new Progress();
		p.startTime();
		for(final XID modelId : modelIdList) {
			
			UniversalTaskQueue.NamedDeferredTask task = new UniversalTaskQueue.NamedDeferredTask() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public void run() {
					XAddress modelAddress = XX.resolveModel(repoId, modelId);
					XydraPersistence persistence = Utils.getPersistence(repoId);
					XWritableModel model = persistence.getModelSnapshot(modelAddress);
					String key = PREFIX + modelAddress;
					StorageOptions storeOpts = StorageOptions.create(cacheInInstance,
					        cacheInMemcache, cacheInDatastore);
					ModelEntry modelEntry = new ModelEntry();
					log.info("Computing serialisation of " + modelAddress + "["
					        + model.getRevisionNumber() + "]");
					modelEntry.serialisation = ModelResource.computeSerialisation(model, style);
					modelEntry.rev = model.getRevisionNumber();
					MODELS.put(key, modelEntry, storeOpts);
				}
				
				@Override
				public String getId() {
					return modelId.toString();
				}
			};
			
			// first round?
			if(p.getProgress() > 3) {
				// check if enough time is left
				long msLeftToGo = p.willTakeMsUntilProgressIs(modelIdList.size());
				long msWeRanAlready = p.getMsSinceStart();
				long totalTimeRequired = msWeRanAlready + msLeftToGo;
				if(totalTimeRequired + 2000 > GaeConstants.GAE_WEB_REQUEST_TIMEOUT) {
					log.warn("We won't make it if we continue like this. Total time required = "
					        + totalTimeRequired + " ms. Giving up.");
					return false;
				}
			} else {
				// proceed as normal
				XAddress modelAddress = XX.resolveModel(repoId, modelId);
				log.info("Inspecting serialisation of " + modelAddress);
				XydraPersistence persistence = Utils.getPersistence(repoId);
				String key = PREFIX + modelAddress;
				StorageOptions storeOpts = StorageOptions.create(cacheInInstance, cacheInMemcache,
				        cacheInDatastore);
				ModelEntry cached = MODELS.get(key, storeOpts);
				if(cached != null
				        && cached.getRev() == persistence.getModelRevision(modelAddress).revision()) {
					// cache is up-to-date
				} else {
					// re-compute
					if(useTaskQueue) {
						UniversalTaskQueue.enqueueTask(task);
					} else {
						task.run();
					}
				}
				p.makeProgress(1);
			}
			
		}
		return true;
	}
	
	public static String getSerialisation(XAddress modelAddress, StorageOptions storeOpts) {
		String key = PREFIX + modelAddress;
		ModelEntry modelEntry = SerialisationCache.MODELS.get(key, storeOpts);
		if(modelEntry == null) {
			throw new RuntimeException("memcache was null for " + modelAddress
			        + ". Maybe better use datastore instead?");
		}
		return modelEntry.serialisation;
	}
}
