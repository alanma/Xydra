package org.xydra.gaemyadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.utils.Page.Form;
import org.xydra.restless.utils.Page.METHOD;
import org.xydra.restless.utils.Page.UnsortedList;
import org.xydra.restless.utils.ServletUtils;

/**
 * Configure logging settings via browser
 *
 * @author xamde
 */
public class LogAdminResource {

	public static final String PAGE_NAME = "Logging Configuration";
	static String URL;

	public static void restless(final Restless restless, final String prefix) {
		URL = prefix + "/logs";
		restless.addMethod(URL, "GET", LogAdminResource.class, "index", true);
		restless.addMethod(URL, "POST", LogAdminResource.class, "post", true);
	}

	public void index(final HttpServletResponse res) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		final Writer w = AppConstants.startPage(res, PAGE_NAME, "");

		final Form form = new Form(null, METHOD.POST, "/admin" + URL);
		final UnsortedList ul = form.unsortedList();

		final LogManager lm = LogManager.getLogManager();
		final Enumeration<String> enu = lm.getLoggerNames();
		while (enu.hasMoreElements()) {
			final String loggerName = enu.nextElement();
			final java.util.logging.Logger logger = lm.getLogger(loggerName);
			if (logger != null) {
				final Level currentLevel = logger.getLevel();
				ul.li().inputText("Logger '" + loggerName + "'", "logger-" + loggerName,
						currentLevel == null ? "" : currentLevel.getName());
			} else {
				ul.li("'" + loggerName + "'");
			}
		}
		ul.li().inputSubmit("Set");

		w.write(form.toHtml("  "));

		AppConstants.endPage(w);
	}

	public void post(final HttpServletRequest req, final HttpServletResponse res) throws IOException {
		GaeMyAdmin_GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// set each logger to the given level, if different from before

		// TODO persist settings in datastore and load from there

		final Map<String, String> params = ServletUtils.getRequestparametersAsMap(req);
		final LogManager lm = LogManager.getLogManager();
		for (final Map.Entry<String, String> param : params.entrySet()) {
			final String key = param.getKey();
			final String loggerName = key.substring("logger-".length());
			final Logger logger = lm.getLogger(loggerName);
			if (logger == null) {
				throw new IllegalArgumentException("Logger '" + loggerName + "' was null");
			}
			final Level current = logger.getLevel();
			Level planned = null;
			try {
				planned = Level.parse(param.getValue());
				if (current == null || !current.equals(planned)) {
					logger.setLevel(planned);
				}
			} catch (final IllegalArgumentException e) {
			}
		}

		// redirect to self
		res.sendRedirect("/admin" + URL);
	}

}
