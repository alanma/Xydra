package org.xydra.testgae;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.store.Callback;
import org.xydra.store.GaeAllowAllStoreReadMethodsTest;
import org.xydra.store.GaeStoreReadMethodsTest;
import org.xydra.store.XydraStore;
import org.xydra.store.access.HashUtils;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class TestResource {
	
	private static final Logger log = LoggerFactory.getLogger(TestResource.class);
	
	public static void restless(Restless r) {
		r.addGet("/test1", TestResource.class, "test1");
		r.addGet("/test1nosec", TestResource.class, "test1noSecurity");
		r.addGet("/test1nosecloop", TestResource.class, "test1loop");
		r.addGet("/test2", TestResource.class, "test2");
		r.addGet("/test3", TestResource.class, "test3");
		r.addGet("/info", TestResource.class, "info");
		r.addGet("/", TestResource.class, "index");
	}
	
	public void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Test GAE");
		w.write("<p>This webapp tests the <a href=\"http://xydra.org\">Xydra GAE datamodel for Google AppEngine</a>.</p>");
		
		w.write("<h2>Generic tools</h2>");
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/assert",
		        "Shows status of virtual machine 'assert' keyword, should be off on GAE"),

		HtmlUtils.link("/info", "Is this test running on real GAE in production or not?"),

		HtmlUtils.link("/echo", "Echo current time to verify basic functionality"),

		HtmlUtils.link("/admin/restless", "Introspect all Restless methods")

		));
		
		w.write("<h2>Run JUnit tests</h2>");
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/test1", "Run Pre-defined JUnit test1"),

		HtmlUtils.link("/test1nosec", "Run test1 with no security checks"),

		HtmlUtils.link("/benchmark", "Runs a benchmark test")

		));
		
		w.write("<h2>Xmas Wish List example app</h2>");
		
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/xmas/repo1",
		        "Xmas example, repo1. You can use any repo with /xmas/{repoId}.")

		));
		
		w.write("Start any request URL with '/logged/' to record AppStats.");
		
		w.flush();
		w.close();
	}
	
	public void test1(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/plain");
		Writer w = res.getWriter();
		w.write("Test1 start.");
		test1();
		w.write("Test1 stop. Some operations might still be pending.");
		w.flush();
		w.close();
	}
	
	public void test1loop(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/plain");
		Writer w = res.getWriter();
		boolean error = false;
		while(!error) {
			w.write("Test1 start.");
			try {
				test1();
			} catch(Throwable e) {
				error = true;
				throw new RuntimeException("Triggered bug in Xydra", e);
			}
			w.write("Test1 stop. Some operations might still be pending.");
			w.flush();
		}
		w.close();
	}
	
	public void test2(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/plain");
		Writer w = res.getWriter();
		w.write("Test2 start.");
		test2(new OutputStreamWriter(res.getOutputStream(), "utf-8"));
		w.write("Test2 stop. Some operations might still be pending.");
		w.flush();
		w.close();
	}
	
	public void test3(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/plain");
		Writer w = res.getWriter();
		w.write("Test3 start.");
		test3(new OutputStreamWriter(res.getOutputStream(), "utf-8"));
		w.write("Test3 stop. Some operations might still be pending.");
		w.flush();
		w.close();
	}
	
	public String info() {
		return "GAE is in production? " + GaeTestfixer.inProduction();
	}
	
	public void test3(Writer w) throws IOException {
		runJunitTest(GaeAllowAllStoreReadMethodsTest.class, w);
		runJunitTest(GaeStoreReadMethodsTest.class, w);
	}
	
	public void runJunitTest(Class<?> clazz, Writer w) throws IOException {
		w.write("Running " + clazz.getName() + "...\n");
		Result result = JUnitCore.runClasses(clazz);
		w.write(result.getIgnoreCount() + " ignored, " + result.getRunCount() + " run, "
		        + result.getFailureCount() + " failed. Time: " + result.getRunTime() + " ms\n");
		for(Failure f : result.getFailures()) {
			w.write("=== " + f.getTestHeader() + "\n");
			w.write(f.getMessage() + "\n");
			w.write(f.getTrace() + "\n");
			w.write(f.getDescription() + "\n");
			w.write(f.getException() + "\n");
		}
		w.write("Done running " + clazz.getName() + ".\n");
	}
	
	XID test1_repoId = null;
	
	/**
	 * Use no security
	 */
	public void test1noSecurity() {
		log.info("Setting up store");
		XydraStore store = new DelegatingAllowAllStore(new GaePersistence(
		        GaePersistence.getDefaultRepositoryId()));
		
		XID actorId = XX.toId("test1");
		String passwordHash = HashUtils.getMD5("secret");
		
		log.info("Asking for repo id...");
		store.getRepositoryId(actorId, passwordHash, new Callback<XID>() {
			
			public void onSuccess(XID repoId) {
				log.info("Success: " + repoId);
				TestResource.this.test1_repoId = repoId;
			}
			
			public void onFailure(Throwable exception) {
				log.info("Error:", exception);
				throw new RuntimeException(exception);
			}
		});
		log.info("fired...");
	}
	
	public void test1() {
		log.info("Setting up store");
		XydraStore store = GaePersistence.get();
		XID actorId = XX.toId("test1");
		String passwordHash = HashUtils.getMD5("secret");
		
		log.info("Asking for repo id...");
		store.getRepositoryId(actorId, passwordHash, new Callback<XID>() {
			
			public void onSuccess(XID repoId) {
				log.info("Success: " + repoId);
				TestResource.this.test1_repoId = repoId;
			}
			
			public void onFailure(Throwable exception) {
				log.info("Error:", exception);
			}
		});
		log.info("fired...");
	}
	
	public void test2(Writer w) throws IOException {
		XID repoId = XX.toId("repo1");
		XydraPersistence persistence = new GaePersistence(repoId);
		w.write("RepoId = " + persistence.getRepositoryId() + "\n");
		w.flush();
		XID actorId = XX.toId("testActor");
		XID modelId = XX.toId("model1");
		persistence.executeCommand(
		        actorId,
		        MemoryRepositoryCommand.createAddCommand(
		                X.getIDProvider().fromComponents(repoId, null, null, null), true, modelId));
		w.write("Created model1.\n");
		w.flush();
	}
	
	/*
	 * For local testing without REST
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		TestResource tr = new TestResource();
		Writer w = new OutputStreamWriter(System.out, "utf-8");
		tr.test3(w);
		w.flush();
		w.close();
	}
	
}
