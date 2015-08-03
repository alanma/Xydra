package org.xydra.core.change;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.index.IndexUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


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

	public ChangedRepository(final XWritableRepository baseRepository) {
		super(baseRepository);
	}

	protected Map<XId,ChangedModel> added = new HashMap<XId,ChangedModel>();
	protected Map<XId,ChangedModel> potentiallyChanged = new HashMap<XId,ChangedModel>();
	protected Set<XId> removed = new HashSet<XId>();

	/* make sure to return always the same references */
	@Override
	public XWritableModel createModel(final XId modelId) {
		if(this.added.containsKey(modelId)) {
			return this.added.get(modelId);
		}
		if(this.potentiallyChanged.containsKey(modelId)) {
			return this.potentiallyChanged.get(modelId);
		}
		if(this.baseRepository.hasModel(modelId)) {
			if(log.isDebugEnabled()) {
				log.debug("model '" + modelId + "' existed already");
			}
			final XWritableModel baseModel = this.baseRepository.getModel(modelId);
			final ChangedModel diffModel = new ChangedModel(baseModel);
			this.potentiallyChanged.put(modelId, diffModel);
			return diffModel;
		} else {
			if(log.isDebugEnabled()) {
				log.debug("model '" + modelId + "' did not exist yet");
			}
			final ChangedModel diffModel = new ChangedModel(new SimpleModel(Base.toAddress(getId(),
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
		final Set<XId> ids = IndexUtils.toSet(this.baseRepository.iterator());
		for(final XId id : this.removed) {
			ids.remove(id);
		}
		for(final XId id : this.added.keySet()) {
			ids.add(id);
		}
		return ids;
	}

	@Override
	public boolean hasModel(final XId id) {
		return modelIds().contains(id);
	}

	@Override
	public boolean isEmpty() {
		return modelIds().isEmpty();
	}

	/* make sure to return always the same Booooo references */
	@Override
	public XWritableModel getModel(final XId modelId) {
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
	public boolean removeModel(final XId modelId) {
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
		for(final ChangedModel model : this.potentiallyChanged.values()) {
			if(model.hasChanges()) {
				return true;
			}
		}
		return false;
	}

	public String changesToString() {
		final StringBuffer buf = new StringBuffer();
		for(final XId modelId : this.added.keySet()) {
			buf.append("=== ADDED " + modelId + " ===<br/>\n");
			final ChangedModel model = this.added.get(modelId);
			if(model.hasChanges()) {
				final XTransactionBuilder xtb = new XTransactionBuilder(model.getAddress());
				xtb.applyChanges(model);
				final XTransaction txn = xtb.build();
				for(final XAtomicCommand command : txn) {
					buf.append(" " + command + " <br/>\n");
				}
			}
		}
		for(final XId modelId : this.removed) {
			buf.append("=== REMOVED " + modelId + " ===<br/>\n");
		}
		for(final ChangedModel model : this.potentiallyChanged.values()) {
			if(model.hasChanges()) {
				buf.append("=== CHANGED " + model.getId() + " === <br/>\n");
				final XTransactionBuilder xtb = new XTransactionBuilder(model.getAddress());
				xtb.applyChanges(model);
				final XTransaction txn = xtb.build();
				for(final XAtomicCommand command : txn) {
					buf.append(" " + command + " <br/>\n");
				}
			}
		}
		return buf.toString();
	}

}
