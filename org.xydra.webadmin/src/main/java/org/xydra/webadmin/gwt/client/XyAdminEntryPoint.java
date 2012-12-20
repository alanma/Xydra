package org.xydra.webadmin.gwt.client;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.shared.XyAdminService;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;


public class XyAdminEntryPoint implements EntryPoint {
	
	private static Logger log = LoggerFactory.getLogger(XyAdminEntryPoint.class);
	
	@Override
	public void onModuleLoad() {
		log.info("Starting XyAdminEntryPoint");
		
		XyAdminServiceAsync service = (XyAdminServiceAsync)GWT.create(XyAdminService.class);
		/* set endpoint to absolute path */
		ServiceDefTarget endpoint = (ServiceDefTarget)service;
		endpoint.setServiceEntryPoint("/xyadmin/ajax");
		
		Panel main = RootPanel.get("main");
		main.add(new XyAdmin(service));
		
		Panel loading = RootPanel.get("loading");
		loading.setVisible(false);
	}
	
}
