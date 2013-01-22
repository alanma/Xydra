package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


public class DataModel {
	
	public static final String REPO = "REPO";
	public static final String MODEL = "MODEL";
	
	private HashMap<XID,RepoDataModel> repoModels;
	
	public DataModel() {
		this.repoModels = new HashMap<XID,RepoDataModel>();
	}
	
	public void addRepoID(XID repoId) {
		this.repoModels.put(repoId, new RepoDataModel(repoId));
	}
	
	public Iterator<RepoDataModel> getRepoIDs() {
		return this.repoModels.values().iterator();
	}
	
	public RepoDataModel getRepo(XID id) {
		
		return this.repoModels.get(id);
	}
	
	public boolean isLoaded(String type, XID id) {
		
		boolean result = false;
		if(type.equals(REPO)) {
			
			if(!this.repoModels.get(id).isEmpty()) {
				result = true;
			}
		}
		return result;
	}
	
	public void getData(XAddress lastClickedElement) {
		// TODO Auto-generated method stub
		
	}
	
}
