package org.xydra.webadmin.gwt.client;

import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.MainFrameWidget;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class XyAdmin extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,XyAdmin> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	@UiField
	public MainFrameWidget mainPanel;
	
	public XyAdminServiceAsync service;
	
	public XyAdmin(XyAdminServiceAsync service) {
		
		this.service = service;
		
		Controller.getInstance().addService(service);
		Controller.getInstance().addRepo(XX.toId("repo1"));
		// Controller.getInstance().addRepo(XX.toId("gae-repo"));
		
		initWidget(uiBinder.createAndBindUi(this));
	}
}
