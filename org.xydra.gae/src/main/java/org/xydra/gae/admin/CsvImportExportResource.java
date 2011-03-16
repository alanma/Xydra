package org.xydra.gae.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.server.inout.CsvExport;


/**
 * Admin functions.
 * 
 * Currently mostly export/import.
 * 
 * @author xamde
 * 
 */
public class CsvImportExportResource {
	
	private static String path;
	
	public static void restless(Restless restless, String path_) {
		path = path_;
		restless.addMethod(path + "/csv", "GET", CsvImportExportResource.class, "index", true);
		restless.addMethod(path + "/csv/import", "POST", CsvImportExportResource.class,
		        "csvImport", true, new RestlessParameter("csv", null), new RestlessParameter(
		                "repository", null));
		restless.addMethod(path + "/csv/export", "GET", CsvImportExportResource.class, "csvExport",
		        true, new RestlessParameter("repository", null));
	}
	
	public static void index(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		res.getWriter().println("<html><head><title>Xydra admin / CSV</title></head><body>");
		res.getWriter().println("Export: <a href='" + req.getRequestURL() + "/export'>CSV</a>");
		// FIXME impl.
		res.getWriter().println("</body></html>");
	}
	
	public static void csvImport(String csv, String repository, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		res.getWriter().println(
		        "<html><head><title>Xydra admin / CSV / Import</title></head><body>");
		res.getWriter().println("Not implemented yet");
		// FIXME impl.
		res.getWriter().println("</body></html>");
	}
	
	public void csvExport(String repository, Restless restless, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/csv");
		/*
		 * via
		 * http://stackoverflow.com/questions/398237/how-to-use-the-csv-mime-
		 * type
		 */
		WritableRepositoryOnPersistence repo = XydraAdminApp.getRepository(restless, repository);
		res.setHeader("Content-disposition", "attachment;filename=" + repo.getID() + ".csv");
		
		CsvExport.toWriter(repo, res.getWriter());
		res.getWriter().flush();
	}
	
}
