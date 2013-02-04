package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;


public class DataModel {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
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
	
	public void changeValue(XAddress address, XValue value) {
		
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		XWritableObject object = model.getObject(address.getObject());
		XWritableField field = object.getField(address.getField());
		String beforeString = "changed value " + field.getValue() + " (rev. "
		        + field.getRevisionNumber() + ")";
		field.setValue(value);
		
		String afterString = " to " + field.getValue().toString() + " (new rev. "
		        + field.getRevisionNumber() + ")";
		log.warn(beforeString + afterString);
		
	}
	
	public void addObject(XAddress address, XID objectID) {
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		model.createObject(objectID);
		
		log.info("object " + objectID.toString() + " added to " + address.toString());
		
	}
	
	public void addModel(XID repoID, XID modelID) {
		RepoDataModel repo = this.repoModels.get(repoID);
		repo.addModelID(modelID);
		
	}
	
	public void removeItem(XAddress address) {
		
		log.info("remove-request for item " + address.toString());
		
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		
		switch(address.getAddressedType()) {
		case XREPOSITORY:
			
			break;
		case XMODEL:
			repo.removeModel(address.getModel());
			log.info("deleted model " + address.getModel().toString());
			break;
		case XFIELD:
			XWritableObject object = model.getObject(address.getObject());
			object.removeField(address.getField());
			log.info("deleted field " + address.getField().toString());
			break;
		case XOBJECT:
			model.removeObject(address.getObject());
			log.info("deleted object " + address.getObject().toString());
			break;
		default:
			break;
		}
	}
	
	public void addField(XAddress address, XValue value) {
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		XWritableObject object = model.getObject(address.getObject());
		object.createField(address.getField());
		
		log.info("added field " + address.toString());
		
		changeValue(address, value);
		
		Controller.getInstance().updateEditorPanel();
		
	}
}
