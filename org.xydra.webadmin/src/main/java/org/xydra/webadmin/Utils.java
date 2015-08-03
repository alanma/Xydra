package org.xydra.webadmin;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XId;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.HeadLinkStyle;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.xgae.gaeutils.AboutAppEngine;

public class Utils {

	private static Map<XId, XydraPersistence> pmap = new HashMap<XId, XydraPersistence>();

	public static synchronized XydraPersistence createPersistence(final XId repoId) {
		XydraPersistence p = pmap.get(repoId);
		if (p == null) {
			p = new GaePersistence(repoId);
			pmap.put(repoId, p);
		}
		return p;
	}

	/**
	 * Suggest an appropriate filename suffixed with the current time.
	 *
	 * @param name
	 *            ..
	 * @return name + '-yyyy-MM-dd-HH-mm-ss' (as of now)
	 */
	public static String filenameOfNow(final String name) {
		final Date now = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return name + "-" + sdf.format(now);
	}

	public static Writer startPage(final HttpServletResponse res, final String page, final String part)
			throws IOException {
		final Writer w = HtmlUtils.startHtmlPage(res, AppConstants.NAME + ":: " + page + " " + part,
				new HeadLinkStyle(AppConstants.CSS_DEBUG_PATH));
		w.write("<div class='xydebug' >");
		w.write(SharedHtmlUtils.link(".", "Back"));
		w.write("Instance: " + AboutAppEngine.getInstanceId() + " Thread: "
				+ AboutAppEngine.getThreadInfo() + "<br/>\n");
		w.write("<h3>" + page + " " + part + "</h3>\n");
		return w;
	}

	public static void endPage(final Writer w) throws IOException {
		w.write("</div>");
		HtmlUtils.endHtmlPage(w);
	}

}
