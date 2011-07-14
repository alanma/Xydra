package org.xydra.core.change;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.index.IndexUtils;


public class ReadCachingWritableRepository extends AbstractDelegatingWritableRepository {
	
	public ReadCachingWritableRepository(XWritableRepository baseRepository) {
		super(baseRepository);
	}
	
	protected Map<XID,ReadCachingWritableModel> map = new HashMap<XID,ReadCachingWritableModel>();
	
	@Override
	public XWritableModel createModel(XID modelId) {
		XWritableModel baseModel = this.baseRepository.createModel(modelId);
		ReadCachingWritableModel readCachingModel = new ReadCachingWritableModel(baseModel);
		this.map.put(modelId, readCachingModel);
		return readCachingModel;
	}
	
	// FIXME 19.50 -
	
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
				ReadCachingWritableModel cacheModel = new ReadCachingWritableModel(model);
				this.map.put(modelId, cacheModel);
				return cacheModel;
			}
		}
		return model;
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		return this.map.remove(modelId) != null | this.baseRepository.removeModel(modelId);
	}
	
}
