package org.xydra.webadmin;

import java.io.Serializable;
import java.util.List;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
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
import org.xydra.xgae.gaeutils.UniversalTaskQueue;

import com.google.apphosting.api.ApiProxy;

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

		public void init(final long rev, final String serialisation) {
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
		public SEntity toEntity(final SKey datastoreKey, final ModelEntry entry) {
			final SEntity e = XGae.get().datastore().createEntity(datastoreKey);
			e.setAttribute(PROP_REV, entry.rev);
			final SText text = XGae.get().datastore().createText(entry.serialisation);
			e.setAttribute(PROP_SERIALISATION, text);
			return e;
		}

		@Override
		public ModelEntry fromEntity(final SEntity entity) {
			final ModelEntry e = new ModelEntry();
			e.rev = (Long) entity.getAttribute(PROP_REV);
			e.serialisation = ((SText) entity.getAttribute(PROP_SERIALISATION)).getValue();
			return e;
		}

		@Override
		public Serializable toSerializable(final ModelEntry entry) {
			return entry;
		}

		@Override
		public ModelEntry fromSerializable(final Serializable s) {
			return (ModelEntry) s;
		}

	}

	private static String toKey(final XAddress modelAddress) {
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
	public static boolean updateAllModels(final XId repoId, final List<XId> modelIdList,
			final MStyle style, final boolean giveUp, final boolean useTaskQueue,
			final boolean cacheInInstance, final boolean cacheInMemcache,
			final boolean cacheInDatastore) {
		log.info("Updating " + modelIdList.size() + " models. Options: useTaskQueue="
				+ useTaskQueue + "; cacheInInstance=" + cacheInInstance + "; cacheInMemcache="
				+ cacheInMemcache + "; cacheInDatastore=" + cacheInDatastore + "<br/>\n");

		if (!cacheInInstance && !cacheInMemcache && !cacheInDatastore) {
			log.info("All storeOpts say: Don't persist, so assuming models later get serialised on the fly");
			return true;
		}

		final Progress p = new Progress();
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
				final long msLeftToGo = p.willTakeMsUntilProgressIs(modelIdList.size());
				final long msWeRanAlready = p.getMsSinceStart();
				final long totalTimeRequired = msWeRanAlready + msLeftToGo;

				final long timeLeft = ApiProxy.getCurrentEnvironment().getRemainingMillis();
				if (giveUp && totalTimeRequired + 2000 < timeLeft) {
					log.warn("We won't make it if we continue like this. Total time required = "
							+ totalTimeRequired + " ms. Giving up.");
					return false;
				}
				if (timeLeft < 2000) {
					log.warn("Only 2 seconds left. Total time required = " + totalTimeRequired
							+ " ms. Giving up.");
					return false;
				}
			}
			// proceed as normal
			final XAddress modelAddress = Base.resolveModel(repoId, modelId);
			final StorageOptions storeOpts = StorageOptions.create(cacheInInstance ? 1 : 0,
					cacheInMemcache, cacheInDatastore, false);

			log.info("Inspecting serialisation of " + modelAddress);
			final XydraPersistence persistence = Utils.createPersistence(repoId);
			final ModelEntry cached = MODELS.get(toKey(modelAddress), storeOpts);
			if (cached != null
					&& cached.getRev() == persistence
							.getModelRevision(
									new GetWithAddressRequest(modelAddress,
											ModelResource.INCLUDE_TENTATIVE)).revision()) {
				// cache is up-to-date
			} else {
				// re-compute
				final UniversalTaskQueue.NamedDeferredTask task = new UniversalTaskQueue.NamedDeferredTask() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						final XAddress modelAddress = Base.resolveModel(repoId, modelId);
						final StorageOptions storeOpts = StorageOptions.create(cacheInInstance ? 1 : 0,
								cacheInMemcache, cacheInDatastore, false);

						final ModelEntry modelEntry = computeSerialisation(modelAddress, style);
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

	private static ModelEntry computeSerialisation(final XAddress modelAddress, final MStyle style) {
		log.info("Computing serialisation of " + modelAddress);
		final XydraPersistence persistence = Utils.createPersistence(modelAddress.getRepository());
		final XWritableModel model = persistence.getModelSnapshot(new GetWithAddressRequest(modelAddress,
				ModelResource.INCLUDE_TENTATIVE));
		final long rev = model.getRevisionNumber();
		final String ser = ModelResource.computeSerialisation(model, style);
		if (ser == null) {
			log.warn("Serialisation of model " + modelAddress + " is null");
		}
		final ModelEntry modelEntry = new ModelEntry();
		modelEntry.init(rev, ser);
		return modelEntry;
	}

	public static String getSerialisation(final XAddress modelAddress, final StorageOptions storeOpts) {
		final ModelEntry modelEntry = SerialisationCache.MODELS.get(toKey(modelAddress), storeOpts);
		if (modelEntry == null) {
			log.debug("Cache was null for " + modelAddress + ". Options used: " + storeOpts);
			if (storeOpts.isComputeIfNull()) {
				final ModelEntry onTheFly = computeSerialisation(modelAddress, MStyle.xml);
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
