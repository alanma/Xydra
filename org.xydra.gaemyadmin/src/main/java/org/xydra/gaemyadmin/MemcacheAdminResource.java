package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;

public class MemcacheAdminResource {

	static final String PAGE_NAME = "Memcache Admin";
	static String URL;

	public static void restless(Restless restless, String prefix) {
		URL = prefix + "/memcache";
		restless.addMethod(URL, "GET", MemcacheAdminResource.class, "index", true);
		restless.addMethod(URL + "/stats", "GET", MemcacheAdminResource.class, "stats", true);
		restless.addMethod(URL + "/clear", "GET", MemcacheAdminResource.class, "clear", true);
	}

	public void index(HttpServletResponse res) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "");

		w.write(

		HtmlUtils.toOrderedList(

		Arrays.asList(

		HtmlUtils.link("/admin" + URL + "/stats", "Memcache Statistics"),

		HtmlUtils.link("/admin" + URL + "/clear", "Clear Memcache")

		)));
		AppConstants.endPage(w);

	}

	public void stats(HttpServletResponse res) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Stats");

		w.write("Loading stats...");
		w.flush();

		MemcacheService mcs = MemcacheServiceFactory.getMemcacheService();
		w.write("<h2>Namespace: " + mcs.getNamespace() + "</h2>");

		Stats stats = mcs.getStatistics();

		if (stats == null) {
			w.write("No stats available.");
		} else {
			w.write(HtmlUtils.toOrderedList(Arrays.asList(

			"BytesReturnedForHits: " + stats.getBytesReturnedForHits(),

			"HitCount: " + stats.getHitCount(),

			"ItemCount: " + stats.getItemCount(),

			"MaxTimeWithoutAccess: " + stats.getMaxTimeWithoutAccess(),

			"MissCount: " + stats.getMissCount(),

			"TotalItemBytes: " + stats.getTotalItemBytes()

			)));
		}
		AppConstants.endPage(w);

	}

	public void clear(HttpServletResponse res) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w = AppConstants.startPage(res, PAGE_NAME, "Clear");

		w.write("Clearing memcache ...");
		w.flush();

		MemcacheService mcs = MemcacheServiceFactory.getMemcacheService();
		mcs.clearAll();

		w.write("Done.");
		AppConstants.endPage(w);
	}
}
