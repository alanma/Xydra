package org.xydra.core.change;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.index.IndexUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class ReadCachingWritableRepository extends AbstractDelegatingWritableRepository {
	
	private static final Logger log = LoggerFactory.getLogger(ReadCachingWritableRepository.class);
	
	private boolean prefetchModels;
	
	/**
	 * @param baseRepository ..
	 * @param prefetchModels if true, all new models are pre-fetched at first
	 *            call
	 */
	public ReadCachingWritableRepository(XWritableRepository baseRepository, boolean prefetchModels) {
		super(baseRepository);
		this.prefetchModels = prefetchModels;
	}
	
	protected Map<XID,ReadCachingWritableModel> map = new HashMap<XID,ReadCachingWritableModel>();
	
	@Override
	public XWritableModel createModel(XID modelId) {
		XWritableModel baseModel = this.baseRepository.createModel(modelId);
		ReadCachingWritableModel readCachingModel = new ReadCachingWritableModel(baseModel, true);
		this.map.put(modelId, readCachingModel);
		return readCachingModel;
	}
	
	@Override
	public Iterator<XID> iterator() {
		return modelIds().iterator();
	}
	
	private Set<XID> modelIds() {
		Set<XID> set = IndexUtils.toSet(this.map.keySet().iterator());
		set.addAll(IndexUtils.toSet(this.baseRepository.iterator()));
		return set;
	}
	
	@Override
	public boolean hasModel(XID id) {
		return modelIds().contains(id);
	}
	
	@Override
	public boolean isEmpty() {
		return modelIds().isEmpty();
	}
	
	@Override
	public XWritableModel getModel(XID modelId) {
		XWritableModel model = this.map.get(modelId);
		if(model == null) {
			model = this.baseRepository.getModel(modelId);
			if(model != null) {
				log.info("Model '" + modelId + "' not found in cache, but in base.");
				ReadCachingWritableModel cacheModel = new ReadCachingWritableModel(model,
				        this.prefetchModels);
				this.map.put(modelId, cacheModel);
				return cacheModel;
			} else {
				log.info("Model '" + modelId + "' not found in cache & not in base.");
			}
		}
		return model;
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		return this.map.remove(modelId) != null | this.baseRepository.removeModel(modelId);
	}
	
}
