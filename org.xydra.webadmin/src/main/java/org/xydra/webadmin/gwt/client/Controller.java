package org.xydra.webadmin.gwt.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.util.TableController;
import org.xydra.webadmin.gwt.client.util.TableController.Status;
import org.xydra.webadmin.gwt.client.util.TempStorage;
import org.xydra.webadmin.gwt.client.widgets.WarningWidget;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTree;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class Controller {
	
	private static Controller instance;
	private XyAdminServiceAsync service;
	private DataModel dataModel;
	private SelectionTree selectionTree;
	private EditorPanel editorPanel;
	private TempStorage tempStorage;
	private XAddress lastClickedElement;
	private WarningWidget warningWidget;
	private TableController tableController;
	
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
	
	public Iterator<XId> getLocallyStoredIDs(final XAddress address) {
		
		final XId repoId = address.getRepository();
		
		Iterator<XId> iterator = null;
		
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
		
		final XId repoId = address.getRepository();
		XId modelId = address.getModel();
		
		XType addressedType = address.getAddressedType();
		
		if(addressedType.equals(XType.XREPOSITORY)) {
			
			this.service.getModelIds(repoId, new AsyncCallback<Set<XId>>() {
				
				@Override
				public void onSuccess(Set<XId> result) {
					log.info("Server said: " + result);
					
					if(result.isEmpty()) {
						log.error("no models found!");
						WarningDialog dialog = new WarningDialog("no models found!");
						dialog.show();
					} else {
						for(XId modelID : result) {
							Controller.this.dataModel.getRepo(repoId).indexModel(modelID);
						}
						Controller.this.notifySelectionTree(address);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					
					log.warn("Error", caught);
				}
			});
			
		} else if(addressedType.equals(XType.XMODEL)) {
			log.warn("hi");
			this.service.getModelSnapshot(repoId, modelId, new AsyncCallback<XReadableModel>() {
				
				@Override
				public void onSuccess(XReadableModel result) {
					log.info("Server said: " + result);
					
					Controller.this.notifySelectionTree(address);
					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					log.warn("hi");
					log.warn("Error", caught);
				}
			});
		}
		this.lastClickedElement = address;
	}
	
	// public void notifySelectionTree(XAddress address, Iterator<XId> iterator)
	// {
	// log.info("selectionTree notified for address " + address.toString());
	// this.selectionTree.notifyMe(address, iterator);
	// }
	
	public void registerSelectionTree(SelectionTree selectionTree) {
		
		this.selectionTree = selectionTree;
	}
	
	public void registerEditorPanel(EditorPanel widget) {
		
		this.editorPanel = widget;
	}
	
	public DataModel getDataModel() {
		return this.dataModel;
	}
	
	public void getData(XAddress address) {
		
		XId repoId = address.getRepository();
		XId modelId = address.getModel();
		
		XType addressedType = address.getAddressedType();
		if(addressedType.equals(XType.XMODEL)) {
			
			SessionCachedModel localModel = this.dataModel.getRepo(repoId).getModel(modelId);
			
			Controller.this.notifyEditorPanel(localModel);
			
		}
		this.lastClickedElement = address;
	}
	
	private void notifyEditorPanel(SessionCachedModel result) {
		this.editorPanel.notifyMe(result);
		
	}
	
	public TempStorage getTempStorage() {
		return this.tempStorage;
	}
	
	public XAddress getSelectedModelAddress() {
		return this.lastClickedElement;
	}
	
	public void loadCurrentModelsObjects() {
		this.service.getModelSnapshot(this.lastClickedElement.getRepository(),
		        this.lastClickedElement.getModel(), new AsyncCallback<XReadableModel>() {
			        
			        @Override
			        public void onSuccess(XReadableModel result) {
				        if(result == null) {
					        @SuppressWarnings("unused")
					        WarningDialog d = new WarningDialog("model is deleted!");
					        Controller.getInstance().getDataModel()
					                .getRepo(Controller.this.lastClickedElement.getRepository())
					                .addDeletedModel(Controller.this.lastClickedElement.getModel());
				        } else {
					        if(result.isEmpty()) {
						        log.error("no objects found!");
						        WarningDialog dialog = new WarningDialog("no objects found!");
						        dialog.show();
					        } else {
						        
						        Controller.this.dataModel
						                .getRepo(Controller.this.lastClickedElement.getRepository())
						                .getModel(result.getId()).indexModel(result);
						        updateEditorPanel();
						        Controller.this
						                .notifySelectionTree(Controller.this.lastClickedElement);
					        }
				        }
				        Controller.this.notifySelectionTree(Controller.this.lastClickedElement);
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
	
	public void commit(XCommand addModelCommand, final XTransaction modelTransactions) {
		
		if(addModelCommand != null) {
			commitAddedModel(addModelCommand, modelTransactions);
		} else {
			commitModelTransactions(modelTransactions);
		}
	}
	
	private void commitAddedModel(XCommand addModelCommand, final XTransaction modelTransactions) {
		this.service.executeCommand(this.lastClickedElement.getRepository(), addModelCommand,
		        new AsyncCallback<Long>() {
			        
			        String resultString = "";
			        
			        @Override
			        public void onSuccess(Long result) {
				        if(XCommandUtils.success(result)) {
					        this.resultString = "successfully committed model! New revision number: "
					                + result;
				        } else if(XCommandUtils.noChange(result)) {
					        this.resultString = "no Changes!";
				        } else if(XCommandUtils.failed(result)) {
					        this.resultString = "commit failed!";
				        } else {
					        this.resultString = "i have no idea...";
				        }
				        
				        Controller.this.tempStorage.notifyDialog(this.resultString);
				        commitModelTransactions(modelTransactions);
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        Controller.this.tempStorage.notifyDialog(this.resultString + "error! \n"
				                + caught.getMessage());
				        
			        }
		        });
	}
	
	private void commitModelTransactions(XTransaction modelTransactions) {
		
		if(modelTransactions != null) {
			this.service.executeCommand(this.lastClickedElement.getRepository(), modelTransactions,
			        new AsyncCallback<Long>() {
				        
				        String resultString = "";
				        
				        @Override
				        public void onSuccess(Long result) {
					        if(XCommandUtils.success(result)) {
						        this.resultString = "successfully committed! New revision number: "
						                + result;
					        } else if(XCommandUtils.noChange(result)) {
						        this.resultString = "no Changes!";
					        } else if(XCommandUtils.failed(result)) {
						        this.resultString = "commit failed!";
					        } else {
						        this.resultString = "i have no idea...";
					        }
					        
					        SessionCachedModel model2 = Controller.this.dataModel.getRepo(
					                Controller.this.lastClickedElement.getRepository()).getModel(
					                Controller.this.lastClickedElement.getModel());
					        model2.markAsCommitted();
					        Controller.this.loadCurrentModelsObjects();
					        Controller.this.tempStorage.notifyDialog(this.resultString);
					        Controller.this.tempStorage.allowDialogClose();
					        
				        }
				        
				        @Override
				        public void onFailure(Throwable caught) {
					        Controller.this.tempStorage.notifyDialog(this.resultString
					                + "error! \n" + caught.getMessage());
					        
				        }
			        });
			
		} else {
			Controller.this.tempStorage.allowDialogClose();
		}
	}
	
	public void notifySelectionTree(XAddress address) {
		log.info("selection tree notified for address " + address.toString());
		this.selectionTree.notifyMe(address);
		
	}
	
	public void displayError(String message) {
		this.warningWidget.display(message);
		
	}
	
	public void registerWarningWidget(WarningWidget warningWidget) {
		this.warningWidget = warningWidget;
	}
	
	public void loadCurrentModelsIDs() {
		this.service.getModelSnapshot(this.lastClickedElement.getRepository(),
		        this.lastClickedElement.getModel(), new AsyncCallback<XReadableModel>() {
			        
			        @Override
			        public void onSuccess(XReadableModel result) {
				        
				        HashSet<XId> objectIDs = new HashSet<XId>();
				        for(XId xid : result) {
					        objectIDs.add(xid);
				        }
				        
				        @SuppressWarnings("unused")
				        SessionCachedModel currentModel = Controller.this.dataModel.getRepo(
				                Controller.this.lastClickedElement.getRepository()).getModel(
				                result.getId());
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.warn("Error", caught);
				        
			        }
		        });
		
	}
	
	public void registerTableController(TableController tableController) {
		this.tableController = tableController;
		
	}
	
	public void notifyTableController(XAddress eventLocation, Status status) {
		if(this.tableController == null) {
			SessionCachedModel model = this.dataModel.getRepo(eventLocation.getRepository())
			        .getModel(eventLocation.getModel());
			notifyEditorPanel(model);
		} else {
			this.tableController.notifyTable(eventLocation, status);
		}
	}
	
	public void addRepo(XId id) {
		this.dataModel.addRepoID(id);
		
	}
	
	public static SessionCachedModel getCurrentlySelectedModel() {
		XId currentRepo = instance.getSelectedModelAddress().getRepository();
		XId currenttModel = instance.getSelectedModelAddress().getModel();
		
		SessionCachedModel model = Controller.getInstance().getDataModel().getRepo(currentRepo)
		        .getModel(currenttModel);
		return model;
	}
	
	public void removeModel(final XAddress address) {
		XRepositoryCommand command = X.getCommandFactory().createForcedRemoveModelCommand(address);
		log.info("hi, " + address.toString());
		this.service.executeCommand(address.getRepository(), command, new AsyncCallback<Long>() {
			
			String resultString = "";
			
			@Override
			public void onSuccess(Long result) {
				if(XCommandUtils.success(result)) {
					this.resultString = "successfully deleted model " + address.getModel()
					        + " from repository";
				} else if(XCommandUtils.noChange(result)) {
					this.resultString = "no Changes!";
				} else if(XCommandUtils.failed(result)) {
					this.resultString = "commit failed!";
				} else {
					this.resultString = "i have no idea...";
				}
				Controller.this.tempStorage.notifyDialog(this.resultString);
				Controller.this.tempStorage.allowDialogClose();
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
}
