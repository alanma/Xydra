package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.core.change.SessionCachedModel;


public class RepoDataModel {
	
	private XId id;
	private HashMap<XId,SessionCachedModel> models;
	
	public RepoDataModel(XId repoId) {
		this.id = repoId;
		this.models = new HashMap<XId,SessionCachedModel>();
	}
	
	public void addModelID(XId xid) {
		this.models.put(xid, new SessionCachedModel(XX.toAddress(this.id, xid, null, null)));
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
	
	public void removeModel(XId model) {
		this.models.remove(model);
		
	}
	
}
