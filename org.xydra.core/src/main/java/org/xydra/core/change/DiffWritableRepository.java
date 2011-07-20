package org.xydra.core.change;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.index.IndexUtils;


/**
 * All write operations are kept here and can be retrieved as a set of added
 * models, removed models and changed models.
 * 
 * TODO manage pre-fetching config (currently: always on)
 * 
 * @author xamde
 * 
 */
public class DiffWritableRepository extends AbstractDelegatingWritableRepository {
	
	public DiffWritableRepository(XWritableRepository baseRepository) {
		super(baseRepository);
	}
	
	protected Map<XID,DiffWritableModel> added = new HashMap<XID,DiffWritableModel>();
	protected Map<XID,DiffWritableModel> potentiallyChanged = new HashMap<XID,DiffWritableModel>();
	protected Set<XID> removed = new HashSet<XID>();
	
	@Override
	public XWritableModel createModel(XID modelId) {
		if(this.added.containsKey(modelId)) {
			return this.added.get(modelId);
		}
		if(this.potentiallyChanged.containsKey(modelId)) {
			return this.potentiallyChanged.get(modelId);
		}
		if(this.baseRepository.hasModel(modelId)) {
			XWritableModel baseModel = this.baseRepository.getModel(modelId);
			assert baseModel instanceof ReadCachingWritableModel;
			baseModel = ((ReadCachingWritableModel)baseModel).getBase();
			DiffWritableModel diffModel = new DiffWritableModel(baseModel, true);
			this.potentiallyChanged.put(modelId, diffModel);
			return diffModel;
		} else {
			DiffWritableModel diffModel = new DiffWritableModel(new SimpleModel(XX.toAddress(
			        getID(), modelId, null, null)), true);
			this.added.put(modelId, diffModel);
			this.removed.remove(modelId);
			this.potentiallyChanged.remove(modelId);
			return diffModel;
		}
	}
	
	@Override
	public Iterator<XID> iterator() {
		return modelIds().iterator();
	}
	
	private Set<XID> modelIds() {
		Set<XID> ids = IndexUtils.toSet(this.baseRepository.iterator());
		for(XID id : this.removed) {
			ids.remove(id);
		}
		for(XID id : this.added.keySet()) {
			ids.add(id);
		}
		return ids;
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
		XWritableModel model = this.added.get(modelId);
		if(model == null) {
			model = this.potentiallyChanged.get(modelId);
		}
		if(model == null) {
			model = this.baseRepository.getModel(modelId);
			if(model != null) {
				assert model instanceof ReadCachingWritableModel;
				model = ((ReadCachingWritableModel)model).getBase();
				model = new DiffWritableModel(model, true);
				this.potentiallyChanged.put(modelId, (DiffWritableModel)model);
			}
		}
		return model;
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		this.added.remove(modelId);
		this.potentiallyChanged.remove(modelId);
		return this.removed.add(modelId);
	}
	
	public Collection<DiffWritableModel> getAdded() {
		return this.added.values();
	}
	
	public Collection<DiffWritableModel> getPotentiallyChanged() {
		return this.potentiallyChanged.values();
	}
	
	public Set<XID> getRemoved() {
		return this.removed;
	}
	
	public boolean hasChanges() {
		if(!this.added.isEmpty() || !this.removed.isEmpty()) {
			return true;
		}
		for(DiffWritableModel model : this.potentiallyChanged.values()) {
			if(model.hasChanges()) {
				return true;
			}
		}
		return false;
	}
	
}
