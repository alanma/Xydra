package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.base.XAddress;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class RemoveModelDialog extends DialogBox implements Observable {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RemoveModelDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Label infoText;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	private XAddress address;
	private CheckBox checkBox;
	private HorizontalPanel panel;
	
	public RemoveModelDialog(XAddress address) {
		
		super();
		
		this.address = address;
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveModelDialog.this.removeFromParent();
				Controller
				        .getInstance()
				        .getTempStorage()
				        .remove(RemoveModelDialog.this.address,
				                RemoveModelDialog.this.checkBox.getValue());
				Controller.getInstance().getTempStorage().register(RemoveModelDialog.this);
				RemoveModelDialog.this.mainPanel.clear();
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.infoText.setText("Are you sure you want to delete the item " + address.toString()
		        + "?");
		
		this.panel = new HorizontalPanel();
		this.panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.mainPanel.insert(this.panel, 1);
		HTML text = new HTML("remove permanentely from repository ");
		this.panel.add(text);
		this.checkBox = new CheckBox();
		this.panel.add(this.checkBox);
		
		this.setStyleName("dialogStyle");
		this.setText("remove Entity");
		this.getElement().setId("removeDialog");
		
		this.center();
	}
	
	@Override
	public void notifyMe(String message) {
		this.mainPanel.add(new HTML("message"));
		
	}
	
	@Override
	public void addCloseOKButton() {
		Button okButton = new Button("ok");
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveModelDialog.this.removeFromParent();
			}
		});
		this.mainPanel.add(okButton);
		
	}
	
}