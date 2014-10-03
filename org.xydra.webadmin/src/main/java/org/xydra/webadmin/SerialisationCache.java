package org.xydra.webadmin;

import java.io.Serializable;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.utils.Progress;
import org.xydra.store.impl.gae.UniCache;
import org.xydra.store.impl.gae.UniCache.StorageOptions;
import org.xydra.webadmin.ModelResource.MStyle;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;
import org.xydra.xgae.datastore.api.SText;
import org.xydra.xgae.gaeutils.AboutAppEngine;
import org.xydra.xgae.gaeutils.GaeConstants;
import org.xydra.xgae.gaeutils.UniversalTaskQueue;

/**
 * Stores serialised models
 * 
 * @author xamde
 */
public class SerialisationCache {

	public static final Logger log = LoggerFactory.getLogger(SerialisationCache.class);

	public static UniCache<SerialisationCache.ModelEntry> MODELS = new UniCache<SerialisationCache.ModelEntry>(
			new SerialisationCache.ModelEntryCacheHandler(), "SERIALMODEL");

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
		public SEntity toEntity(SKey datastoreKey, ModelEntry entry) {
			SEntity e = XGae.get().datastore().createEntity(datastoreKey);
			e.setAttribute(PROP_REV, entry.rev);
			SText text = XGae.get().datastore().createText(entry.serialisation);
			e.setAttribute(PROP_SERIALISATION, text);
			return e;
		}

		@Override
		public ModelEntry fromEntity(SEntity entity) {
			ModelEntry e = new ModelEntry();
			e.rev = (Long) entity.getAttribute(PROP_REV);
			e.serialisation = ((SText) entity.getAttribute(PROP_SERIALISATION)).getValue();
			return e;
		}

		@Override
		public Serializable toSerializable(ModelEntry entry) {
			return entry;
		}

		@Override
		public ModelEntry fromSerializable(Serializable s) {
			return (ModelEntry) s;
		}

	}

	private static String toKey(XAddress modelAddress) {
		return modelAddress.toString();
	}

	/**
	 * Makes sure all models have a serialisation in the cache
	 * 
	 * @param repoId
	 *            ..
	 * @param modelIdList
	 *            which models to compute a serialisation of
	 * @param style
	 *            for export
	 * @param giveUp
	 * @param useTaskQueue
	 *            if many models
	 * @param cacheInInstance
	 *            ..
	 * @param cacheInMemcache
	 *            ..
	 * @param cacheInDatastore
	 *            ..
	 * @return true if all models are now up-to-date in cache and no task queue
	 *         is working on it
	 */
	public static boolean updateAllModels(final XId repoId, List<XId> modelIdList,
			final MStyle style, boolean giveUp, boolean useTaskQueue,
			final boolean cacheInInstance, final boolean cacheInMemcache,
			final boolean cacheInDatastore) {
		log.info("Updating " + modelIdList.size() + " models. Options: useTaskQueue="
				+ useTaskQueue + "; cacheInInstance=" + cacheInInstance + "; cacheInMemcache="
				+ cacheInMemcache + "; cacheInDatastore=" + cacheInDatastore + "<br/>\n");

		if (!cacheInInstance && !cacheInMemcache && !cacheInDatastore) {
			log.info("All storeOpts say: Don't persist, so assuming models later get serialised on the fly");
			return true;
		}

		Progress p = new Progress();
		p.startTime();
		/*
		 * IMPROVE by adding a batch-get-current-model-rev method to xydra-gae
		 * this could be significantly speed-up
		 */
		for (final XId modelId : modelIdList) {
			log.info("Updating " + modelId);

			// first round?
			if (p.getProgress() > 3) {
				// check if enough time is left
				long msLeftToGo = p.willTakeMsUntilProgressIs(modelIdList.size());
				long msWeRanAlready = p.getMsSinceStart();
				long totalTimeRequired = msWeRanAlready + msLeftToGo;
				if (giveUp && totalTimeRequired + 2000 > GaeConstants.GAE_WEB_REQUEST_TIMEOUT) {
					log.warn("We won't make it if we continue like this. Total time required = "
							+ totalTimeRequired + " ms. Giving up.");
					return false;
				}
				if (!AboutAppEngine.onBackend()
						&& msWeRanAlready + 2000 > GaeConstants.GAE_WEB_REQUEST_TIMEOUT) {
					log.warn("Only 2 seconds left. Total time required = " + totalTimeRequired
							+ " ms. Giving up.");
					return false;
				}
			}
			// proceed as normal
			XAddress modelAddress = XX.resolveModel(repoId, modelId);
			StorageOptions storeOpts = StorageOptions.create(cacheInInstance ? 1 : 0,
					cacheInMemcache, cacheInDatastore, false);

			log.info("Inspecting serialisation of " + modelAddress);
			XydraPersistence persistence = Utils.createPersistence(repoId);
			ModelEntry cached = MODELS.get(toKey(modelAddress), storeOpts);
			if (cached != null
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
						StorageOptions storeOpts = StorageOptions.create(cacheInInstance ? 1 : 0,
								cacheInMemcache, cacheInDatastore, false);

						ModelEntry modelEntry = computeSerialisation(modelAddress, style);
						MODELS.put(toKey(modelAddress), modelEntry, storeOpts);
					}

					@Override
					public String getId() {
						return modelId.toString();
					}
				};
				if (useTaskQueue) {
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

	private static ModelEntry computeSerialisation(XAddress modelAddress, MStyle style) {
		log.info("Computing serialisation of " + modelAddress);
		XydraPersistence persistence = Utils.createPersistence(modelAddress.getRepository());
		XWritableModel model = persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress,
				ModelResource.INCLUDE_TENTATIVE));
		long rev = model.getRevisionNumber();
		String ser = ModelResource.computeSerialisation(model, style);
		if (ser == null) {
			log.warn("Serialisation of model " + modelAddress + " is null");
		}
		ModelEntry modelEntry = new ModelEntry();
		modelEntry.init(rev, ser);
		return modelEntry;
	}

	public static String getSerialisation(XAddress modelAddress, StorageOptions storeOpts) {
		ModelEntry modelEntry = SerialisationCache.MODELS.get(toKey(modelAddress), storeOpts);
		if (modelEntry == null) {
			log.debug("Cache was null for " + modelAddress + ". Options used: " + storeOpts);
			if (storeOpts.isComputeIfNull()) {
				ModelEntry onTheFly = computeSerialisation(modelAddress, MStyle.xml);
				return onTheFly.serialisation;
			} else {
				return null;
			}
		}
		if (modelEntry.serialisation == null) {
			log.debug("Serialisation in cache was null for " + modelAddress + ". Options used: "
					+ storeOpts);
			return null;
		}
		return modelEntry.serialisation;
	}
}
