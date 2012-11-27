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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.server.rest.log.LogTestResource;
import org.xydra.server.util.Delay;
import org.xydra.store.InternalStoreException;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A REST-server exposing a {@link XydraStore} over HTTP.
 * 
 * 
 * 
 * 
 * @author voelkel
 * @author dscharrer
 * 
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
	 * Web.xml param. Optional. 0 or not set: No delay. Other values: Delay in
	 * milliseconds.
	 */
	public static final String INIT_PARAM_DELAY = "org.xydra.server.util.delay";
	
	/**
	 * The defined key used for storing a reference to a {@link XydraStore} in
	 * the servlet context.
	 */
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE = "org.xydra.store";
	
	/**
	 * The defined key for storing a reference to a {@link XydraPersistence} in
	 * the servlet context.
	 */
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRA_PERSISTENCE = "org.xydra.persistence";
	
	/**
	 * @param restless
	 * @return a {@link XydraStore} from servlet context
	 * @throws InternalStoreException if no XydraStore is found in servlet
	 *             context
	 */
	public static XydraStore getStore(Restless restless) {
		XydraStore store = getXydraStoreInternal(restless);
		if(store == null) {
			throw new InternalStoreException(
			        "XydraRestServer not initialized - store not found in servlet context");
		}
		return store;
	}
	
	/**
	 * @param restless
	 * @return a {@link XydraStore} from servlet context
	 */
	private static XydraStore getXydraStoreInternal(Restless restless) {
		return (XydraStore)restless.getServletContext().getAttribute(
		        SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE);
	}
	
	/**
	 * @param restless
	 * @param store too be retrieved via {@link #getStore(Restless)}
	 */
	public static void setXydraStoreInServletContext(Restless restless, XydraStore store) {
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE, store);
	}
	
	/**
	 * Setup the Xydra REST API at '/store/v1'
	 * 
	 * @param restless
	 * @param prefix
	 */
	public static void restless(Restless restless, String prefix) {
		log.info("Booting XydraRestServer");
		
		// configure
		initializeServer(restless);
		
		restless.addExceptionHandler(new XAccessExceptionHandler());
		
		// init store resources, delegate all traffic to XydraStoreResource
		String storePrefix = prefix + "/store/v1";
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
	 * @param restless never null
	 * @throws RuntimeException if instantiation goes wrong
	 */
	public static synchronized void initializeServer(Restless restless) throws RuntimeException {
		// initialize only once
		if(getXydraStoreInternal(restless) != null) {
			// server already initialized
			return;
		}
		
		String storeClassName = restless.getInitParameter(INIT_PARAM_XYDRASTORE);
		XydraStore storeInstance;
		if(storeClassName != null) {
			try {
				Class<?> storeClass = Class.forName(storeClassName);
				Constructor<?> cons = storeClass.getConstructor();
				if(!XydraStore.class.isAssignableFrom(storeClass)) {
					throw new RuntimeException(storeClass.getClass() + " is not a XydraStore");
				}
				
				storeInstance = (XydraStore)cons.newInstance();
				// store in context
				setXydraStoreInServletContext(restless, storeInstance);
				log.info("XydraStore instance stored in servletContext at key '"
				        + SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE + "'");
			} catch(InvocationTargetException e) {
				log.error("Could no start XydraStore: '" + storeClassName + "'", e);
			} catch(InstantiationException e) {
				log.error("Could no start XydraStore: '" + storeClassName + "'", e);
			} catch(IllegalAccessException e) {
				log.error("Could no start XydraStore due to IllegalAccessException", e);
			} catch(NoSuchMethodException e) {
				log.error("Could no start XydraStore: '" + storeClassName
				        + "' seems to be a broken implementation", e);
			} catch(ClassNotFoundException e) {
				log.error("XydraStore is malconfigured: '" + storeClassName + "'", e);
			}
		} else {
			throw new RuntimeException("no xydra store backend configured in web.xml. Set <"
			        + INIT_PARAM_XYDRASTORE + "> to the classname of a XydraStore impl.");
		}
		
		/* Configure simulated delay */
		String simulateDelay = restless.getInitParameter(INIT_PARAM_DELAY);
		if(simulateDelay == null || simulateDelay.equals(new String("false"))) {
			Delay.setAjaxDelayMs(0);
		} else {
			int msDelay = Integer.parseInt(simulateDelay);
			Delay.setAjaxDelayMs(msDelay);
		}
		
	}
	
	/**
	 * Returns server time as text/plain
	 * 
	 * @param res
	 * @throws IOException
	 */
	public static void ping(HttpServletResponse res) throws IOException {
		res.setStatus(200);
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Running. Server time = " + System.currentTimeMillis());
		w.flush();
		w.close();
	}
	
	public static void textResponse(HttpServletResponse res, int statusCode, String xml) {
		response(res, "text/plain; charset=UTF-8", statusCode, xml);
	}
	
	public static void response(HttpServletResponse res, String contentType, int statusCode,
	        String xml) {
		
		res.setContentType(contentType);
		res.setStatus(statusCode);
		try {
			new OutputStreamWriter(res.getOutputStream(), "utf-8").write(xml);
		} catch(IOException ioe) {
			throw new RuntimeException("IOException while sending response", ioe);
		}
		
	}
	
	public static String readPostData(HttpServletRequest req) {
		
		try {
			
			Reader in = req.getReader();
			final char[] buffer = new char[0x10000];
			StringBuilder out = new StringBuilder();
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if(read > 0) {
					out.append(buffer, 0, read);
				}
			} while(read >= 0);
			
			return out.toString();
			
		} catch(IOException ioe) {
			throw new RuntimeException("IOException while reading POSTed data", ioe);
		}
		
	}
	
}
