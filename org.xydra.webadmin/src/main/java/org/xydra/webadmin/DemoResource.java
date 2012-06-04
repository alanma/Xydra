package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XX;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class DemoResource {
	
	public static final Logger log = LoggerFactory.getLogger(DemoResource.class);
	public static final String PAGE_NAME = "Add Demo Data";
	public static String URL;
	
	public static void restless(Restless restless, String prefix) {
		URL = prefix + "/demo";
		restless.addMethod(URL, "GET", DemoResource.class, "demo", true);
	}
	
	public static void demo(HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = Utils.startPage(res, PAGE_NAME, "");
		
		w.write("Generating phonebook demodata...");
		w.flush();
		XydraPersistence p = Utils.createPersistence(XX.toId("repo1"));
		WritableRepositoryOnPersistence repository = new WritableRepositoryOnPersistence(p,
		        XX.toId("DEMO"));
		
		XRepository xr = new MemoryRepository(XyAdminApp.ACTOR, "pass", XX.toId("repo1"));
		DemoModelUtil.addPhonebookModel(xr);
		
		w.write("  adding ...");
		w.flush();
		XCopyUtils.copyData(xr, repository);
		w.write(" done");
		
		Utils.endPage(w);
	}
	
}
