package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.Base;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class DemoResource {

	public static final Logger log = LoggerFactory.getLogger(DemoResource.class);
	public static final String PAGE_NAME = "Add Demo Data";
	public static String URL;

	public static void restless(final Restless restless, final String prefix) {
		URL = prefix + "/demo";
		restless.addMethod(URL, "GET", DemoResource.class, "demo", true);
	}

	public static void demo(final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = Utils.startPage(res, PAGE_NAME, "");

		w.write("Generating phonebook demodata...");
		w.flush();
		final XydraPersistence p = Utils.createPersistence(Base.toId("repo1"));
		final WritableRepositoryOnPersistence repository = new WritableRepositoryOnPersistence(p,
				Base.toId("DEMO"));

		final XRepository xr = new MemoryRepository(XyAdminApp.ACTOR, "pass", Base.toId("repo1"));
		DemoModelUtil.addPhonebookModel(xr);

		w.write("  adding ...");
		w.flush();
		XCopyUtils.copyData(xr, repository);
		w.write(" done");

		Utils.endPage(w);
	}

}
