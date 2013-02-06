package org.xydra.webadmin.gwt.client.widgets.dialogs;

import java.util.Collection;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.model.delta.DeltaUtils.IFieldDiff;
import org.xydra.core.model.delta.DeltaUtils.IObjectDiff;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class CommittingDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,CommittingDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	VerticalPanel changesPanel;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	public CommittingDialog() {
		
		super();
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Controller.getInstance().commit();
				Controller.getInstance().getTempStorage().register(CommittingDialog.this);
				
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		
		setWidget(uiBinder.createAndBindUi(this));
		this.setStyleName("dialogStyle");
		this.setText("check changes before committing");
		this.center();
		
		this.showChanges();
		
	}
	
	private void showChanges() {
		
		boolean changesHappened = false;
		
		this.changesPanel.add(new Label("changes: "));
		
		Controller controller = Controller.getInstance();
		XAddress selectedModel = controller.getSelectedModelAddress();
		SessionCachedModel model = controller.getDataModel().getRepo(selectedModel.getRepository())
		        .getModel(selectedModel.getModel());
		
		Collection<? extends IObjectDiff> objectChanges = model.getPotentiallyChanged();
		for(IObjectDiff iObjectDiff : objectChanges) {
			changesHappened = true;
			this.changesPanel.add(new Label("changes in Object " + iObjectDiff.getId().toString()));
			
			Collection<? extends IFieldDiff> fieldChanges = iObjectDiff.getPotentiallyChanged();
			for(IFieldDiff iFieldDiff : fieldChanges) {
				this.changesPanel
				        .add(new Label("changes in Field " + iFieldDiff.getId().toString()));
				this.changesPanel.add(new Label("old Value: " + iFieldDiff.getInitialValue()
				        + ", new Value: " + iFieldDiff.getValue()));
				
			}
			
			Collection<? extends XReadableField> addedFields = iObjectDiff.getAdded();
			for(XReadableField xReadableField : addedFields) {
				this.changesPanel.add(new Label("added Field " + xReadableField.getId().toString()
				        + "with value: " + xReadableField.getValue()));
			}
			
			Collection<XID> removedFields = iObjectDiff.getRemoved();
			for(XID xid : removedFields) {
				this.changesPanel.add(new Label("removed Field " + xid.toString()));
			}
		}
		
		if(!changesHappened) {
			this.mainPanel.clear();
			this.mainPanel.add(new Label("no Changes!"));
			this.addCloseOKButton();
		}
		
	}
	
	public void notifyMe(String message) {
		this.mainPanel.clear();
		this.mainPanel.add(new Label(message));
		
		addCloseOKButton();
	}
	
	private void addCloseOKButton() {
		Button okButton = new Button("ok");
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				CommittingDialog.this.removeFromParent();
			}
		});
		this.mainPanel.add(okButton);
	}
}
