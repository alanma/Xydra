package org.xydra.testgae.server.rest.xmas;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.common.NanoClock;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.testgae.server.model.xmas.Xmas;
import org.xydra.xgae.gaeutils.GaeTestfixer;

/**
 * Main resource for the Xmas sample application
 *
 * @author xamde
 */
public class XmasResource {

	private static final Logger log = LoggerFactory.getLogger(XmasResource.class);

	public static void restless(final Restless r, final String path) {
		r.addGet(path + "/xmas/{repo}/add", XmasResource.class, "addData", // .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("lists", "1"),// .
				new RestlessParameter("wishes", "1")// .
		);
		r.addGet(path + "/xmas/{repo}/clear", XmasResource.class, "clearRepository", // .
				new RestlessParameter("repo", null)// .
		);
		r.addGet(path + "/xmas/{repo}", XmasResource.class, "get",// .
				new RestlessParameter("repo", null),// .
				new RestlessParameter("view", "collapsed"),// .
				new RestlessParameter("format", "html")// .
		);
		// expose wish lists
		WishlistResource.restless(r, path + "/xmas");
	}

	public void addData(final String repoStr, final String listsStr, final String wishesStr, final HttpServletRequest req,
			final HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");
		final int listCount = Integer.parseInt(listsStr);
		final int wishesCount = Integer.parseInt(wishesStr);
		final Writer w = HtmlUtils.startHtmlPage(res, "Xmas | Adding test data");
		w.write("Adding test data ...?lists=" + listsStr + "&wishes=" + wishesStr
				+ " wishes.<br />");
		Xmas.addData(repoStr, listCount, wishesCount, w);
		w.write("<a href='.'>See all wish lists in repository '" + repoStr + "'</a>");
		HtmlUtils.endHtmlPage(w);
	}

	public void clearRepository(final String repoStr, final HttpServletRequest req, final HttpServletResponse res)
			throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		ServletUtils.headers(res, "text/html");

		final Writer w = HtmlUtils.startHtmlPage(res, "Xmas | Clearing repository");
		w.write("Deleting all wishlists from repository '" + repoStr + "'.<br />");
		Xmas.clearRepository(repoStr);
		w.write("<a href='.'>Go back to repository '" + repoStr + "'</a>");
		HtmlUtils.endHtmlPage(w);
	}

	/**
	 * @param repoStr
	 *            ..
	 * @param view
	 *            "expanded" or "collapsed" (default)
	 * @param format
	 *            "html" (default) or "urls"
	 * @param res
	 *            ..
	 * @throws IOException
	 *             ...
	 */
	public void get(final String repoStr, final String view, final String format, final HttpServletResponse res)
			throws IOException {
		final NanoClock c = new NanoClock().start();
		log.info("Getting " + repoStr + "?view=" + view + "&format=" + format);
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		if (format.equals("urls")) {
			ServletUtils.headers(res, "text/plain");
			final XWritableRepository repo = Xmas.getRepository(repoStr);
			final Writer w = res.getWriter();
			for (final XId modelId : repo) {
				final String url = WishlistResource.toRelativeUrl(repoStr, modelId);
				w.write(url + "\n");
			}
			// always write at least a single blank line
			w.write("\n");
			w.flush();
			w.close();
		} else {
			ServletUtils.headers(res, "text/html");
			final Writer w = HtmlUtils.startHtmlPage(res, "Xmas | Adding test data");

			Xmas.get(repoStr, view, w);
			w.write(SharedHtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/add")
					.withInputText("lists", "1").withInputText("wishes", "1")
					.withInputSubmit("Add lists with wishes").toString());
			w.write(SharedHtmlUtils.form(METHOD.GET, "/xmas/" + repoStr + "/clear")
					.withInputSubmit("Delete all lists").toString());
			HtmlUtils.endHtmlPage(w);
		}
		c.stop("get");
		log.info("Done " + repoStr + "?view=" + view + "&format=" + format + " at "
				+ System.currentTimeMillis() + " " + c.getStats());
	}

}
