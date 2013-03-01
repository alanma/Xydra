package org.xydra.core.change;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.IndexUtils;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * All write operations are kept here and can be retrieved as a set of added
 * models, removed models and changed models.
 * 
 * TODO manage pre-fetching config (currently: always on)
 * 
 * @author xamde
 * 
 */
public class ChangedRepository extends AbstractDelegatingWritableRepository {
	
	private static final Logger log = LoggerFactory.getLogger(ChangedRepository.class);
	
	public ChangedRepository(XWritableRepository baseRepository) {
		super(baseRepository);
	}
	
	protected Map<XId,ChangedModel> added = new HashMap<XId,ChangedModel>();
	protected Map<XId,ChangedModel> potentiallyChanged = new HashMap<XId,ChangedModel>();
	protected Set<XId> removed = new HashSet<XId>();
	
	/* make sure to return always the same references */
	@Override
	public XWritableModel createModel(XId modelId) {
		if(this.added.containsKey(modelId)) {
			return this.added.get(modelId);
		}
		if(this.potentiallyChanged.containsKey(modelId)) {
			return this.potentiallyChanged.get(modelId);
		}
		if(this.baseRepository.hasModel(modelId)) {
			log.debug("model '" + modelId + "' existed already");
			XWritableModel baseModel = this.baseRepository.getModel(modelId);
			ChangedModel diffModel = new ChangedModel(baseModel);
			this.potentiallyChanged.put(modelId, diffModel);
			return diffModel;
		} else {
			log.debug("model '" + modelId + "' did not exist yet");
			ChangedModel diffModel = new ChangedModel(new SimpleModel(XX.toAddress(getId(),
			        modelId, null, null)));
			this.added.put(modelId, diffModel);
			this.removed.remove(modelId);
			this.potentiallyChanged.remove(modelId);
			return diffModel;
		}
	}
	
	@Override
	public Iterator<XId> iterator() {
		return modelIds().iterator();
	}
	
	private Set<XId> modelIds() {
		Set<XId> ids = IndexUtils.toSet(this.baseRepository.iterator());
		for(XId id : this.removed) {
			ids.remove(id);
		}
		for(XId id : this.added.keySet()) {
			ids.add(id);
		}
		return ids;
	}
	
	@Override
	public boolean hasModel(XId id) {
		return modelIds().contains(id);
	}
	
	@Override
	public boolean isEmpty() {
		return modelIds().isEmpty();
	}
	
	/* make sure to return always the same Booooo references */
	@Override
	public XWritableModel getModel(XId modelId) {
		XWritableModel model = this.added.get(modelId);
		if(model == null) {
			model = this.potentiallyChanged.get(modelId);
		}
		if(model == null) {
			model = this.baseRepository.getModel(modelId);
			if(model != null) {
				model = new ChangedModel(model);
				this.potentiallyChanged.put(modelId, (ChangedModel)model);
			}
		}
		return model;
	}
	
	@Override
	public boolean removeModel(XId modelId) {
		this.added.remove(modelId);
		this.potentiallyChanged.remove(modelId);
		return this.removed.add(modelId);
	}
	
	public Collection<ChangedModel> getAdded() {
		return this.added.values();
	}
	
	public Collection<ChangedModel> getPotentiallyChanged() {
		return this.potentiallyChanged.values();
	}
	
	public Set<XId> getRemoved() {
		return this.removed;
	}
	
	public boolean hasChanges() {
		if(!this.added.isEmpty() || !this.removed.isEmpty()) {
			return true;
		}
		for(ChangedModel model : this.potentiallyChanged.values()) {
			if(model.hasChanges()) {
				return true;
			}
		}
		return false;
	}
	
	public String changesToString() {
		StringBuffer buf = new StringBuffer();
		for(XId modelId : this.added.keySet()) {
			buf.append("=== ADDED " + modelId + " ===<br/>\n");
			ChangedModel model = this.added.get(modelId);
			if(model.hasChanges()) {
				XTransactionBuilder xtb = new XTransactionBuilder(model.getAddress());
				xtb.applyChanges(model);
				XTransaction txn = xtb.build();
				for(XAtomicCommand command : txn) {
					buf.append(" " + command + " <br/>\n");
				}
			}
		}
		for(XId modelId : this.removed) {
			buf.append("=== REMOVED " + modelId + " ===<br/>\n");
		}
		for(ChangedModel model : this.potentiallyChanged.values()) {
			if(model.hasChanges()) {
				buf.append("=== CHANGED " + model.getId() + " === <br/>\n");
				XTransactionBuilder xtb = new XTransactionBuilder(model.getAddress());
				xtb.applyChanges(model);
				XTransaction txn = xtb.build();
				for(XAtomicCommand command : txn) {
					buf.append(" " + command + " <br/>\n");
				}
			}
		}
		return buf.toString();
	}
	
}
