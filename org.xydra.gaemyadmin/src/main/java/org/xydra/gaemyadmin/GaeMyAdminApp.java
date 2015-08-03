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
import org.xydra.restless.utils.SharedHtmlUtils;

/**
 * Goal: A web application that offers admin capabilities beyond the ones
 * already provided by Google.
 *
 * @author xamde
 */
public class GaeMyAdminApp {

	/** Keep in sync with local index.html file */
	public static String URL = "/gma";

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GaeMyAdminApp.class);
	private static final String PAGE_NAME = "Main";

	public static void restless(final Restless restless, final String prefix) {
		restless.addMethod(URL, "GET", GaeMyAdminApp.class, "index", true);

		BlobAdminResource.restless(restless, URL);
		DatastoreAdminResource.restless(restless, URL);
		MemcacheAdminResource.restless(restless, URL);
		LogAdminResource.restless(restless, URL);
	}

	public static void index(final HttpServletResponse res, final HttpServletRequest req) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = AppConstants.startPage(res, PAGE_NAME, "");

		w.write(SharedHtmlUtils.toOrderedList(Arrays.asList(

		SharedHtmlUtils.link("/admin" + BlobAdminResource.URL, BlobAdminResource.PAGE_NAME),

		SharedHtmlUtils.link("/admin" + DatastoreAdminResource.URL, DatastoreAdminResource.PAGE_NAME),

		SharedHtmlUtils.link("/admin" + LogAdminResource.URL, LogAdminResource.PAGE_NAME),

		SharedHtmlUtils.link("/admin" + MemcacheAdminResource.URL, MemcacheAdminResource.PAGE_NAME)

		)));
		AppConstants.endPage(w);
	}

}
