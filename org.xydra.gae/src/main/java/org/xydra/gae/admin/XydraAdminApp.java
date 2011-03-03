package org.xydra.gae.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.restless.Restless;
import org.xydra.server.rest.ShareXydraPersistenceApp;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaePersistence;


/**
 * Admin functions.
 * 
 * Currently mostly export/import.
 * 
 * @author xamde
 * 
 */
public class XydraAdminApp {
	
	private static String path;
	
	public static void restless(Restless restless, String path_) {
		path = path_;
		restless.addMethod(path + "/io", "GET", XydraAdminApp.class, "index", true);
		restless.addMethod(path + "/io/addDemoData", "GET", XydraAdminApp.class, "addDemoData",
		        true);
		CsvImportExportResource.restless(restless, path + "/io");
	}
	
	// TODO move to restless utils
	private static void setHeaders(HttpServletResponse res, String contentType, int statusCode) {
		res.setContentType(contentType);
		res.setCharacterEncoding("utf-8");
		res.setStatus(statusCode);
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		setHeaders(res, "text/html", 200);
		res.getWriter().println("<html><head><title>Xydra admin</title></head><body>");
		res.getWriter().println(
		        "<a href='" + req.getRequestURL() + "/addDemoData'>Add demo data</a>");
		res.getWriter().println("Format: <a href='" + req.getRequestURL() + "/csv'>CSV</a>");
		// TODO XML, JSON
		res.getWriter().println("</body></html>");
	}
	
	/**
	 * @param repository can be null (then the default is used)
	 * @return a WritableRepositoryOnPersistence
	 */
	static WritableRepositoryOnPersistence getRepository(Restless restless, String repository) {
		XID repositoryId;
		if(repository == null) {
			repositoryId = GaePersistence.getDefaultRepositoryId();
		} else {
			repositoryId = XX.toId(repository);
		}
		XydraPersistence xydraPersistence = ShareXydraPersistenceApp.getXydraPersistence(restless,
		        repositoryId);
		XID executingActorId = XX.toId("admin-csv-export");
		WritableRepositoryOnPersistence repo = new WritableRepositoryOnPersistence(
		        xydraPersistence, executingActorId);
		return repo;
	}
	
	public static String addDemoData(Restless restless) {
		WritableRepositoryOnPersistence repo = getRepository(restless,
		        GaePersistence.getDefaultRepositoryId() + "");
		if(repo.hasModel(DemoModelUtil.PHONEBOOK_ID)) {
			return "Repo contains already the Phonebook sample model";
		}
		
		XRepository memRepo = new MemoryRepository(XX.toId("actor"), "secret",
		        GaePersistence.getDefaultRepositoryId());
		DemoModelUtil.addPhonebookModel(memRepo);
		XCopyUtils.copyData(memRepo, repo);
		return "Done adding Phonebook sample model";
	}
}
