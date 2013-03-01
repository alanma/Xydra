package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.Restless.IRequestListener;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.webadmin.ModelResource.MStyle;


/**
 * Run this either by configuring your Restless servlet to run this
 * {@link XyAdminApp} or embed this admin tool in your own app by calling
 * {@link XyAdminApp#restless(Restless, String)} from your own restless app.
 * 
 * IMPROVE Add ability to load from servletcontext via
 * {@link XydraRestServer#SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE}
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XyAdminApp {
	
	public static final Logger log = LoggerFactory.getLogger(XyAdminApp.class);
	
	public static final String URL = "/xyadmin";
	
	public static final XId ACTOR = XX.toId("_XyAdmin");
	
	public static final String PAGE_NAME = "Main";
	
	public static void restless(Restless restless, String prefix) {
		/**
		 * Register for web request events. Make sure to not have
		 * XydraRuntime.startRequest/finishRequest in your code.
		 */
		restless.addRequestListener(new IRequestListener() {
			
			@Override
			public void onRequestStarted(IRestlessContext restlessContext) {
				XydraRuntime.startRequest();
			}
			
			@Override
			public void onRequestFinished(IRestlessContext restlessContext) {
				XydraRuntime.finishRequest();
			}
		});
		restless_setupServices(restless);
	}
	
	public static void restless_setupServices(Restless restless) {
		restless.addMethod(URL, "GET", XyAdminApp.class, "index", true);
		restless.addMethod(URL + "/list", "GET", XyAdminApp.class, "listRepos", true);
		DemoResource.restless(restless, URL);
		RepositoryResource.restless(restless, URL);
	}
	
	public static void index(HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = Utils.startPage(res, PAGE_NAME, "");
		
		w.write(HtmlUtils.toOrderedList(
		
		HtmlUtils.link("/admin" + DemoResource.URL, DemoResource.PAGE_NAME),
		
		HtmlUtils.link("/admin" + URL + "/list", "List all Models"),
		
		HtmlUtils.link("/admin" + RepositoryResource.URL + "/gae-data",
		        "Default repository 'gae-data'")
		
		));
		w.write("<p>Got to .." + RepositoryResource.URL + "/{repo-id}/</p>");
		
		Utils.endPage(w);
	}
	
	public static void listRepos(HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = Utils.startPage(res, PAGE_NAME, "List all Models in all Repositories");
		
		// find repositories in GAE datastore
		Iterator<XAddress> it = org.xydra.store.impl.gae.changes.Utils.findModelAdresses();
		while(it.hasNext()) {
			XAddress modelAddress = it.next();
			ModelResource.render(w, modelAddress, MStyle.link);
		}
		Utils.endPage(w);
	}
	
}
