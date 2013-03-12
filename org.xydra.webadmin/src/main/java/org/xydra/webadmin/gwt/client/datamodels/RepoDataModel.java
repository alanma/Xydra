package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.change.XTransactionBuilder;


public class RepoDataModel {
	
	private XId id;
	private HashMap<XId,SessionCachedModel> models;
	private HashMap<XId,SessionCachedModel> deletedModels;
	private HashSet<XId> addedModels;
	
	public RepoDataModel(XId repoId) {
		this.id = repoId;
		this.models = new HashMap<XId,SessionCachedModel>();
		this.deletedModels = new HashMap<XId,SessionCachedModel>();
		this.addedModels = new HashSet<XId>();
	}
	
	public void addModelID(XId xid) {
		this.models.put(xid, new SessionCachedModel(XX.toAddress(this.id, xid, null, null)));
		this.addedModels.add(xid);
	}
	
	public Iterator<XId> getModelIDs() {
		return this.models.keySet().iterator();
	}
	
	public String toString() {
		return this.id.toString();
	}
	
	public XId getId() {
		return this.id;
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
	
}
