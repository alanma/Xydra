package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.resources.BundledRes;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveModelDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EntityWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EntityWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel panel;
	
	@UiField
	HTMLPanel buttonPanel;
	
	@UiField
	HorizontalPanel idPanel;
	
	@UiField
	Anchor idLabel;
	
	@UiField
	Label revisionLabel;
	
	@UiField
	Button removeButton;
	
	@UiField
	Button addButton;
	
	private XAddress address;
	
	private String addText;
	private String addTitle;
	private String removeTitle;
	
	private String addFieldText = "enter Field ID";
	private String addObjectText = "enter Object ID";
	private String addFieldButtonTitle = "add new field";
	private String addObjectButtonTitle = "add new object";
	private String removeObjectButtonTitle = "remove this object";
	private String removeModelButtonTitle = "remove this model";
	
	private HandlerRegistration removeClickHandlerRegistration;
	
	public EntityWidget(XAddress address, ClickHandler anchorClickHandler) {
		super();
		
		this.address = address;
		initWidget(uiBinder.createAndBindUi(this));
		
		Image deleteImg = new Image(BundledRes.INSTANCE.images().delete());
		this.removeButton.getElement().appendChild(deleteImg.getElement());
		this.removeButton.setStyleName("imageButtonStyle");
		Image addImg = new Image(BundledRes.INSTANCE.images().add());
		this.addButton.getElement().appendChild(addImg.getElement());
		this.addButton.setStyleName("imageButtonStyle");
		this.removeButton.getElement().setAttribute("style", "float: right");
		this.addButton.getElement().setAttribute("style", "float: left");
		
		XId entityId = address.getModel();
		long revisionNumber = -1000l;
		SessionCachedModel model = Controller.getInstance().getDataModel()
		        .getRepo(address.getRepository()).getModel(entityId);
		
		switch(address.getAddressedType()) {
		case XMODEL:
			// entityID stays the same
			revisionNumber = model.getRevisionNumber();
			this.addText = this.addObjectText;
			this.addTitle = this.addObjectButtonTitle;
			this.removeTitle = this.removeModelButtonTitle;
			
			this.idPanel.addStyleName("modelIDLabel");
			this.idLabel.addStyleName("modelAnchorStyle");
			break;
		case XOBJECT:
			
			this.addText = this.addFieldText;
			this.addTitle = this.addFieldButtonTitle;
			this.removeTitle = this.removeObjectButtonTitle;
			entityId = address.getObject();
			XWritableObject object = model.getObject(entityId);
			revisionNumber = object.getRevisionNumber();
			
			this.idPanel.addStyleName("objectIDLabel");
			break;
		default:
			XyAssert.xyAssert(false);
		}
		
		this.idLabel.setText(entityId.toString());
		this.idLabel.addClickHandler(anchorClickHandler);
		this.revisionLabel.setText("rev. " + revisionNumber);
		this.addButton.setTitle(this.addTitle);
		this.removeButton.setTitle(this.removeTitle);
		
		this.addDomHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				EntityWidget.this.addButton.setVisible(true);
				EntityWidget.this.removeButton.setVisible(true);
				
			}
		}, MouseOverEvent.getType());
		
		this.addDomHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				EntityWidget.this.addButton.setVisible(false);
				EntityWidget.this.removeButton.setVisible(false);
				
			}
		}, MouseOutEvent.getType());
		
		this.addButton.setVisible(false);
		this.removeButton.setVisible(false);
		
		this.removeClickHandlerRegistration = this.removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveElementDialog removeDialog = new RemoveElementDialog(
				        EntityWidget.this.address);
				removeDialog.show();
				
			}
		});
		
	}
	
	@UiHandler("addButton")
	void onClickAdd(ClickEvent event) {
		AddElementDialog addDialog = new AddElementDialog(EntityWidget.this.address, this.addText);
		addDialog.show();
		addDialog.selectEverything();
		
	}
	
	public void setDeleteModelDialog() {
		this.removeClickHandlerRegistration.removeHandler();
		this.removeButton.addClickHandler(new ClickHandler() {
			
			@SuppressWarnings("unused")
			@Override
			public void onClick(ClickEvent event) {
				
				new RemoveModelDialog(EntityWidget.this.address);
				
			}
		});
	}
	
	public void setStatusDeleted() {
		this.idLabel.addStyleName("deletedStyle");
		
	}
	
	public void setRevisionUnknown() {
		this.revisionLabel.setText("???");
	}
}
