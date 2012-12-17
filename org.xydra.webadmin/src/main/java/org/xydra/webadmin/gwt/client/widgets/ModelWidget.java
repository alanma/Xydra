package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.util.ModelConfiguration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;


public class ModelWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private ModelConfiguration modelConfig;
	
	@UiField
	public Button modelButton;
	
	@UiField
	public FlowPanel buttonPanel;
	
	@UiField
	public Button addUserButton;
	
	@UiField
	public FlowPanel objectPanel;
	
	public ModelWidget(ModelConfiguration modelConfig) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.modelConfig = modelConfig;
		
		this.modelButton.setText(modelConfig.modelId.toString());
		
		this.modelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getModelInfos();
				
			}
		});
	}
	
	protected void getModelInfos() {
		
		this.modelConfig.adminObject.service.getModelSnapshot(this.modelConfig.repoId,
		        this.modelConfig.modelId, new AsyncCallback<XReadableModel>() {
			        
			        @Override
			        public void onSuccess(XReadableModel model) {
				        log.info("now: model " + model);
				        
				        for(XID objectID : model) {
					        
					        log.info("now: object " + objectID + ":");
					        XReadableObject object = model.getObject(objectID);
					        
					        ObjectWidget objectWidget = new ObjectWidget(objectID, object
					                .getRevisionNumber());
					        
					        boolean flag = false;
					        for(XID fieldID : object) {
						        
						        log.info("now: field " + fieldID);
						        
						        String fieldValue = "no Value";
						        if(!fieldID.toString().equals("emptyfield")) {
							        XReadableField field = object.getField(fieldID);
							        
							        fieldValue = field.getValue().toString();
							        flag = true;
						        }
						        FieldWidget currentField = new FieldWidget(fieldID, fieldValue);
						        
						        objectWidget.add(currentField);
					        }
					        if(flag == false) {
						        log.info("no fields for " + objectID);
					        }
					        
					        log.info("");
					        ModelWidget.this.add(objectWidget);
				        }
				        
			        }
			        
			        @Override
			        public void onFailure(Throwable caught) {
				        log.warn("Error", caught);
			        }
		        });
		
	}
	
	private void add(Widget widget) {
		this.objectPanel.add(widget);
	}
	
	// private void addObject() {
	// XRepositoryCommand command = X.getCommandFactory().createAddModelCommand(
	// this.modelConfig.repoId, XX.toId("model1"), true);
	// // FIXME das hier funktioniert nicht
	// this.modelConfig.adminObject.service.executeCommand(this.modelConfig.repoId,
	// command,
	// new AsyncCallback<Long>() {
	//
	// @Override
	// public void onSuccess(Long result) {
	// log.info("Server said: " + result);
	// }
	//
	// @Override
	// public void onFailure(Throwable caught) {
	// log.warn("Error", caught);
	// }
	// });
	//
	// }
}
