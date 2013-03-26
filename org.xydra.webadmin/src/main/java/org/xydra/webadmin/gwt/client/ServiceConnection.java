package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.util.TempStorage;
import org.xydra.webadmin.gwt.client.widgets.AddressWidget.CompoundActionCallback;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;


public class ServiceConnection {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private XyAdminServiceAsync service;
	
	private TempStorage tempStorage;
	
	public ServiceConnection(XyAdminServiceAsync service, TempStorage tempStorage) {
		this.service = service;
		this.tempStorage = tempStorage;
	}
	
	public void getModelIdsFromServer(final XAddress address,
	        final CompoundActionCallback compoundActionCallback) {
		
		TempStorage.showWaitCursor();
		final XId repoId = address.getRepository();
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
						DataModel.getInstance().getRepo(repoId).indexModel(modelID);
					}
					Controller.getInstance().present();
					if(compoundActionCallback != null) {
						compoundActionCallback.presentModelAndContinue();
					}
				}
				TempStorage.showDefaultCursor();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
				log.warn("Error", caught);
				TempStorage.showDefaultCursor();
			}
		});
		
	}
	
	public void loadModelsObjects(final XAddress address,
	        final CompoundActionCallback compoundActionCallback) {
		this.service.getModelSnapshot(address.getRepository(), address.getModel(),
		        new AsyncCallback<XReadableModel>() {
			        
			        @Override
			        public void onSuccess(XReadableModel result) {
				        if(result == null) {
					        @SuppressWarnings("unused")
					        WarningDialog d = new WarningDialog("model doesn't exist!");
					        DataModel.getInstance().getRepo(address.getRepository())
					                .addDeletedModel(address.getModel());
				        } else {
					        if(result.isEmpty()) {
						        log.error("no objects found!");
						        WarningDialog dialog = new WarningDialog("no objects found!");
						        dialog.show();
					        } else {
						        
						        DataModel.getInstance().getRepo(address.getRepository())
						                .getModel(result.getId()).indexModel(result);
						        if(compoundActionCallback != null) {
							        compoundActionCallback.presentObjects();
						        }
					        }
				        }
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.warn("Error", caught);
				        TempStorage.showDefaultCursor();
			        }
			        
		        });
		
	}
	
	public void commitAddedModel(final XAddress modelAddress, XCommand addModelCommand,
	        final XTransaction modelTransactions) {
		this.service.executeCommand(modelAddress.getRepository(), addModelCommand,
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
				        
				        ServiceConnection.this.tempStorage.notifyDialog(this.resultString);
				        commitModelTransactions(modelAddress, modelTransactions);
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        ServiceConnection.this.tempStorage.notifyDialog(this.resultString
				                + "error! \n" + caught.getMessage());
				        
			        }
		        });
	}
	
	public void commitModelTransactions(final XAddress modelAddress, XTransaction modelTransactions) {
		
		if(modelTransactions != null) {
			this.service.executeCommand(modelAddress.getRepository(), modelTransactions,
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
					        
					        SessionCachedModel model2 = DataModel.getInstance()
					                .getRepo(modelAddress.getRepository())
					                .getModel(modelAddress.getModel());
					        model2.markAsCommitted();
					        ServiceConnection.this.loadModelsObjects(modelAddress, null);
					        ServiceConnection.this.tempStorage.notifyDialog(this.resultString);
					        ServiceConnection.this.tempStorage.allowDialogClose();
					        
				        }
				        
				        @Override
				        public void onFailure(Throwable caught) {
					        ServiceConnection.this.tempStorage.notifyDialog(this.resultString
					                + "error! \n" + caught.getMessage());
					        
				        }
			        });
			
		} else {
			ServiceConnection.this.tempStorage.allowDialogClose();
		}
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
				ServiceConnection.this.tempStorage.notifyDialog(this.resultString);
				ServiceConnection.this.tempStorage.allowDialogClose();
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
}
