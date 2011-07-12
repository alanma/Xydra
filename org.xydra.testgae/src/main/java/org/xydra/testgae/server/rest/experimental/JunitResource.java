package org.xydra.testgae.server.rest.experimental;

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


/**
 * FIXME Max to max: not ready - not required
 * 
 * Can run JUnit tests on the server side.
 * 
 * @author xamde
 */
public class JunitResource {
	
	private static final Logger log = LoggerFactory.getLogger(JunitResource.class);
	
	public static void restless(Restless r) {
		r.addGet("/test1", JunitResource.class, "test1");
		r.addGet("/test2", JunitResource.class, "test2");
		r.addGet("/test3", JunitResource.class, "test3");
		// .. add more tests here
		r.addGet("/info", JunitResource.class, "info");
		r.addGet("/", JunitResource.class, "index");
	}
	
	public void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "Xydra Test GAE");
		w.write("<h2>Run JUnit tests</h2>");
		w.write(HtmlUtils.toOrderedList(

		HtmlUtils.link("/test1", "Run Pre-defined JUnit test1"),

		HtmlUtils.link("/test1nosec", "Run test1 with no security checks"),

		HtmlUtils.link("/benchmark", "Runs a benchmark test")

		));
		
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
	
	public void test3(Writer w) throws IOException {
		runJunitTest(GaeAllowAllStoreReadMethodsTest.class, w);
		runJunitTest(GaeStoreReadMethodsTest.class, w);
	}
	
	/**
	 * Runs all tests in given class
	 * 
	 * @param clazz should contain @Test methods
	 * @param w a writer where results are printed as HTML
	 * @throws IOException if the writer has {@link IOException}
	 */
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
				JunitResource.this.test1_repoId = repoId;
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
				JunitResource.this.test1_repoId = repoId;
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
		JunitResource tr = new JunitResource();
		Writer w = new OutputStreamWriter(System.out, "utf-8");
		tr.test3(w);
		w.flush();
		w.close();
	}
	
}
