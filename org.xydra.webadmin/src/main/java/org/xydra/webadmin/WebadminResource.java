package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.server.rest.XydraRestServer;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.changes.Utils;
import org.xydra.webadmin.ModelResource.MStyle;


/**
 * The main manager resource.
 * 
 * It must always run at "/admin/xyadmin"
 * 
 * TODO ability to load from servletcontext via
 * {@link XydraRestServer#SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE}
 * 
 * @author xamde
 */
public class WebadminResource {
	
	public static final String XYADMIN = "/xyadmin";
	
	public static final Logger log = LoggerFactory.getLogger(WebadminResource.class);
	
	public static final XID ACTOR = XX.toId("_XydraWebadmin");
	
	public static void restless(Restless restless) {
		restless.addMethod(XYADMIN, "GET", WebadminResource.class, "index", true);
		DemoResource.restless(restless, XYADMIN);
		ObjectResource.restless(restless, XYADMIN);
		ModelResource.restless(restless, XYADMIN);
		RepositoryResource.restless(restless, XYADMIN);
	}
	
	public static void index(HttpServletResponse res) throws IOException {
		XydraRuntime.startRequest();
		
		// find repositories in GAE datastore
		Writer w = HtmlUtils.startHtmlPage(res, "XydraWebAdmin",
		        new HeadLinkStyle("/s/xyadmin.css"));
		w.write(HtmlUtils.link("/admin" + XYADMIN + "/demo", "Add phonebook demo data") + "<br/>\n");
		w.write("<h3>List of all models in all repositories</h3>");
		Iterator<XAddress> it = Utils.findModelAdresses();
		while(it.hasNext()) {
			XAddress modelAddress = it.next();
			ModelResource.render(w, modelAddress, MStyle.link);
		}
		
	}
}
