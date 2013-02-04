package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.change.SessionCachedModel;


public class RepoDataModel {
	
	private XID id;
	private HashMap<XID,SessionCachedModel> models;
	
	public RepoDataModel(XID repoId) {
		this.id = repoId;
		this.models = new HashMap<XID,SessionCachedModel>();
	}
	
	public void addModelID(XID xid) {
		this.models.put(xid, new SessionCachedModel(XX.toAddress(this.id, xid, null, null)));
	}
	
	public Iterator<XID> getModelIDs() {
		return this.models.keySet().iterator();
	}
	
	public String toString() {
		return this.id.toString();
	}
	
	public XID getId() {
		return this.id;
	}
	
	public boolean isEmpty() {
		
		return this.models.isEmpty();
	}
	
	public SessionCachedModel getModel(XID modelId) {
		
		return this.models.get(modelId);
	}
	
	public void addBaseModel(XReadableModel result) {
		SessionCachedModel cachedModel = this.models.get(result.getId());
		
		cachedModel.indexModel(result);
	}
	
	public void removeModel(XID model) {
		this.models.remove(model);
		
	}
	
}
