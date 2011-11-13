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
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


public class DemoResource {
	
	public static final Logger log = LoggerFactory.getLogger(DemoResource.class);
	
	public static void restless(Restless restless, String prefix) {
		
		restless.addMethod(prefix + "/demo/", "GET", DemoResource.class, "demo", true);
		
	}
	
	public static void demo(HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "Demo", new HeadLinkStyle("/s/xyadmin.css"));
		w.write("Generating demodata...");
		w.flush();
		XydraPersistence p = Utils.getPersistence(XX.toId("repo1"));
		WritableRepositoryOnPersistence repository = new WritableRepositoryOnPersistence(p,
		        XX.toId("DEMO"));
		
		XRepository xr = new MemoryRepository(WebadminResource.ACTOR, "pass", XX.toId("repo1"));
		DemoModelUtil.addPhonebookModel(xr);
		
		w.write("  adding ...");
		w.flush();
		XCopyUtils.copyData(xr, repository);
		w.write(" done");
		w.flush();
	}
	
}
