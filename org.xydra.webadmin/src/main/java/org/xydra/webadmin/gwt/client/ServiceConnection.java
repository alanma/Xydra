package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.CommitStatus;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.widgets.AddressWidget.CompoundActionCallback;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The only class that talks to the server
 * 
 * @author andi
 */
public class ServiceConnection {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private XyAdminServiceAsync service;
	
	public ServiceConnection(XyAdminServiceAsync service) {
		this.service = service;
	}
	
	public void getModelIdsFromServer(final XAddress address,
	        final CompoundActionCallback compoundActionCallback) {
		
		Controller.showWaitCursor();
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
						XyAdmin.getInstance().getModel().getRepo(repoId).registerModel(modelID);
						
					}
					EventHelper.fireRepoChangeEvent(XX.toAddress(repoId, null, null, null),
					        EntityStatus.REGISTERED);
					if(compoundActionCallback != null) {
						compoundActionCallback.presentModelAndContinue();
					} else
						Controller.showDefaultCursor();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				
				log.warn("Error", caught);
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
					        XyAdmin.getInstance().getModel().getRepo(address.getRepository())
					                .addDeletedModel(address.getModel());
				        } else {
					        if(result.isEmpty()) {
						        log.error("no objects found!");
						        WarningDialog dialog = new WarningDialog("no objects found!");
						        dialog.show();
					        } else {
						        
						        XyAdmin.getInstance().getModel().getRepo(address.getRepository())
						                .getModel(result.getId()).indexModel(result);
						        EventHelper.fireModelChangeEvent(address, EntityStatus.INDEXED,
						                XX.toId("dummy"));
						        if(compoundActionCallback != null) {
							        compoundActionCallback.presentObjects();
						        }
					        }
				        }
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.warn("Error", caught);
			        }
			        
		        });
		
	}
	
	public void commitAddedModel(final XAddress modelAddress, XCommand addModelCommand,
	        final XTransaction modelTransactions) {
		this.service.executeCommand(modelAddress.getRepository(), addModelCommand,
		        new AsyncCallback<Long>() {
			        
			        @SuppressWarnings("unused")
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
				        
				        commitModelTransactions(modelAddress, modelTransactions);
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        
			        }
		        });
	}
	
	public void commitModelTransactions(final XAddress modelAddress, XTransaction modelTransactions) {
		
		if(modelTransactions != null) {
			this.service.executeCommand(modelAddress.getRepository(), modelTransactions,
			        new AsyncCallback<Long>() {
				        
				        @SuppressWarnings("unused")
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
					        
					        SessionCachedModel model2 = XyAdmin.getInstance().getModel()
					                .getRepo(modelAddress.getRepository())
					                .getModel(modelAddress.getModel());
					        model2.markAsCommitted();
					        ServiceConnection.this.loadModelsObjects(modelAddress, null);
					        
				        }
				        
				        @Override
				        public void onFailure(Throwable caught) {
					        
				        }
			        });
			
		} else {
			// ServiceConnection.this.tempStorage.allowDialogClose();
		}
	}
	
	public void removeModel(final XAddress address) {
		XRepositoryCommand command = X.getCommandFactory().createForcedRemoveModelCommand(address);
		log.info("hi, " + address.toString());
		this.service.executeCommand(address.getRepository(), command, new AsyncCallback<Long>() {
			
			@SuppressWarnings("unused")
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
				EventHelper.fireCommitEvent(address, CommitStatus.SUCCESS);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
}
