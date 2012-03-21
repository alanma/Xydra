package org.xydra.testgae.server.rest;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.change.RWCachingRepository;
import org.xydra.gae.AboutAppEngine;
import org.xydra.gae.admin.GaeConfigurationManager;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.util.HtmlWriterLogListener;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.NanoClock;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.server.util.XydraHtmlUtils;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeAssert;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class ConsistencyTestResource {
	
	private static final XID repoId = XX.toId("gae-data");
	private static final XID actorId = XX.toId("gae-data");
	
	private static final Logger log = LoggerFactory.getLogger(ConsistencyTestResource.class);
	private static HtmlWriterLogListener logListener;
	private static final XID ctId = XX.toId("__consistencyTest");
	
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
		GaeAssert.enable();
		c.stopAndStart("init");
		ServletUtils.headers(res, "text/html");
		Writer w = res.getWriter();
		String instance = XydraRuntime.getInstanceId() + "=" + AboutAppEngine.getThreadInfo();
		log.info("instanceId=" + instance);
		w.write("instance: " + instance);
		c.stopAndStart("headers,getInstanceId");
		
		XydraPersistence pers = XydraRuntime.getPersistence(repoId);
		XAddress modelAddress = XX.toAddress(repoId, ctId, null, null);
		
		long current = pers.getModelRevision(modelAddress).revision();
		w.write("Current rev: " + current + "<br/>\n");
		w.flush();
		c.stopAndStart("current");
		
		List<XEvent> events = pers.getEvents(modelAddress, 0, current);
		
		XydraHtmlUtils.writeEvents(events, w);
		
		w.flush();
		w.close();
	}
	
	public static void get(String create, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		NanoClock c = new NanoClock().start();
		GaeAssert.enable();
		GaeConfigurationManager.assertValidGaeConfiguration();
		c.stopAndStart("config");
		ServletUtils.headers(res, "text/html");
		Writer w = res.getWriter();
		String instance = XydraRuntime.getInstanceId() + "+=" + AboutAppEngine.getThreadInfo();
		log.info("instanceId=" + instance);
		c.stopAndStart("headers,getInstanceId");
		w.write("<div style='"
		
		+ "font-family: \"Courier New\", Courier, monospace;"
		
		+ "font-size: 13px;"
		
		+ "'>\n");
		w.write("instanceId=" + instance + "<br/>\n");
		w.flush();
		
		XydraPersistence persistence = XydraRuntime.getPersistence(repoId);
		WritableRepositoryOnPersistence nakedRepo = new WritableRepositoryOnPersistence(
		        persistence, actorId);
		// should be done in another way
		// InstanceContext.clearThreadContext();
		c.stopAndStart("init");
		// FIXME next 3 lines takes 98% of CPU time
		RWCachingRepository repo = new RWCachingRepository(nakedRepo, persistence, true);
		c.stopAndStart("prefetch");
		XWritableModel model = repo.createModel(ctId);
		c.stopAndStart("create-model");
		// create
		if(create != null && !create.equals("")) {
			model.createObject(XX.toId(create));
		}
		c.stopAndStart("create-object");
		// list
		w.write("version=" + model.getRevisionNumber() + "<br/>\n");
		c.stopAndStart("getRevNr");
		w.flush();
		int count = 0;
		for(XID id : model) {
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
		
		int result = RWCachingRepository.commit(repo, actorId);
		c.stopAndStart("commit");
		if(result != 200) {
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
