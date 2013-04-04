package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.resources.BundledRes;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * For empty fields in the table
 * 
 * @author Andi
 * 
 */
public class EmptyFieldWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EmptyFieldWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Button addButton;
	
	@SuppressWarnings("unused")
	private XAddress address;
	
	public EmptyFieldWidget(XAddress address) {
		super();
		this.address = address;
		initWidget(uiBinder.createAndBindUi(this));
		
		Image addImg = new Image(BundledRes.INSTANCE.images().add());
		this.addButton.getElement().appendChild(addImg.getElement());
		this.addButton.setVisible(false);
		
		this.addDomHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				EmptyFieldWidget.this.addButton.setVisible(true);
				
			}
		}, MouseOverEvent.getType());
		
		this.addDomHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				EmptyFieldWidget.this.addButton.setVisible(false);
				
			}
		}, MouseOutEvent.getType());
	}
	
	@UiHandler("addButton")
	void onClick(ClickEvent event) {
		// DataModel.getInstance().addField(this.address, null);
		
	}
	
}
