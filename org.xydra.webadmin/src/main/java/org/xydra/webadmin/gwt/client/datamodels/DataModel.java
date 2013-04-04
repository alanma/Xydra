package org.xydra.webadmin.gwt.client.datamodels;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;


public class DataModel {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	public static final String REPO = "REPO";
	public static final String MODEL = "MODEL";
	
	private HashMap<XId,RepoDataModel> repoModels;
	
	public DataModel() {
		this.repoModels = new HashMap<XId,RepoDataModel>();
	}
	
	public void addRepoID(XId repoId) {
		this.repoModels.put(repoId, new RepoDataModel(repoId));
	}
	
	public Iterator<RepoDataModel> getRepoIDs() {
		return this.repoModels.values().iterator();
	}
	
	public Iterator<XId> getLocallyStoredModelIDs(final XAddress repoAddress) {
		
		final XId repoId = repoAddress.getRepository();
		
		Iterator<XId> iterator = null;
		
		iterator = this.getRepo(repoId).getModelIDs();
		
		return iterator;
	}
	
	public RepoDataModel getRepo(XId id) {
		
		return this.repoModels.get(id);
	}
	
	public boolean isLoaded(String type, XId id) {
		
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
	
	public void addObject(XAddress modelAddress, XId objectID) {
		log.info("here i am ");
		RepoDataModel repo = this.repoModels.get(modelAddress.getRepository());
		SessionCachedModel model = repo.getModel(modelAddress.getModel());
		model.createObject(objectID);
		
		EventHelper.fireModelChangeEvent(modelAddress, EntityStatus.EXTENDED, objectID);
		
		log.info("object " + objectID.toString() + " added to " + modelAddress.toString());
		
	}
	
	public void addModel(XId repoId, XId modelID) {
		RepoDataModel repo = this.repoModels.get(repoId);
		repo.addModelID(modelID);
		EventHelper.fireRepoChangeEvent(XX.toAddress(repoId, null, null, null),
		        EntityStatus.REGISTERED);
	}
	
	public void removeItem(XAddress address) {
		
		log.info("remove-request for item " + address.toString());
		
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		
		switch(address.getAddressedType()) {
		case XREPOSITORY:
			// never happens
			break;
		case XMODEL:
			repo.removeModel(address.getModel());
			log.info("deleted model " + address.getModel().toString());
			EventHelper.fireModelChangeEvent(address, EntityStatus.DELETED, XX.toId("dummy"));
			break;
		case XFIELD:
			XWritableObject object = model.getObject(address.getObject());
			object.removeField(address.getField());
			log.info("deleted field " + address.getField().toString());
			break;
		case XOBJECT:
			log.info("object to be removed: " + model.getObject(address.getObject()).toString());
			model.removeObject(address.getObject());
			EventHelper.fireObjectChangedEvent(address, EntityStatus.DELETED, null);
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
		
		// changeValue(address, value);
		
	}
	
	public void loadEntity(XAddress typedAddress) {
		
		XId repoID = typedAddress.getRepository();
		RepoDataModel repoDataModel = this.repoModels.get(repoID);
		if(repoDataModel == null) {
			
			log.error("no such repo found!");
			return;
		} else {
			SessionCachedModel model = repoDataModel.getModel(typedAddress.getModel());
			if(model == null) {
				log.error("no such model found!");
				return;
			} else {
				XId objectID = typedAddress.getObject();
				if(objectID == null) {
					// XyAdmin.getInstance().getController().openView(model);
				} else {
					// an object = model.getObject(objectID)
				}
			}
		}
	}
	
}
