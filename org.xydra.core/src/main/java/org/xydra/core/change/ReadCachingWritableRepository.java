package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.XID;
import org.xydra.base.rmof.XStateWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A read-caching repository.
 * 
 * @author xamde
 */
@Deprecated
public class ReadCachingWritableRepository extends AbstractDelegatingWritableRepository {
	
	public ReadCachingWritableRepository(XWritableRepository baseRepository) {
		super(baseRepository);
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ReadCachingWritableRepository.class);
	
	@Override
	public XStateWritableModel createModel(XID modelId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public XStateWritableModel getModel(XID modelId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean hasModel(XID id) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Iterator<XID> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// private boolean prefetchModels;
	//
	// private boolean knowsAllModelsIds = false;
	//
	// /** Only used for fast prefetching */
	// private XydraPersistence persistence;
	//
	// /**
	// * @param baseRepository ..
	// * @param prefetchModels if true, all new models are pre-fetched at first
	// * call
	// */
	// public ReadCachingWritableRepository(XWritableRepository baseRepository,
	// boolean prefetchModels) {
	// super(baseRepository);
	// this.prefetchModels = prefetchModels;
	// }
	//
	// /**
	// * Loads state much faster directly from snapshots.
	// *
	// * @param baseRepository ..
	// * @param persistence where to load snapshots from
	// */
	// public ReadCachingWritableRepository(XWritableRepository baseRepository,
	// XydraPersistence persistence) {
	// super(baseRepository);
	// this.prefetchModels = true;
	// this.persistence = persistence;
	// }
	//
	// protected Set<XID> knownExistingModelIds = new HashSet<XID>();
	//
	// protected Set<XID> knownNonExistingModelIds = new HashSet<XID>();
	//
	// protected Map<XID,ReadCachingWritableModel> map = new
	// HashMap<XID,ReadCachingWritableModel>();
	//
	// @Override
	// public XWritableModel createModel(XID modelId) {
	// ReadCachingWritableModel readCachingModel = this.map.get(modelId);
	// if(readCachingModel == null) {
	// // we never read it => created it & cache it
	// XWritableModel baseModel = this.baseRepository.createModel(modelId);
	// readCachingModel = this.persistence == null ? new
	// ReadCachingWritableModel(baseModel,
	// this.prefetchModels) :
	// // load much faster
	// new ReadCachingWritableModel(baseModel, this.persistence);
	// this.map.put(modelId, readCachingModel);
	// this.knownExistingModelIds.add(modelId);
	// this.knownNonExistingModelIds.remove(modelId);
	// }
	// return readCachingModel;
	// }
	//
	// @Override
	// public Iterator<XID> iterator() {
	// return modelIds().iterator();
	// }
	//
	// private Set<XID> modelIds() {
	// if(!this.knowsAllModelsIds) {
	// this.knownExistingModelIds =
	// IndexUtils.toSet(this.map.keySet().iterator());
	// this.knownExistingModelIds.addAll(IndexUtils.toSet(this.baseRepository.iterator()));
	// this.knowsAllModelsIds = true;
	// }
	// return this.knownExistingModelIds;
	// }
	//
	// @Override
	// public boolean hasModel(XID modelId) {
	// return getModel(modelId) != null;
	// /*
	// * This impl triggers gae.changes.Utils:73 findChildren() = a query that
	// * is not cached.
	// *
	// * return modelIds().contains(modelId);
	// */
	// }
	//
	// @Override
	// public boolean isEmpty() {
	// return modelIds().isEmpty();
	// }
	//
	// @Override
	// public XWritableModel getModel(XID modelId) {
	// XWritableModel model = this.map.get(modelId);
	// if(model == null) {
	// /* prevent asking base again for models that simply don't exists */
	// if(this.knownNonExistingModelIds.contains(model)) {
	// return null;
	// }
	// /* ask base */
	// model = this.baseRepository.getModel(modelId);
	// if(model != null) {
	// log.info("Model '" + modelId + "' not found in cache, but in base.");
	//
	// ReadCachingWritableModel cacheModel;
	// if(this.prefetchModels && this.persistence != null) {
	// // take the fast route
	// cacheModel = new ReadCachingWritableModel(model, this.persistence);
	// } else {
	// // normal, slow
	// cacheModel = new ReadCachingWritableModel(model, this.prefetchModels);
	// }
	// this.map.put(modelId, cacheModel);
	// return cacheModel;
	// } else {
	// this.knownNonExistingModelIds.add(modelId);
	// log.info("Model '" + modelId + "' not found in cache & not in base.");
	// }
	// }
	// return model;
	// }
	//
	// @Override
	// public boolean removeModel(XID modelId) {
	// this.knownExistingModelIds.remove(modelId);
	// this.knownNonExistingModelIds.add(modelId);
	// return this.map.remove(modelId) != null |
	// this.baseRepository.removeModel(modelId);
	// }
	
}
