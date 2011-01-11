package org.xydra.testgae;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.GaeLoggerFactorySPI;
import org.xydra.restless.Restless;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;
import org.xydra.store.access.GroupModelWrapper;
import org.xydra.store.base.HashUtils;
import org.xydra.store.impl.gae.GaeXydraStore;


public class TestResource {
	
	static {
		LoggerFactory.setLoggerFactorySPI(new GaeLoggerFactorySPI());
	}
	
	private static final Logger log = LoggerFactory.getLogger(TestResource.class);
	
	public static void restless(Restless r) {
		r.addGet("/test1", TestResource.class, "test1");
		r.addGet("/test2", TestResource.class, "test2");
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
	
	XID test1_repoId = null;
	
	private void test1() {
		log.info("Setting up store");
		XydraStore store = GaeXydraStore.get();
		XID actorId = XX.toId("test1");
		String passwordHash = HashUtils.getMD5("secret");
		
		XID modelId = XX.toId("model1");
		GroupModelWrapper gmw = new GroupModelWrapper(store, modelId);
		
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
		GaeXydraStore store = new GaeXydraStore(repoId);
		w.write("RepoId = " + store.getRepositoryId() + "\n");
		w.flush();
		XID actorId = XX.toId("testActor");
		XID modelId = XX.toId("model1");
		store.executeCommand(
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
		tr.test2(w);
		w.flush();
		w.close();
	}
	
}
