package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;


public class MemcacheResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix + "/", "GET", MemcacheResource.class, "index", true);
		restless.addMethod(prefix + "/stats", "GET", MemcacheResource.class, "stats", true);
	}
	
	public void index(HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		HtmlUtils.writeHtmlHeaderOpenBody(w, "GaeMyAdmin :: Memcache");
		w.write(HtmlUtils.toOrderedList(Arrays.asList(

		HtmlUtils.link("stats", "Memcache Statistics")

		)));
		HtmlUtils.writeCloseBodyHtml(w);
		w.flush();
	}
	
	public void stats(HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		HtmlUtils.writeHtmlHeaderOpenBody(w, "GaeMyAdmin :: Memcache stats");
		
		w.write("Loading stats...");
		w.flush();
		
		// Get a handle on the service itself
		MemcacheService mcs = MemcacheServiceFactory.getMemcacheService();
		w.write("<h2>Namespace: " + mcs.getNamespace() + "</h2>");
		
		Stats stats = mcs.getStatistics();
		
		w.write(HtmlUtils.toOrderedList(Arrays.asList(

		"BytesReturnedForHits: " + stats.getBytesReturnedForHits(),

		"HitCount: " + stats.getHitCount(),

		"ItemCount: " + stats.getItemCount(),

		"MaxTimeWithoutAccess: " + stats.getMaxTimeWithoutAccess(),

		"MissCount: " + stats.getMissCount(),

		"TotalItemBytes: " + stats.getTotalItemBytes()

		)));
		w.flush();
		HtmlUtils.writeCloseBodyHtml(w);
	}
}
