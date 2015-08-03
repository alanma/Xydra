package org.xydra.server.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.XydraPersistence;
import org.xydra.restless.Restless;
import org.xydra.restless.utils.Delay;
import org.xydra.server.XydraServerApp;
import org.xydra.server.rest.log.LogTestResource;
import org.xydra.store.InternalStoreException;
import org.xydra.store.XydraStore;

/**
 * A REST-server exposing a {@link XydraStore} over HTTP.
 *
 * @author xamde
 * @author dscharrer
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XydraRestServer {

	private static final Logger log = LoggerFactory.getLogger(XydraRestServer.class);

	/**
	 * Web.xml param. Mandatory. Which {@link XydraStore} to use.
	 */
	public static final String INIT_PARAM_XYDRASTORE = "org.xydra.store";

	/**
	 * Web.xml param. Optional. Which XydraServerApp to use.
	 */
	public static final String INIT_PARAM_SERVER_APP = "org.xydra.server.app";

	/**
	 * Web.xml param. Optional. 0 or not set: No delay. Other values: Delay in
	 * milliseconds.
	 */
	public static final String INIT_PARAM_DELAY = "org.xydra.server.util.delay";

	/**
	 * The defined key used for storing a reference to a {@link XydraStore} in
	 * the servlet context.
	 */
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE = "org.xydra.store";

	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVERAPP = "org.xydra.server.app";

	/**
	 * The defined key for storing a reference to a {@link XydraPersistence} in
	 * the servlet context.
	 */
	// TODO use conf instead OR XydraRuntime
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE = "org.xydra.persistence";

	/**
	 * @param restless
	 * @return a {@link XydraStore} from servlet context
	 * @throws InternalStoreException
	 *             if no XydraStore is found in servlet context
	 */
	public static XydraStore getStore(final Restless restless) {
		final XydraStore store = getXydraStoreInternal(restless);
		if (store == null) {
			throw new InternalStoreException(
					"XydraRestServer not initialized - store not found in servlet context");
		}
		return store;
	}

	/**
	 * Use the servlet context to exchange parameters among different Restless
	 * apps booting/running in the same servlet.
	 *
	 * @param restless
	 * @return a {@link XydraStore} from servlet context
	 */
	private static XydraStore getXydraStoreInternal(final Restless restless) {
		return (XydraStore) restless.getServletContext().getAttribute(
				SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE);
	}

	/**
	 * Use the servlet context to exchange parameters among different Restless
	 * apps booting/running in the same servlet.
	 *
	 * @param restless
	 * @param store
	 *            too be retrieved via {@link #getStore(Restless)}
	 */
	public static void setXydraStoreInServletContext(final Restless restless, final XydraStore store) {
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE, store);
	}

	public static void setXydraServerAppInServletContext(final Restless restless, final XydraServerApp serverApp) {
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVERAPP,
				serverApp);
	}

	/**
	 * @param restless
	 * @return a {@link XydraServerApp} (assuming init ran first and one was
	 *         configured in web.xml)
	 */
	public static XydraServerApp getXydraServerApp(final Restless restless) {
		return (XydraServerApp) restless.getServletContext().getAttribute(
				SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVERAPP);
	}

	/**
	 * Setup the Xydra REST API at '/store/v1'
	 *
	 * @param restless
	 * @param prefix
	 */
	public static void restless(final Restless restless, final String prefix) {
		log.info("Booting XydraRestServer");
		xydraV1(restless, prefix);
	}

	private static void xydraV1(final Restless restless, final String prefix) {
		// configure
		initializeServer(restless);

		restless.addExceptionHandler(new XAccessExceptionHandler());

		// init store resources, delegate all traffic to XydraStoreResource
		final String storePrefix = prefix + "/store/v1";
		XydraStoreResource.restless(restless, storePrefix);

		// '/ping' - for debugging purposes
		restless.addMethod(prefix + "/ping", "GET", XydraRestServer.class, "ping", false);

		// AddDemoDataResource.restless(restless, prefix);

		// '/logtest' - for debugging purposes
		LogTestResource.restless(restless, prefix);
	}

	/**
	 * Instantiate a XydraStore via reflection as configured in web.xml
	 *
	 * @param restless
	 *            never null
	 * @throws RuntimeException
	 *             if instantiation goes wrong
	 */
	public static synchronized void initializeServer(final Restless restless) throws RuntimeException {
		// initialize only once
		if (getXydraStoreInternal(restless) != null) {
			// server already initialized
			return;
		}

		final String storeClassName = restless.getInitParameter(INIT_PARAM_XYDRASTORE);
		XydraStore storeInstance;
		if (storeClassName == null) {
			throw new RuntimeException("no xydra store backend configured in web.xml. Set param '"
					+ INIT_PARAM_XYDRASTORE + "' to the classname of a XydraStore impl.");
		}

		try {
			final Class<?> storeClass = Class.forName(storeClassName);
			final Constructor<?> cons = storeClass.getConstructor();
			if (!XydraStore.class.isAssignableFrom(storeClass)) {
				throw new RuntimeException(storeClass.getClass() + " is not a XydraStore");
			}

			storeInstance = (XydraStore) cons.newInstance();
			// store in context
			setXydraStoreInServletContext(restless, storeInstance);
			log.info("XydraStore instance stored in servletContext at key '"
					+ SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE + "'");
		} catch (final InvocationTargetException e) {
			log.error("Could no start XydraStore: '" + storeClassName + "'", e);
		} catch (final InstantiationException e) {
			log.error("Could no start XydraStore: '" + storeClassName + "'", e);
		} catch (final IllegalAccessException e) {
			log.error("Could no start XydraStore due to IllegalAccessException", e);
		} catch (final NoSuchMethodException e) {
			log.error("Could no start XydraStore: '" + storeClassName
					+ "' seems to be a broken implementation", e);
		} catch (final ClassNotFoundException e) {
			log.error("XydraStore is malconfigured: '" + storeClassName + "'", e);
		}

		/* Configure ServerApp, if one is defined */
		final String serverAppClassName = restless.getInitParameter(INIT_PARAM_SERVER_APP);
		XydraServerApp serverAppInstance;
		if (serverAppClassName != null) {
			try {
				final Class<?> storeClass = Class.forName(serverAppClassName);
				final Constructor<?> cons = storeClass.getConstructor();
				if (!XydraServerApp.class.isAssignableFrom(storeClass)) {
					throw new RuntimeException(storeClass.getClass() + " is not a XydraServerApp");
				}

				serverAppInstance = (XydraServerApp) cons.newInstance();
				// store in context
				setXydraServerAppInServletContext(restless, serverAppInstance);
				log.info("XydraServerApp instance stored in servletContext at key '"
						+ SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVERAPP + "'");
			} catch (final InvocationTargetException e) {
				log.error("Could no start XydraServerApp: '" + serverAppClassName + "'", e);
			} catch (final InstantiationException e) {
				log.error("Could no start XydraServerApp: '" + serverAppClassName + "'", e);
			} catch (final IllegalAccessException e) {
				log.error("Could no start XydraServerApp due to IllegalAccessException", e);
			} catch (final NoSuchMethodException e) {
				log.error("Could no start XydraServerApp: '" + serverAppClassName
						+ "' seems to be a broken implementation", e);
			} catch (final ClassNotFoundException e) {
				log.error("XydraServerApp is malconfigured: '" + serverAppClassName + "'", e);
			}
		} else {
			log.debug("no XydraServerApp configured in web.xml. Set <" + INIT_PARAM_SERVER_APP
					+ "> to the classname of a XydraStore impl.");
		}

		/* Configure simulated delay */
		final String simulateDelay = restless.getInitParameter(INIT_PARAM_DELAY);
		if (simulateDelay == null || simulateDelay.equals(new String("false"))) {
			Delay.setAjaxDelayMs(0);
		} else {
			final int msDelay = Integer.parseInt(simulateDelay);
			Delay.setAjaxDelayMs(msDelay);
		}

	}

	/**
	 * Returns server time as text/plain
	 *
	 * @param res
	 * @throws IOException
	 */
	public static void ping(final HttpServletResponse res) throws IOException {
		res.setStatus(200);
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		final Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Running. Server time = " + System.currentTimeMillis());
		w.flush();
		w.close();
	}

	public static void textResponse(final HttpServletResponse res, final int statusCode, final String xml) {
		response(res, "text/plain; charset=UTF-8", statusCode, xml);
	}

	public static void response(final HttpServletResponse res, final String contentType, final int statusCode,
			final String xml) {

		res.setContentType(contentType);
		res.setStatus(statusCode);
		try {
			new OutputStreamWriter(res.getOutputStream(), "utf-8").write(xml);
		} catch (final IOException ioe) {
			throw new RuntimeException("IOException while sending response", ioe);
		}

	}

	public static String readPostData(final HttpServletRequest req) {

		try {

			final Reader in = req.getReader();
			final char[] buffer = new char[0x10000];
			final StringBuilder out = new StringBuilder();
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if (read > 0) {
					out.append(buffer, 0, read);
				}
			} while (read >= 0);

			return out.toString();

		} catch (final IOException ioe) {
			throw new RuntimeException("IOException while reading POSTed data", ioe);
		}

	}

}
