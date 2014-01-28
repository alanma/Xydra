package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
	
	private XId repoId;
	private HashMap<XId,SessionCachedModel> models;
	private HashMap<XId,SessionCachedModel> deletedModels;
	private HashSet<XId> addedModels;
	private HashSet<XId> notExistingModels;
	
	private boolean knowsAllModels = false;
	
	public RepoDataModel(XId repoId) {
		this.repoId = repoId;
		this.models = new HashMap<XId,SessionCachedModel>();
		this.deletedModels = new HashMap<XId,SessionCachedModel>();
		this.addedModels = new HashSet<XId>();
		this.notExistingModels = new HashSet<XId>();
	}
	
	public void indexModel(XId modelId) {
		// TODO install proper data state checking
		this.models.put(modelId,
		        new SessionCachedModel(XX.toAddress(this.repoId, modelId, null, null)));
		// log.info("indexed model " + modelId.toString());
	}
	
	public int addModelID(XId xid) {
		if(this.models.containsKey(xid)) {
			return RepoDataModel.ALREADYEXISTING;
		} else {
			this.indexModel(xid);
			this.addedModels.add(xid);
			if(this.notExistingModels.contains(xid)) {
				this.notExistingModels.remove(xid);
			}
			return RepoDataModel.SUCCESS;
		}
	}
	
	public Iterator<XId> getModelIDs() {
		return this.models.keySet().iterator();
	}
	
	public String toString() {
		return this.repoId.toString();
	}
	
	public XId getId() {
		return this.repoId;
	}
	
	public boolean isEmpty() {
		
		return this.models.isEmpty();
	}
	
	public SessionCachedModel getModel(XId modelId) {
		
		return this.models.get(modelId);
	}
	
	public void removeModel(XId modelID) {
		if(this.addedModels.contains(modelID)) {
			this.addedModels.remove(modelID);
		} else {
			this.deletedModels.put(modelID, this.models.get(modelID));
		}
		this.models.remove(modelID);
	}
	
	public Set<XId> getAddedModels() {
		return this.addedModels;
	}
	
	public Set<Entry<XId,SessionCachedModel>> getDeletedModelIDs() {
		return this.deletedModels.entrySet();
	}
	
	public HashSet<SessionCachedModel> getChangedModels() {
		HashSet<SessionCachedModel> changedModels = new HashSet<SessionCachedModel>();
		for(SessionCachedModel sessionCachedModel : this.models.values()) {
			if(sessionCachedModel.hasChanges()) {
				changedModels.add(sessionCachedModel);
			}
		}
		return changedModels;
	}
	
	public XTransactionBuilder getModelChanges(XTransactionBuilder givenTxnBuilder, XAddress address) {
		
		XTransactionBuilder txnBuilder = givenTxnBuilder;
		
		if(givenTxnBuilder == null) {
			txnBuilder = new XTransactionBuilder(address);
		}
		
		SessionCachedModel model = getModel(address.getModel());
		model.commitTo(txnBuilder);
		
		return txnBuilder;
		
	}
	
	public boolean isAddedModel(XId model) {
		return this.addedModels.contains(model);
	}
	
	public void addDeletedModel(XId model) {
		this.notExistingModels.add(model);
		
	}
	
	public boolean isNotExisting(XId model) {
		boolean notExisting = false;
		if(this.notExistingModels.contains(model)) {
			notExisting = true;
		}
		return notExisting;
	}
	
	public void setCommitted(XId model) {
		if(this.isAddedModel(model)) {
			this.addedModels.remove(model);
		}
		if(this.deletedModels.containsKey(model)) {
			this.deletedModels.remove(model);
		}
		
		SessionCachedModel model2 = this.models.get(model);
		model2.markAsCommitted();
		
	}
	
	public boolean knowsAllModels() {
		
		return this.knowsAllModels;
	}
	
	public void setKnowsAllModels() {
		this.knowsAllModels = true;
	}
	
}
