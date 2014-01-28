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
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;


/**
 * Stores all data via {@link RepoDataModel}s that store
 * {@link SessionCachedModel}s. <br>
 * <font color=RED> Performs all add- and remove-requests (does some checks
 * first and fires the appropriate events) </font>
 * 
 * @author Andi_Ka
 * 
 */
public class DataModel {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	public static final String REPO = "REPO";
	public static final String MODEL = "MODEL";
	
	private HashMap<XId,RepoDataModel> repoModels;
	
	public DataModel() {
		this.repoModels = new HashMap<XId,RepoDataModel>();
	}
	
	public void addRepoID(XId repoId) {
		if(!this.repoModels.containsKey(repoId)) {
			
			RepoDataModel repoDataModel = new RepoDataModel(repoId);
			this.repoModels.put(repoId, repoDataModel);
			log.info("added new repo: " + repoId.toString());
			
			EventHelper.fireRepoChangeEvent(XX.resolveRepository(XX.toId("newRepo")),
			        EntityStatus.EXTENDED, repoDataModel);
		} else {
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog("repo already existed!");
		}
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
		
		EventHelper.fireObjectChangedEvent(XX.resolveObject(address), EntityStatus.CHANGED, null);
	}
	
	public void addObject(XAddress modelAddress, XId objectID) {
		String logMessage = "";
		RepoDataModel repo = this.repoModels.get(modelAddress.getRepository());
		SessionCachedModel model = repo.getModel(modelAddress.getModel());
		log.info("attempting to create object " + objectID.toString());
		if(model.hasObject(objectID)) {
			logMessage = "" + objectID.toString() + " already existed in model "
			        + modelAddress.toString();
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog(logMessage);
			log.info(logMessage);
			XyAdmin.getInstance().getController().getAddressWidgetPresenter()
			        .unregisterAllListeners();
		} else {
			model.createObject(objectID);
			log.info("object " + objectID.toString() + " added to " + modelAddress.toString()
			        + ", now firing ModelChangedEvent: EXTENDED");
			
			EventHelper.fireModelChangedEvent(modelAddress, EntityStatus.EXTENDED, objectID);
			
		}
		
	}
	
	public void addModel(XId repoId, XId modelID) {
		RepoDataModel repo = this.repoModels.get(repoId);
		int result = repo.addModelID(modelID);
		
		if(result == RepoDataModel.SUCCESS) {
			log.info("model " + modelID.toString() + " added to " + repoId.toString()
			        + ", now firing RepoChangedEvent: EXTENDED");
			EventHelper.fireRepoChangeEvent(XX.toAddress(repoId, null, null, null),
			        EntityStatus.EXTENDED, null);
		} else {
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog("" + modelID.toString()
			        + " already existed in repo " + repoId.toString());
			XyAdmin.getInstance().getController().getAddressWidgetPresenter()
			        .unregisterAllListeners();
		}
		
	}
	
	public void removeItem(final XAddress address) {
		
		log.info("remove-request for item " + address.toString());
		
		RepoDataModel repo = this.repoModels.get(address.getRepository());
		SessionCachedModel model = repo.getModel(address.getModel());
		
		switch(address.getAddressedType()) {
		case XREPOSITORY:
			// never happens
			break;
		case XMODEL:
			repo.removeModel(address.getModel());
			log.info("deleted model " + address.getModel().toString()
			        + ", now firing MOodelChangedEvent: DELETED");
			
			EventHelper.fireModelChangedEvent(address, EntityStatus.DELETED, XX.toId("dummy"));
			
			break;
		case XFIELD:
			XWritableObject object = model.getObject(address.getObject());
			object.removeField(address.getField());
			log.info("deleted field " + address.getField().toString()
			        + ", now firing ObjectChangedEvent: Changed");
			EventHelper.fireObjectChangedEvent(XX.resolveObject(address), EntityStatus.CHANGED,
			        address.getField());
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
		XId fieldId = address.getField();
		if(object.hasField(fieldId)) {
			String logMessage = "" + fieldId.toString() + " already existed in object "
			        + object.getAddress().toString();
			log.info(logMessage);
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog(logMessage);
			XyAdmin.getInstance().getController().getAddressWidgetPresenter()
			        .unregisterAllListeners();
		} else {
			object.createField(address.getField());
			
			log.info("added field " + address.toString()
			        + ", now firing ObjectChanged - EXTENDED - event!");
			EventHelper.fireObjectChangedEvent(XX.resolveObject(address), EntityStatus.EXTENDED,
			        address.getField());
		}
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
