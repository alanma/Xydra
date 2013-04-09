package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.core.X;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.CommittingEvent;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.ICommitEventHandler;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.tableWidgets.TablePresenter;

import com.google.gwt.user.client.ui.Label;


public class EditorPanelPresenter extends Presenter {
	
	private IEditorPanel editorPanel;
	private XAddress currentModelAddress;
	private TablePresenter tablePresenter;
	
	public EditorPanelPresenter(IEditorPanel editorPanel) {
		this.editorPanel = editorPanel;
		
		init();
	}
	
	private void init() {
		XyAdmin.getInstance().getController().registerEditorPanelPresenter(this);
		this.editorPanel.init();
	}
	
	public void presentModel(XAddress address) {
		this.currentModelAddress = address;
		
		this.buildModelView();
	}
	
	public void buildModelView() {
		
		this.editorPanel.clear();
		
		ModelControlPanel modelControlPanel = new ModelControlPanel(this);
		
		ModelInformationPanel modelInformationPanel = new ModelInformationPanel(
		        this.currentModelAddress.getModel().toString());
		this.tablePresenter = new TablePresenter(this, modelInformationPanel);
		this.tablePresenter.generateTableOrShowInformation();
		this.editorPanel.add(modelControlPanel);
		this.editorPanel.add(modelInformationPanel);
		
		EventHelper.addModelChangeListener(this.currentModelAddress,
		        new IModelChangedEventHandler() {
			        
			        @Override
			        public void onModelChange(ModelChangedEvent event) {
				        if(event.getStatus().equals(EntityStatus.DELETED)) {
					        resetView();
					        
				        } else if(event.getStatus().equals(EntityStatus.EXTENDED)) {
					        // TODO implement...
					        // EditorPanelPresenter.this.presentNewInformation();
				        }
				        
				        else if(event.getStatus().equals(EntityStatus.INDEXED)) {
					        EditorPanelPresenter.this.tablePresenter
					                .generateTableOrShowInformation();
				        }
			        }
			        
		        });
	}
	
	private void resetView() {
		this.editorPanel.clear();
		Label noModelLabel = new Label("choose model via selection tree");
		this.editorPanel.add(noModelLabel);
	}
	
	public void loadModelsObjectsFromPersistence() {
		XyAdmin.getInstance().getController().loadModelsObjects(this.currentModelAddress, null);
	}
	
	public void handleFetchIDs() {
		@SuppressWarnings("unused")
		WarningDialog warning = new WarningDialog("not yet Implemented!");
	}
	
	void openCommitDialog(ModelControlPanel modelControlPanel) {
		CommittingDialog committingDialog = new CommittingDialog(this);
		committingDialog.show();
	}
	
	@SuppressWarnings("unused")
	void openDdiscardChangesDialog() {
		new ConfirmationDialog(this, "discard all Changes");
	}
	
	public void expandAll(String expandButtonText) {
		this.tablePresenter.expandAll(expandButtonText);
	}
	
	public XAddress getCurrentModelAddress() {
		return this.currentModelAddress;
	}
	
	public SessionCachedModel getCurrentModel() {
		SessionCachedModel selectedModel = XyAdmin.getInstance().getModel()
		        .getRepo(this.currentModelAddress.getRepository())
		        .getModel(this.currentModelAddress.getModel());
		return selectedModel;
	}
	
	public void commit(final CommittingDialog committingDialog) {
		XRepositoryCommand addModelCommand = null;
		if(XyAdmin.getInstance().getModel().getRepo(this.currentModelAddress.getRepository())
		        .isAddedModel(this.currentModelAddress.getModel())) {
			addModelCommand = X.getCommandFactory().createAddModelCommand(
			        this.currentModelAddress.getRepository(), this.currentModelAddress.getModel(),
			        true);
		}
		
		XTransaction modelTransactions = null;
		try {
			modelTransactions = XyAdmin.getInstance().getModel()
			        .getRepo(this.currentModelAddress.getRepository())
			        .getModelChanges(null, this.currentModelAddress).build();
		} catch(Exception e) {
			// just no changes
		}
		XyAdmin.getInstance().getController()
		        .commit(this.currentModelAddress, addModelCommand, modelTransactions);
		
		EventHelper.addCommittingListener(this.currentModelAddress, new ICommitEventHandler() {
			
			@Override
			public void onCommit(CommittingEvent event) {
				processCommitResponse(committingDialog, event);
			}
		});
	}
	
	private void processCommitResponse(final CommittingDialog committingDialog,
	        CommittingEvent event) {
		String message = "";
		long responseRevisionNumber = event.getNewRevision();
		
		if(event.getStatus().equals(CommittingEvent.CommitStatus.SUCCESS)) {
			if(XCommandUtils.success(responseRevisionNumber)) {
				message = "successfully committed! New revision number: " + event.getNewRevision();
				XyAdmin.getInstance().getModel().getRepo(event.getModelAddress().getRepository())
				        .setCommitted(event.getModelAddress().getModel());
				XyAdmin.getInstance().getController()
				        .loadModelsObjects(this.currentModelAddress, null);
			} else if(XCommandUtils.noChange(responseRevisionNumber)) {
				message = "no Changes!";
			} else if(XCommandUtils.failed(responseRevisionNumber)) {
				message = "commit failed!";
			} else {
				message = "i have no idea...";
			}
		} else {
			message = "commit failed!";
		}
		committingDialog.setText(message);
		committingDialog.addCloseOKButton();
		Controller.showDefaultCursor();
	}
	
	public void discardChanges() {
		this.getCurrentModel().discardAllChanges();
		this.buildModelView();
		
	}
}
