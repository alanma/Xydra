package org.xydra.webadmin.gwt.client.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;


public class EntityTree {
	
	HashSet<XId> openRepos = new HashSet<XId>();
	HashMap<XAddress,Set<XAddress>> modelObjectMap = new HashMap<XAddress,Set<XAddress>>();
	
	public void add(XAddress entityAddress) {
		
		/* assert repository is opened */
		XId entityRepoId = entityAddress.getRepository();
		this.openRepos.add(entityRepoId);
		
		if(entityAddress.getModel() != null) {
			/* find out if model is already opened */
			XAddress entityModelAddress = XX.resolveModel(
			        XX.toAddress(entityRepoId, null, null, null), entityAddress.getModel());
			
			Set<XAddress> openModels = this.modelObjectMap.keySet();
			if(openModels.contains(entityModelAddress)) {
				// nothing: model is already opened
			} else {
				this.modelObjectMap.put(entityModelAddress, new HashSet<XAddress>());
			}
			
			if(entityAddress.getObject() != null) {
				
				/* find out if object is already opened */
				XAddress objectAddress = XX.resolveObject(
				        XX.toAddress(entityRepoId, null, null, null), entityAddress.getModel(),
				        entityAddress.getObject());
				
				Set<XAddress> openObjectSet = this.modelObjectMap.get(entityModelAddress);
				openObjectSet.add(objectAddress);
			}
		}
	}
	
	public void remove(XAddress entityAddress) {
		
		XId entityRepoId = entityAddress.getRepository();
		XAddress entityModelAddress = XX.resolveModel(XX.toAddress(entityRepoId, null, null, null),
		        entityAddress.getModel());
		
		if(entityAddress.getObject() != null) {
			XAddress objectAddress = XX.resolveObject(XX.toAddress(entityRepoId, null, null, null),
			        entityAddress.getModel(), entityAddress.getObject());
			Set<XAddress> openObjectSet = this.modelObjectMap.get(entityModelAddress);
			openObjectSet.remove(objectAddress);
		} else {
			if(entityAddress.getModel() != null) {
				this.modelObjectMap.remove(entityModelAddress);
			} else {
				XId repoId = entityAddress.getRepository();
				this.openRepos.remove(repoId);
				
				/* remove all models, which contain that repo */
				Set<Entry<XAddress,Set<XAddress>>> modelObjectEntries = this.modelObjectMap
				        .entrySet();
				
				Set<XAddress> modelToBeDeleted = new HashSet<XAddress>();
				for(Entry<XAddress,Set<XAddress>> entry : modelObjectEntries) {
					XAddress modelKey = entry.getKey();
					if(modelKey.getRepository().equals(repoId)) {
						modelToBeDeleted.add(modelKey);
					}
				}
				for(XAddress xAddress : modelToBeDeleted) {
					this.modelObjectMap.remove(xAddress);
				}
			}
		}
	}
	
	public String toString() {
		String resultString = "";
		
		resultString += "open Repos: \n";
		for(XId repoId : this.openRepos) {
			resultString += repoId.toString() + ", ";
		}
		
		resultString += "\n Models: \n";
		for(XAddress modelAddress : this.modelObjectMap.keySet()) {
			resultString += modelAddress.toString() + ", ";
			
			Set<XAddress> objectSet = this.modelObjectMap.get(modelAddress);
			resultString += "objects in this model: \n";
			for(XAddress xAddress : objectSet) {
				resultString += xAddress.toString() + ", ";
			}
			resultString += "\n";
		}
		
		return resultString;
	}
	
	public Set<XId> getOpenRepos() {
		return this.openRepos;
	}
	
}
