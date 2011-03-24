package org.xydra.testgae.xmas.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.HtmlUtils.METHOD;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.testgae.xmas.data.Benchmark;
import org.xydra.testgae.xmas.data.Xmas;


public class XmasResource {
	
	public static void restless(Restless r, String path) {
		GaeConfigurator.restless(r, path + "/xmas");
		Benchmark.restless(r, path + "/xmas");
		
		r.addGet(path + "/xmas/{repo}/add", XmasResource.class, "addData",

		new RestlessParameter("repo", null),

		new RestlessParameter("lists", "1"),

		new RestlessParameter("wishes", "1")

		);
		
		r.addGet(path + "/xmas/{repo}", XmasResource.class, "get",

		new RestlessParameter("repo", null),

		new RestlessParameter("view", "collapsed")

		);
		
		WishlistResource.restless(r, path + "/xmas");
	}
	
	public void addData(String repoStr, String listsStr, String wishesStr, HttpServletRequest req,
	        HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		int listCount = Integer.parseInt(listsStr);
		int wishesCount = Integer.parseInt(wishesStr);
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Adding test data ...?lists=" + listsStr + "&wishes=" + wishesStr
		        + " wishes.<br />");
		Xmas.addData(repoStr, listCount, wishesCount, new OutputStreamWriter(res.getOutputStream(),
		        "utf-8"));
		w.write("<a href='.'>See all wish lists in repository '" + repoStr + "'</a>");
	}
	
	public void get(String repoStr, String view, HttpServletResponse res) throws IOException {
		ServletUtils.headers(res, "text/html");
		Xmas.get(repoStr, view, new OutputStreamWriter(res.getOutputStream(), "utf-8"));
		
		new OutputStreamWriter(res.getOutputStream(), "utf-8").write(HtmlUtils
		        .form(METHOD.GET, "/xmas/" + repoStr + "/add").withInputText("lists", "1")
		        .withInputText("wishes", "1").withInputSubmit("Add lists with wishes").toString());
	}
	
}
