package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.FieldWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EditorPanel extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EditorPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	public EditorPanel() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		Controller.getInstance().registerEditorPanel(this);
		this.mainPanel.add(new Label("TestLabel"));
	}
	
	public void notifyMe(XReadableObject result) {
		log.info("editorPanel notified!");
		this.mainPanel.clear();
		// XObject thing = XCopyUtils.copyObject(XX.toId("ich"), "HI", result);
		// this.mainPanel.add(new XObjectEditor(thing));
		boolean isEmpty = true;
		for(XID xid : result) {
			FieldWidget field = new FieldWidget(xid, result.getField(xid).getValue());
			this.mainPanel.add(field);
			isEmpty = false;
		}
		if(isEmpty)
			this.mainPanel.add(new Label("this is an empty Object!"));
		
	}
	
	public void notifyMe(XReadableModel result) {
		log.info("editorPanel notified!");
		this.mainPanel.clear();
		ModelPane modelPane = new ModelPane(result);
		this.mainPanel.add(modelPane);
	}
}
