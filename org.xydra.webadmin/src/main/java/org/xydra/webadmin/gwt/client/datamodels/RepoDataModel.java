package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashSet;
import java.util.Iterator;

import org.xydra.base.XID;


public class RepoDataModel {
	
	private XID id;
	private HashSet<ModelDataModel> models;
	
	public RepoDataModel(XID repoId) {
		this.id = repoId;
		this.models = new HashSet<ModelDataModel>();
	}
	
	public void addModelID(XID xid) {
		this.models.add(new ModelDataModel(xid));
	}
	
	public Iterator<ModelDataModel> getModelIDs() {
		return this.models.iterator();
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
	
}
