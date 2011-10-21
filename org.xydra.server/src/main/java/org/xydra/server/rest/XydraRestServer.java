package org.xydra.server.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.server.rest.demo.AddDemoDataResource;
import org.xydra.server.rest.log.LogTestResource;
import org.xydra.store.InternalStoreException;
import org.xydra.store.XydraStore;


/**
 * A REST-server exposing a {@link XydraStore} over HTTP.
 * 
 * @author voelkel
 * @author dscharrer
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XydraRestServer {
	
	private static final Logger log = LoggerFactory.getLogger(XydraRestServer.class);
	
	public static final String INIT_PARAM_XYDRASTORE = "org.xydra.store";
	
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE = "org.xydra.store";
	
	public static XydraStore getStore(Restless restless) {
		XydraStore store = getXydraStoreInternal(restless);
		if(store == null) {
			throw new InternalStoreException("XydraRestSever not initialized");
		}
		return store;
	}
	
	private static XydraStore getXydraStoreInternal(Restless restless) {
		return (XydraStore)restless.getServletContext().getAttribute(
		        SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE);
	}
	
	/**
	 * Setup the Xydra REST API.
	 */
	public static void restless(Restless restless, String prefix) {
		
		// configure
		initializeServer(restless);
		
		restless.addExceptionHandler(new XAccessExceptionHandler());
		
		// init store resources
		String storePrefix = prefix + "/store/v1";
		XydraStoreResource.restless(restless, storePrefix);
		
		// for debugging purposes
		restless.addMethod(prefix + "/ping", "GET", XydraRestServer.class, "ping", false);
		AddDemoDataResource.restless(restless, prefix);
		LogTestResource.restless(restless, prefix);
	}
	
	public static synchronized void initializeServer(Restless restless) {
		
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
			} catch(Exception e) {
				throw new RuntimeException("Error configuring XydraStore from class '"
				        + storeClassName + "'", e);
			}
		} else {
			throw new RuntimeException("no xydra store backend configured in web.xml. Set <"
			        + INIT_PARAM_XYDRASTORE + "> to the classname of a XydraStore impl.");
		}
		
		// store in context
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE,
		        storeInstance);
		log.info("XydraStore instance stored in servletContext at key '"
		        + SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE + "'");
	}
	
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
