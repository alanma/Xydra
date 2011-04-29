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


public class MemcacheResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix + "/", "GET", MemcacheResource.class, "index", true);
		restless.addMethod(prefix + "/stats", "GET", MemcacheResource.class, "stats", true);
	}
	
	public void index(HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "GaeMyAdmin :: Memcache");
		w.write(HtmlUtils.toOrderedList(Arrays.asList(

		HtmlUtils.link("stats/", "Memcache Statistics")

		)));
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
	}
	
	public void stats(HttpServletResponse res) throws IOException {
		Writer w = HtmlUtils.startHtmlPage(res, "GaeMyAdmin :: Memcache stats");
		w.write("Loading stats...");
		w.flush();
		
		// Get a handle on the service itself
		MemcacheService mcs = MemcacheServiceFactory.getMemcacheService();
		w.write("<h2>Namespace: " + mcs.getNamespace() + "</h2>");
		
		Stats stats = mcs.getStatistics();
		
		if(stats == null) {
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
		w.flush();
		HtmlUtils.writeCloseBodyHtml(w);
	}
}
