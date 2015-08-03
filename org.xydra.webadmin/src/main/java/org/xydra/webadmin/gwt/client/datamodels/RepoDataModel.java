package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

public class RepoDataModel {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	public static final int SUCCESS = 0;
	public static final int ALREADYEXISTING = 1;

	private final XId repoId;
	private final HashMap<XId, SessionCachedModel> models;
	private final HashMap<XId, SessionCachedModel> deletedModels;
	private final HashSet<XId> addedModels;
	private final HashSet<XId> notExistingModels;

	private boolean knowsAllModels = false;

	public RepoDataModel(final XId repoId) {
		this.repoId = repoId;
		this.models = new HashMap<XId, SessionCachedModel>();
		this.deletedModels = new HashMap<XId, SessionCachedModel>();
		this.addedModels = new HashSet<XId>();
		this.notExistingModels = new HashSet<XId>();
	}

	public void indexModel(final XId modelId) {
		// TODO install proper data state checking
		this.models.put(modelId,
				new SessionCachedModel(Base.toAddress(this.repoId, modelId, null, null)));
		// log.info("indexed model " + modelId.toString());
	}

	public int addModelID(final XId xid) {
		if (this.models.containsKey(xid)) {
			return RepoDataModel.ALREADYEXISTING;
		} else {
			indexModel(xid);
			this.addedModels.add(xid);
			if (this.notExistingModels.contains(xid)) {
				this.notExistingModels.remove(xid);
			}
			return RepoDataModel.SUCCESS;
		}
	}

	public Iterator<XId> getModelIDs() {
		return this.models.keySet().iterator();
	}

	@Override
	public String toString() {
		return this.repoId.toString();
	}

	public XId getId() {
		return this.repoId;
	}

	public boolean isEmpty() {

		return this.models.isEmpty();
	}

	public SessionCachedModel getModel(final XId modelId) {

		return this.models.get(modelId);
	}

	public void removeModel(final XId modelID) {
		if (this.addedModels.contains(modelID)) {
			this.addedModels.remove(modelID);
		} else {
			this.deletedModels.put(modelID, this.models.get(modelID));
		}
		this.models.remove(modelID);
	}

	public Set<XId> getAddedModels() {
		return this.addedModels;
	}

	public Set<Entry<XId, SessionCachedModel>> getDeletedModelIDs() {
		return this.deletedModels.entrySet();
	}

	public HashSet<SessionCachedModel> getChangedModels() {
		final HashSet<SessionCachedModel> changedModels = new HashSet<SessionCachedModel>();
		for (final SessionCachedModel sessionCachedModel : this.models.values()) {
			if (sessionCachedModel.hasChanges()) {
				changedModels.add(sessionCachedModel);
			}
		}
		return changedModels;
	}

	public XTransactionBuilder getModelChanges(final XTransactionBuilder givenTxnBuilder, final XAddress address) {

		XTransactionBuilder txnBuilder = givenTxnBuilder;

		if (givenTxnBuilder == null) {
			txnBuilder = new XTransactionBuilder(address);
		}

		final SessionCachedModel model = getModel(address.getModel());
		model.commitTo(txnBuilder);

		return txnBuilder;

	}

	public boolean isAddedModel(final XId model) {
		return this.addedModels.contains(model);
	}

	public void addDeletedModel(final XId model) {
		this.notExistingModels.add(model);

	}

	public boolean isNotExisting(final XId model) {
		boolean notExisting = false;
		if (this.notExistingModels.contains(model)) {
			notExisting = true;
		}
		return notExisting;
	}

	public void setCommitted(final XId model) {
		if (isAddedModel(model)) {
			this.addedModels.remove(model);
		}
		if (this.deletedModels.containsKey(model)) {
			this.deletedModels.remove(model);
		}

		final SessionCachedModel model2 = this.models.get(model);
		model2.markAsCommitted();

	}

	public boolean knowsAllModels() {

		return this.knowsAllModels;
	}

	public void setKnowsAllModels() {
		this.knowsAllModels = true;
	}

}
