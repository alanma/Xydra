package org.xydra.gae.admin;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.gae.AboutAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.gae.GaePersistence;


public class XydraInfoResource {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XydraInfoResource.class);
	
	/**
	 * @param r restless
	 * @param path should be empty string
	 */
	public static void restless(Restless r, String path) {
		r.addMethod("/xinfo/{repoId}", "GET", XydraInfoResource.class, "index", true,

		new RestlessParameter("repoId", null)

		);
	}
	
	public static void index(String repoIdStr, HttpServletResponse res) throws IOException {
		String instance = AboutAppEngine.getInstanceId() + "---" + AboutAppEngine.getThreadInfo();
		String title = "Repo " + repoIdStr + " on " + instance;
		Writer w = HtmlUtils.startHtmlPage(res, title);
		w.write("<h3>" + title + "</h3>\n");
		w.flush();
		
		XID repoId = XX.toId(repoIdStr);
		GaePersistence p = new GaePersistence(repoId);
		for(XID modelId : p.getModelIds()) {
			w.write("<h2>Model: " + modelId + "</h2>\n");
			w.flush();
			
			XAddress modelAddress = XX.toAddress(repoId, modelId, null, null);
			RevisionState rev = p.getModelRevision(modelAddress);
			w.write("rev=" + rev.revision() + " exists:" + rev.modelExists() + "<br/>\n");
			w.flush();
			
			List<XEvent> events = p.getEvents(modelAddress, 0, rev.revision());
			XydraHtmlUtils.writeEvents(events, w);
			
		}
		w.flush();
		w.close();
	}
	
}
