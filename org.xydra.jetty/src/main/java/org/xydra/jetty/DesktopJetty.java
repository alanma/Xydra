package org.xydra.jetty;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class DesktopJetty extends EmbeddedJetty {

	private static Logger log = LoggerFactory.getLogger(DesktopJetty.class);

	private final Filter noCachingFilter = JettyUtils.createNoCacheFilter();

	@Override
	protected void configureWebapp(final WebAppContext webapp) {
		log.info("Configuring DesktopJetty");

		// TODO really required?
		// ClassLoader classloader =
		// Thread.currentThread().getContextClassLoader();
		// webapp.setClassLoader(classloader);

		/* caching for desktop jetty? don't cache anything until we know better */
		final FilterHolder cacheNothingFilterHolder = new FilterHolder();
		cacheNothingFilterHolder.setFilter(this.noCachingFilter);
		webapp.addFilter(cacheNothingFilterHolder, "*.*", EnumSet.allOf(DispatcherType.class));

		// dont cache marked files
		final FilterHolder noCacheFilterHolder = new FilterHolder();
		noCacheFilterHolder.setFilter(this.noCachingFilter);
		webapp.addFilter(noCacheFilterHolder, "*.nocache.*",
				EnumSet.allOf(DispatcherType.class));


		// IMPROVE move?
		final MimeTypes mimeTypes = new MimeTypes();

		mimeTypes.addMimeMapping("html", "text/html");
		mimeTypes.addMimeMapping("ico", "image/x-icon");

		// For serving SVG fonts -->
		mimeTypes.addMimeMapping("svg", "image/svg+xml");
		mimeTypes.addMimeMapping("woff", "application/x-font-woff");
		mimeTypes.addMimeMapping("eot", "application/vnd.ms-fontobject");
		mimeTypes.addMimeMapping("otf", "font/opentype");

		// prevent weird appengine bug -->
		mimeTypes.addMimeMapping("css", "text/css");

		// for Mozilla store -->
		mimeTypes.addMimeMapping("webapp", "application/x-web-app-manifest+json");

		// for AppCache (offline mode), see http://appcachefacts.info/ -->
		mimeTypes.addMimeMapping("appcache", "text/cache-manifest");
		mimeTypes.addMimeMapping("manifest", "text/cache-manifest");

		webapp.setMimeTypes(mimeTypes);

		// TODO have an admin user for restless debug tools?
		/*
		 * Add simple security handler that puts anybody with the name 'admin'
		 * into the admin role.
		 */
		webapp.getSecurityHandler().setLoginService(JettyUtils.createInsecureTestLoginService());
		// jetty6
		// webapp.getSecurityHandler().setUserRealm(JettyUtils.createInsecureTestUserRealm());
	}
}
