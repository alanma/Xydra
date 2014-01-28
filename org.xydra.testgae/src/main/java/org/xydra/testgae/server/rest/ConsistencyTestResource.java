package org.xydra.testgae.server.rest;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.core.XX;
import org.xydra.gae.admin.GaeConfigurationManager;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.log.util.listener.HtmlWriterLogListener;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.NanoClock;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraRuntime;
import org.xydra.store.session.ChangeSession;
import org.xydra.store.session.DelegatingSessionPersistence;
import org.xydra.store.session.ISessionPersistence;
import org.xydra.store.session.SessionModel;
import org.xydra.xgae.gaeutils.AboutAppEngine;
import org.xydra.xgae.gaeutils.GaeTestfixer;


/**
 * Expose a textual list which appends each access. Great for testing
 * concurrency.
 * 
 * @author xamde
 */
public class ConsistencyTestResource {
	
	private static final XId repoId = XX.toId("gae-data");
	private static final XId actorId = XX.toId("gae-data");
	
	private static final Logger log = LoggerFactory.getLogger(ConsistencyTestResource.class);
	private static HtmlWriterLogListener logListener;
	private static final XId ctId = XX.toId("__consistencyTest");
	
	/**
	 * Expose /consistency for read/write access and /consistency/events to help
	 * debugging
	 * 
	 * @param restless
	 * @param prefix
	 */
	public static void restless(Restless restless, String prefix) {
		LoggerFactory.addLogListener(getLogListener());
		restless.addMethod("/consistency/", "GET", ConsistencyTestResource.class, "get", false,
		        new RestlessParameter("create", ""));
		restless.addMethod("/consistency/events", "GET", ConsistencyTestResource.class, "events",
		        false);
	}
	
	private static synchronized HtmlWriterLogListener getLogListener() {
		if(logListener == null) {
			logListener = new HtmlWriterLogListener();
		}
		return logListener;
	}
	
	public static void events(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		NanoClock c = new NanoClock().start();
		XyAssert.enable();
		c.stopAndStart("init");
		ServletUtils.headers(res, "text/html");
		Writer w = res.getWriter();
		String instance = XydraRuntime.getInstanceId() + "=" + AboutAppEngine.getThreadInfo();
		log.info("instanceId=" + instance);
		w.write("instance: " + instance);
		c.stopAndStart("headers,getInstanceId");
		
		XydraPersistence pers = XydraRuntime.getPersistence(repoId);
		XAddress modelAddress = XX.toAddress(repoId, ctId, null, null);
		
		long tentativeRev = pers.getModelRevision(new GetWithAddressRequest(modelAddress, true))
		        .tentativeRevision();
		w.write("Current rev: " + tentativeRev + "<br/>\n");
		w.flush();
		c.stopAndStart("current");
		
		List<XEvent> events = pers.getEvents(modelAddress, 0, tentativeRev);
		
		XydraHtmlUtils.writeEvents(events, w);
		
		w.flush();
		w.close();
	}
	
	public static void get(String createStr, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		NanoClock c = new NanoClock().start();
		XyAssert.enable();
		GaeConfigurationManager.assertValidGaeConfiguration();
		c.stopAndStart("config");
		
		String instance = XydraRuntime.getInstanceId() + "+=" + AboutAppEngine.getThreadInfo();
		log.info("instanceId=" + instance);
		c.stopAndStart("getInstanceId");
		
		boolean create = ServletUtils.isSet(createStr);
		XydraPersistence persistence = XydraRuntime.getPersistence(repoId);
		ISessionPersistence sessionPersistence = new DelegatingSessionPersistence(persistence,
		        actorId);
		ChangeSession session = ChangeSession.createSession(sessionPersistence, create, actorId);
		c.stopAndStart("init");
		
		SessionModel model = session.openModel(ctId, create).loadAllObjects();
		log.debug("Loaded model DATA?rev=" + model.getRevisionNumber() + "&i_addr="
		        + model.getAddress() + "&instance=" + AboutAppEngine.getInstanceId()
		        + "&changesMethod=ConsTestRes.get");
		c.stopAndStart("prefetch");
		
		// create
		if(create) {
			try {
				model.createObject(XX.toId(createStr));
			} catch(Exception e) {
				throw new RuntimeException("Could not create", e);
			}
		}
		c.stopAndStart("create-object");
		
		ServletUtils.headers(res, "text/html");
		Writer w = res.getWriter();
		w.write("<div style='"
		
		+ "font-family: \"Courier New\", Courier, monospace;"
		
		+ "font-size: 13px;"
		
		+ "'>\n");
		w.write("instanceId=" + instance + "<br/>\n");
		w.flush();
		// list
		w.write("version=" + model.getRevisionNumber() + "<br/>\n");
		c.stopAndStart("getRevNr");
		w.flush();
		int count = 0;
		for(XId id : model) {
			w.write("id=" + id.toString() + "<br/>\n");
			w.flush();
			count++;
		}
		c.stopAndStart("model.children");
		w.write("size=" + count + "<br/>\n");
		w.flush();
		
		w.write("----<br/>\n" + getLogListener().getAndResetBuffer());
		w.flush();
		c.stopAndStart("logger-write");
		
		long result = model.commitToSessionPersistence();
		c.stopAndStart("commit");
		if(XCommandUtils.failed(result)) {
			throw new RuntimeException("commit failed " + result);
		}
		w.write("COMMIT: " + result + "<br/>\n");
		w.flush();
		
		w.write("----<br/>\n" + getLogListener().getAndResetBuffer());
		w.flush();
		c.stopAndStart("logger-flush");
		
		w.write("Stats: " + c.getStats());
		w.flush();
		
		w.close();
	}
}
