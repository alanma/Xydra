package org.xydra.webadmin.gwt.client;

import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.util.TempStorage;
import org.xydra.webadmin.gwt.client.widgets.version2.BranchTypes;
import org.xydra.webadmin.gwt.client.widgets.version2.EditorPanel;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class Controller {
	
	private static Controller instance;
	private XyAdminServiceAsync service;
	private DataModel dataModel;
	private Observable selectionTree;
	private EditorPanel editorPanel;
	private TempStorage tempStorage;
	private XAddress lastClickedElement;
	// private HashMap<XID,SharedSession> repos;
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private Controller() {
		this.dataModel = new DataModel();
		this.tempStorage = new TempStorage();
	}
	
	public static Controller getInstance() {
		if(instance == null)
			instance = new Controller();
		return instance;
	}
	
	public void addService(XyAdminServiceAsync service) {
		this.service = service;
	}
	
	public void getIDs(final XAddress address) {
		
		BranchTypes clickedBranchType = BranchTypes.getBranchFromAddress(address);
		XID repoId = address.getRepository();
		XID modelId = address.getModel();
		
		if(clickedBranchType.equals(BranchTypes.REPO)) {
			
			this.service.getModelIds(repoId, new AsyncCallback<Set<XID>>() {
				
				@Override
				public void onSuccess(Set<XID> result) {
					log.info("Server said: " + result);
					
					Controller.this.notifySelectionTree(address, result.iterator());
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
				}
			});
		} else if(clickedBranchType.equals(BranchTypes.MODEL)) {
			this.service.getModelSnapshot(repoId, modelId, new AsyncCallback<XReadableModel>() {
				
				@Override
				public void onSuccess(XReadableModel result) {
					log.info("Server said: " + result);
					
					Iterator<XID> iterator = result.iterator();
					Controller.this.notifySelectionTree(address, iterator);
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
				}
			});
		}
		this.lastClickedElement = address;
	}
	
	private void notifySelectionTree(XAddress address, Iterator<XID> iterator) {
		this.selectionTree.notifyMe(address, iterator);
	}
	
	public void registerSelectionTree(Observable widget) {
		
		this.selectionTree = widget;
	}
	
	public void registerEditorPanel(EditorPanel widget) {
		
		this.editorPanel = widget;
	}
	
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
	public void getData(XAddress address) {
		
		BranchTypes clickedBranchType = BranchTypes.getBranchFromAddress(address);
		XID repoId = address.getRepository();
		XID modelId = address.getModel();
		XID objectId = address.getObject();
		
		if(clickedBranchType.equals(BranchTypes.REPO)) {
			
			// this.service.getModelIds(repoId, new AsyncCallback<Set<XID>>() {
			//
			// @Override
			// public void onSuccess(Set<XID> result) {
			// log.info("Server said: " + result);
			//
			// Controller.this.notifySelectionTree(address, result.iterator());
			// }
			//
			// @Override
			// public void onFailure(Throwable caught) {
			// log.warn("Error", caught);
			// }
			// });
		} else if(clickedBranchType.equals(BranchTypes.MODEL)) {
			this.service.getModelSnapshot(repoId, modelId, new AsyncCallback<XReadableModel>() {
				
				@Override
				public void onSuccess(XReadableModel result) {
					Controller.this.notifyEditorPanel(result);
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
					
				}
			});
		} else if(clickedBranchType.equals(BranchTypes.OBJECT)) {
			this.service.getObjectSnapshot(repoId, modelId, objectId,
			        new AsyncCallback<XReadableObject>() {
				        
				        @Override
				        public void onSuccess(XReadableObject result) {
					        Controller.this.notifyEditorPanel(result);
					        
				        }
				        
				        @Override
				        public void onFailure(Throwable caught) {
					        log.warn("Error", caught);
					        
				        }
			        });
		}
		this.lastClickedElement = address;
	}
	
	private void notifyEditorPanel(XReadableObject result) {
		this.editorPanel.notifyMe(result);
		
	}
	
	private void notifyEditorPanel(XReadableModel result) {
		this.editorPanel.notifyMe(result);
		
	}
	
	public TempStorage getTempStorage() {
		return this.tempStorage;
	}
	
	public void getSelectedModel() {
		this.dataModel.getData(this.lastClickedElement);
		
	}
}
