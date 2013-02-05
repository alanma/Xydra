package org.xydra.webadmin.gwt.client;

import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.util.TempStorage;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
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
	
	public Iterator<XID> getLocallyStoredIDs(final XAddress address) {
		
		final XID repoId = address.getRepository();
		
		Iterator<XID> iterator = null;
		
		XType addressedType = address.getAddressedType();
		if(addressedType.equals(XType.XREPOSITORY)) {
			
			iterator = Controller.this.dataModel.getRepo(repoId).getModelIDs();
			
		} else if(addressedType.equals(XType.XMODEL)) {
			
			// Controller.this.notifySelectionTree(address,
			// Controller.this.dataModel.getRepo(repoId)
			// .getModelIDs());
			
		}
		// this.lastClickedElement = address;
		return iterator;
	}
	
	public void getIDsFromServer(final XAddress address) {
		
		final XID repoId = address.getRepository();
		XID modelId = address.getModel();
		
		XType addressedType = address.getAddressedType();
		
		if(addressedType.equals(XType.XREPOSITORY)) {
			
			this.service.getModelIds(repoId, new AsyncCallback<Set<XID>>() {
				
				@Override
				public void onSuccess(Set<XID> result) {
					log.info("Server said: " + result);
					
					for(XID modelID : result) {
						Controller.this.dataModel.getRepo(repoId).addModelID(modelID);
					}
					Controller.this.notifySelectionTree(address);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
				}
			});
			
		} else if(addressedType.equals(XType.XMODEL)) {
			this.service.getModelSnapshot(repoId, modelId, new AsyncCallback<XReadableModel>() {
				
				@Override
				public void onSuccess(XReadableModel result) {
					log.info("Server said: " + result);
					
					Controller.this.notifySelectionTree(address);
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("Error", caught);
				}
			});
		}
		this.lastClickedElement = address;
	}
	
	// public void notifySelectionTree(XAddress address, Iterator<XID> iterator)
	// {
	// log.info("selectionTree notified for address " + address.toString());
	// this.selectionTree.notifyMe(address, iterator);
	// }
	
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
		
		XID repoId = address.getRepository();
		XID modelId = address.getModel();
		
		XType addressedType = address.getAddressedType();
		if(addressedType.equals(XType.XMODEL)) {
			
			XReadableModel localModel = this.dataModel.getRepo(repoId).getModel(modelId);
			
			Controller.this.notifyEditorPanel(localModel);
			
		}
		this.lastClickedElement = address;
	}
	
	private void notifyEditorPanel(XReadableModel result) {
		this.editorPanel.notifyMe(result);
		
	}
	
	public TempStorage getTempStorage() {
		return this.tempStorage;
	}
	
	public XAddress getSelectedModelAddress() {
		return this.lastClickedElement;
	}
	
	public void loadCurrentData() {
		this.service.getModelSnapshot(this.lastClickedElement.getRepository(),
		        this.lastClickedElement.getModel(), new AsyncCallback<XReadableModel>() {
			        
			        @Override
			        public void onSuccess(XReadableModel result) {
				        Controller.this.dataModel.getRepo(
				                Controller.this.lastClickedElement.getRepository()).addBaseModel(
				                result);
				        updateEditorPanel();
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.warn("Error", caught);
				        
			        }
		        });
		
	}
	
	public void updateEditorPanel() {
		SessionCachedModel model = Controller.this.dataModel.getRepo(
		        Controller.this.lastClickedElement.getRepository()).getModel(
		        Controller.this.lastClickedElement.getModel());
		if(model == null) {
			log.warn("problem! lastClickedElement: "
			        + Controller.this.lastClickedElement.toString());
		}
		Controller.this.notifyEditorPanel(model);
	}
	
	public void commit() {
		XTransactionBuilder txnBuilder = new XTransactionBuilder(getSelectedModelAddress());
		SessionCachedModel model = this.dataModel.getRepo(this.lastClickedElement.getRepository())
		        .getModel(this.lastClickedElement.getModel());
		model.commitTo(txnBuilder);
		XTransaction transaction = txnBuilder.build();
		
		for(XAtomicCommand xAtomicCommand : transaction) {
			System.out
			        .println("changes will be at " + xAtomicCommand.getChangedEntity().toString());
		}
		
		this.service.executeCommand(this.lastClickedElement.getRepository(), transaction,
		        new AsyncCallback<Long>() {
			        
			        @Override
			        public void onSuccess(Long result) {
				        log.info("worked!");
				        
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.error(caught.getMessage());
				        caught.printStackTrace();
				        
			        }
		        });
		
	}
	
	public void notifySelectionTree(XAddress address) {
		log.info("selection tree notified for address " + address.toString());
		this.selectionTree.notifyMe(address);
		
	}
}
