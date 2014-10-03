package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;

/**
 * Goal: A web application that offers admin capabilities beyond the ones
 * already provided by Google.
 * 
 * @author voelkel
 */
public class GaeMyAdminApp {

	/** Keep in sync with local index.html file */
	public static String URL = "/gma";

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GaeMyAdminApp.class);
	private static final String PAGE_NAME = "Main";

	public static void restless(Restless restless, String prefix) {
		restless.addMethod(URL, "GET", GaeMyAdminApp.class, "index", true);

		BlobAdminResource.restless(restless, URL);
		DatastoreAdminResource.restless(restless, URL);
		MemcacheAdminResource.restless(restless, URL);
		LogAdminResource.restless(restless, URL);
	}

	public static void index(HttpServletResponse res, HttpServletRequest req) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "");

		w.write(HtmlUtils.toOrderedList(Arrays.asList(

		HtmlUtils.link("/admin" + BlobAdminResource.URL, BlobAdminResource.PAGE_NAME),

		HtmlUtils.link("/admin" + DatastoreAdminResource.URL, DatastoreAdminResource.PAGE_NAME),

		HtmlUtils.link("/admin" + LogAdminResource.URL, LogAdminResource.PAGE_NAME),

		HtmlUtils.link("/admin" + MemcacheAdminResource.URL, MemcacheAdminResource.PAGE_NAME)

		)));
		AppConstants.endPage(w);
	}

}
