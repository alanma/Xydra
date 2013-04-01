package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.util.TableController;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ModelControlPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelControlPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	HorizontalPanel mainPanel;
	
	@UiField
	Button loadAllObjectsButton;
	
	@UiField
	Button loadAllIDsButton;
	
	@UiField
	Button commitModelChangesButton;
	
	@UiField
	Button discardModelChangesButton;
	
	@UiField
	Button expandAllButton;
	
	public ModelControlPanel(EditorPanelPresenter presenter) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("loadAllIDsButton")
	void onClickFetchIDs(ClickEvent event) {
		// Controller.getInstance().loadCurrentModelsIDs();
		WarningDialog dialog = new WarningDialog("not yet implemented");
		dialog.show();
	}
	
	@UiHandler("loadAllObjectsButton")
	void onClickFetchObjects(ClickEvent event) {
		Controller.getInstance().loadCurrentModelsObjects(null);
		
	}
	
	@UiHandler("commitModelChangesButton")
	public void onClickCommit(ClickEvent event) {
		CommittingDialog committingDialog = new CommittingDialog();
		committingDialog.show();
		
	}
	
	@SuppressWarnings("unused")
	@UiHandler("discardModelChangesButton")
	public void onClickDiscard(ClickEvent event) {
		
		new ConfirmationDialog("discard all Changes");
		
	}
	
	@UiHandler("expandAllButton")
	public void onClickExpand(ClickEvent event) {
		
		XAddress currentAddress = Controller.getInstance().getSelectedModelAddress();
		SessionCachedModel model = Controller.getCurrentlySelectedModel();
		
		if(this.expandAllButton.getText().equals("expand all objects")) {
			
			for(XId objectId : model) {
				Controller.getInstance().notifyTableController(
				        XX.resolveObject(currentAddress, objectId), TableController.Status.Opened);
			}
			
			this.expandAllButton.setText("close all objects");
		} else {
			
			for(XId objectId : model) {
				Controller.getInstance().notifyTableController(
				        XX.resolveObject(currentAddress, objectId), TableController.Status.Present);
			}
			
			this.expandAllButton.setText("expand all objects");
			
		}
	}
}
