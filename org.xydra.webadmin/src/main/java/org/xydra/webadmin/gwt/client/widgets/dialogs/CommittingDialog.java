package org.xydra.webadmin.gwt.client.widgets.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.delta.DeltaUtils.IFieldDiff;
import org.xydra.core.model.delta.DeltaUtils.IObjectDiff;
import org.xydra.core.util.DumpUtils;
import org.xydra.core.util.DumpUtils.XidComparator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class CommittingDialog extends DialogBox implements Observable {
	
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
				XAddress selectedModelAddress = Controller.getInstance().getSelectedModelAddress();
				Controller.getInstance().getTempStorage().register(CommittingDialog.this);
				XRepositoryCommand addModelCommand = null;
				if(Controller.getInstance().getDataModel()
				        .getRepo(selectedModelAddress.getRepository())
				        .isAddedModel(selectedModelAddress.getModel())) {
					addModelCommand = X.getCommandFactory().createAddModelCommand(
					        selectedModelAddress.getRepository(), selectedModelAddress.getModel(),
					        true);
				}
				
				XTransaction modelTransactions = null;
				try {
					modelTransactions = Controller.getInstance().getDataModel()
					        .getRepo(selectedModelAddress.getRepository())
					        .getModelChanges(null, selectedModelAddress).build();
				} catch(Exception e) {
					// just no changes
				}
				Controller.getInstance().commit(addModelCommand, modelTransactions);
				
				CommittingDialog.this.mainPanel.clear();
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
		
		Controller controller = Controller.getInstance();
		XAddress selectedModelAddress = controller.getSelectedModelAddress();
		SessionCachedModel model = controller.getDataModel()
		        .getRepo(selectedModelAddress.getRepository())
		        .getModel(selectedModelAddress.getModel());
		
		this.changesPanel.add(new HTML("Changes: <br> <br>"));
		
		if(controller.getDataModel().getRepo(selectedModelAddress.getRepository())
		        .isAddedModel(selectedModelAddress.getModel())) {
			this.changesPanel.add(new HTML("---added Model "
			        + selectedModelAddress.getModel().toString() + "---"));
		}
		this.changesPanel.add(new HTML(changesToString(model).toString()));
		
	}
	
	public void notifyMe(String message) {
		this.mainPanel.add(new Label(message));
	}
	
	public void addCloseOKButton() {
		Button okButton = new Button("ok");
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				CommittingDialog.this.removeFromParent();
			}
		});
		this.mainPanel.add(okButton);
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IModelDiff changedModel) {
		StringBuilder sb = new StringBuilder();
		List<XReadableObject> addedList = new ArrayList<XReadableObject>(changedModel.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for(XReadableObject addedObject : addedList) {
			sb.append("<br><br>=== ADDED   Object '" + addedObject.getId() + "' ===<br/>\n");
			sb.append(DumpUtils.toStringBuffer(addedObject).toString());
		}
		List<XId> removedList = new ArrayList<XId>(changedModel.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for(XId removedObjectId : removedList) {
			sb.append("<br><br>=== REMOVED Object '" + removedObjectId + "' ===<br/>\n");
		}
		List<IObjectDiff> potentiallyChangedList = new ArrayList<IObjectDiff>(
		        changedModel.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for(IObjectDiff changedObject : potentiallyChangedList) {
			if(changedObject.hasChanges()) {
				sb.append("<br><br>=== CHANGED Object '" + changedObject.getId() + "' === <br/>\n");
				sb.append(changesToString(changedObject).toString());
			}
		}
		return sb;
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IObjectDiff changedObject) {
		StringBuilder sb = new StringBuilder();
		List<XReadableField> addedList = new ArrayList<XReadableField>(changedObject.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for(XReadableField field : addedList) {
			sb.append("--- ADDED Field '" + field.getId() + "' ---<br/>\n");
			sb.append(DumpUtils.toStringBuffer(field));
		}
		List<XId> removedList = new ArrayList<XId>(changedObject.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for(XId objectId : changedObject.getRemoved()) {
			sb.append("--- REMOVED Field '" + objectId + "' ---<br/>\n");
		}
		List<IFieldDiff> potentiallyChangedList = new ArrayList<IFieldDiff>(
		        changedObject.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for(IFieldDiff changedField : potentiallyChangedList) {
			if(changedField.isChanged()) {
				sb.append("--- CHANGED Field '" + changedField.getId() + "' ---<br/>\n");
				sb.append(changesToString(changedField).toString());
			}
		}
		return sb;
	}
	
	public static StringBuilder changesToString(final DeltaUtils.IFieldDiff changedField) {
		StringBuilder sb = new StringBuilder();
		sb.append("'" + changedField.getInitialValue() + "' ==> '" + changedField.getValue()
		        + "' \n");
		return sb;
	}
	
}
