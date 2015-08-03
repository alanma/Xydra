package org.xydra.webadmin.gwt.client;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
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

		final XyAdminServiceAsync service = (XyAdminServiceAsync) GWT.create(XyAdminService.class);
		/* set endpoint to absolute path */
		final ServiceDefTarget endpoint = (ServiceDefTarget) service;
		endpoint.setServiceEntryPoint("/xyadmin/ajax");

		final Panel main = RootPanel.get("main");
		main.add(new XyAdmin(service));

		final Panel loading = RootPanel.get("loading");
		loading.setVisible(false);
	}

}
