package org.xydra.restless.gaedemo;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;

public class TimeResource {

	public static void restless(Restless r) {
		r.addGet("/servertime", TimeResource.class, "currentTimeOnServer");
		r.addGet("/browsertime", TimeResource.class, "getTimeFromBrowser", new RestlessParameter(
				"millisAtCallTime", null));
	}

	public String currentTimeOnServer() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat();
		return "Server time is now " + sdf.format(d);
	}

	public void getTimeFromBrowser(HttpServletResponse res, String millisAtCallTime)
			throws IOException {
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("It is now: \r\n"
				+ "<script type=\"text/javascript\">\r\n"
				+ "<!--\r\n"
				+ "var d = new Date();\r\n"
				+ "var curr_hour = d.getHours();\r\n"
				+ "var curr_min = d.getMinutes();\r\n"
				+ "var curr_s = d.getSeconds();\r\n"
				+ "document.write(curr_hour + \":\" + curr_min + \":\"+ curr_s+' = '+d.getTime()+'in unix time');\r\n"
				+ "//-->\r\n" + "</script>\r\n<br>");
		if (millisAtCallTime != null) {
			// calculate difference
			w.write("Called at " + millisAtCallTime + " ms (UNIX time)<br>");
			w.write("Milliseconds elapsed since then: <b>"
					+ "<script type=\"text/javascript\">\r\n" + "<!--\r\n"
					+ "document.write(new Date().getTime() - " + millisAtCallTime + ");\r\n"
					+ "//-->\r\n" + "</script></b>\r\n"
					+ "<br>Reloading this page delivers wrong results. Go back and click again.");
		}
		w.flush();
	}

}
