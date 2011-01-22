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
import org.xydra.log.gae.GaeLoggerFactorySPI;
import org.xydra.restless.Restless;
import org.xydra.store.Callback;
import org.xydra.store.GaeAllowAllStoreReadMethodsTest;
import org.xydra.store.GaeStoreReadMethodsTest;
import org.xydra.store.XydraStore;
import org.xydra.store.access.HashUtils;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.gae.GaeTestfixer;


public class TestResource {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new GaeLoggerFactorySPI());
	}
	
	private static final Logger log = LoggerFactory.getLogger(TestResource.class);
	
	public static void restless(Restless r) {
		r.addGet("/test1", TestResource.class, "test1");
		r.addGet("/test2", TestResource.class, "test2");
		r.addGet("/test3", TestResource.class, "test3");
		r.addGet("/info", TestResource.class, "info");
	}
	
	public void test1(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		res.getWriter().println("Test1 start.");
		test1();
		res.getWriter().println("Test1 stop. Some operations might still be pending.");
	}
	
	public void test2(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		res.getWriter().println("Test2 start.");
		test2(res.getWriter());
		res.getWriter().println("Test2 stop. Some operations might still be pending.");
	}
	
	public void test3(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setContentType("text/plain");
		res.setStatus(200);
		res.getWriter().println("Test3 start.");
		test3(res.getWriter());
		res.getWriter().println("Test3 stop. Some operations might still be pending.");
	}
	
	public String info() {
		return "GAE is in production? " + GaeTestfixer.inProduction();
	}
	
	private void test3(Writer w) throws IOException {
		runJunitTest(GaeAllowAllStoreReadMethodsTest.class, w);
		runJunitTest(GaeStoreReadMethodsTest.class, w);
	}
	
	private void runJunitTest(Class<?> clazz, Writer w) throws IOException {
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
	
	private void test1() {
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
	
	private void test2(Writer w) throws IOException {
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
	
	/**
	 * For local testing without REST
	 * 
	 * @param args
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		TestResource tr = new TestResource();
		Writer w = new OutputStreamWriter(System.out, "utf-8");
		tr.test3(w);
		w.flush();
		w.close();
	}
	
}
