package org.xydra.server.rest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.IXydraServer;
import org.xydra.server.IXydraSession;
import org.xydra.server.impl.memory.ArmXydraSession;
import org.xydra.server.rest.changes.XRepositoryChangesResource;
import org.xydra.server.rest.changes.XSynchronizeChangesResource;
import org.xydra.server.rest.data.XFieldResource;
import org.xydra.server.rest.data.XModelResource;
import org.xydra.server.rest.data.XObjectResource;
import org.xydra.server.rest.data.XRepositoryResource;
import org.xydra.server.rest.demo.AddDemoDataResource;
import org.xydra.server.rest.log.LogTestResource;
import org.xydra.store.InternalStoreException;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A REST-server exposing a {@link IXydraServer} over HTTP. The
 * {@link IXydraServer} should have been set statically before accessing REST
 * functions.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class XydraRestServer {
	
	private static final Logger log = LoggerFactory.getLogger(XydraRestServer.class);
	
	public static final String INIT_PARAM_XYDRASERVER = "org.xydra.server";
	public static final String INIT_PARAM_XYDRASTORE = "org.xydra.store";
	
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER = "org.xydra.server";
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE = "org.xydra.store";
	
	/**
	 * @param restless The current Restless instance.
	 * @return an instance of {@link IXydraServer} from the current servlet
	 *         context that has hopefully been put there by a previous call of
	 *         restless().
	 */
	public static IXydraServer getXydraServer(Restless restless) {
		IXydraServer xydraServer = getXydraServerInternal(restless);
		if(xydraServer == null) {
			log
			        .warn("XydraRestServer.restless hasn't been run properly before calling this method.");
		}
		return xydraServer;
	}
	
	private static IXydraServer getXydraServerInternal(Restless restless) {
		IXydraServer xydraServer = (IXydraServer)restless.getServletContext().getAttribute(
		        SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER);
		return xydraServer;
	}
	
	public static XydraStore getXydraStore(Restless restless) {
		XydraStore store = (XydraStore)restless.getServletContext().getAttribute(
		        SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE);
		if(store == null) {
			throw new InternalStoreException("XydraRestSever not initialized");
		}
		return store;
	}
	
	/**
	 * Setup the Xydra REST API.
	 * 
	 * Make sure to have configured a server first in
	 * {@link XydraServerDefaultConfiguration}.
	 */
	public void restless(Restless restless, String prefix) {
		
		// configure
		initializeServer(restless);
		
		restless.addExceptionHandler(new XAccessExceptionHandler());
		
		// init store resources
		String storePrefix = prefix + "/store/v1";
		XydraStoreResource.restless(restless, storePrefix);
		
		// init data/snapshot handling resources
		String dataPrefix = prefix + "/data";
		XRepositoryResource.restless(restless, dataPrefix);
		XModelResource.restless(restless, dataPrefix);
		XObjectResource.restless(restless, dataPrefix);
		XFieldResource.restless(restless, dataPrefix);
		
		// init change handling resources
		String changesPrefix = prefix + "/changes";
		XSynchronizeChangesResource.restless(restless, changesPrefix);
		XRepositoryChangesResource.restless(restless, changesPrefix);
		
		// for debugging purposes
		restless.addMethod(prefix + "/ping", "GET", this, "ping", false);
		AddDemoDataResource.restless(restless, prefix);
		LogTestResource.restless(restless, prefix);
	}
	
	public static synchronized void initializeServer(Restless restless) {
		
		if(getXydraServerInternal(restless) != null) {
			// server already initialized
			return;
		}
		
		String serverClassName = restless.getInitParameter(INIT_PARAM_XYDRASERVER);
		IXydraServer serverInstance;
		if(serverClassName != null) {
			Class<?> serverClass;
			try {
				serverClass = Class.forName(serverClassName);
				Constructor<?> cons = serverClass.getConstructor();
				assert IXydraServer.class.isAssignableFrom(serverClass) : serverClass.getClass()
				        + " is not a IXydraServer";
				serverInstance = (IXydraServer)cons.newInstance();
			} catch(ClassNotFoundException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(SecurityException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(NoSuchMethodException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(IllegalArgumentException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(InstantiationException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(IllegalAccessException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			} catch(InvocationTargetException e) {
				throw new RuntimeException("Error configuring XydraServer from class '"
				        + serverClassName + "'", e);
			}
		} else {
			throw new RuntimeException("no xydra server backend configured in web.xml");
		}
		
		// store in context
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER,
		        serverInstance);
		log.info("XydraServer instance stored in servletContext at key '"
		        + SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER + "'");
		
		String storeClassName = restless.getInitParameter(INIT_PARAM_XYDRASTORE);
		XydraStore storeInstance;
		if(storeClassName != null) {
			try {
				Class<?> storeClass = Class.forName(storeClassName);
				Constructor<?> cons = storeClass.getConstructor();
				assert XydraPersistence.class.isAssignableFrom(storeClass) : storeClass.getClass()
				        + " is not a XydraPersistence";
				storeInstance = (XydraStore)cons.newInstance();
			} catch(Exception e) {
				throw new RuntimeException("Error configuring XydraStore from persistence class '"
				        + storeClassName + "'", e);
			}
		} else {
			throw new RuntimeException("no xydra store backend configured in web.xml");
		}
		
		// store in context
		restless.getServletContext().setAttribute(SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE,
		        storeInstance);
		log.info("XydraStore instance stored in servletContext at key '"
		        + SERVLET_CONTEXT_ATTRIBUTE_XYDRASTORE + "'");
	}
	
	public void ping(HttpServletResponse res) throws IOException {
		res.setStatus(200);
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		Writer w = new OutputStreamWriter(res.getOutputStream(), "utf-8");
		w.write("Running. Server time = " + System.currentTimeMillis());
		w.flush();
		w.close();
	}
	
	public static XID getId(String idStr) {
		try {
			return XX.toId(idStr);
		} catch(Exception e) {
			throw new RestlessException(RestlessException.Bad_request, "invalid XID: " + idStr);
		}
	}
	
	public static void xmlResponse(HttpServletResponse res, int statusCode, String xml) {
		response(res, "application/xml; charset=UTF-8", statusCode, xml);
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
	
	public static Long getLongParameter(HttpServletRequest req, String name) {
		
		String valueStr = req.getParameter(name);
		
		if(valueStr == null) {
			return null;
		}
		
		try {
			return Long.decode(valueStr);
		} catch(NumberFormatException nfe) {
			throw new RestlessException(RestlessException.Bad_request, "parameter " + name
			        + " must be a long, was: " + valueStr);
		}
	}
	
	public static final String COOKIE_ACTOR = "actor";
	
	/**
	 * Get and authenticate the current user.
	 * 
	 * @param req The request
	 * @return The authenticated actor or null if no actor was specified.
	 * @throws RestlessException if an actor was specified but could not be
	 *             authenticated
	 */
	public static synchronized XID getActor(HttpServletRequest req) {
		
		Cookie[] cookies = req.getCookies();
		if(cookies == null) {
			return null;
		}
		
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals(COOKIE_ACTOR)) {
				
				try {
					// TODO authenticate
					return XX.toId(cookie.getValue());
				} catch(Exception e) {
					throw new RestlessException(RestlessException.Unauthorized,
					        "actor is invalid XID: " + cookie.getValue());
				}
				
			}
		}
		
		// anonymous
		return null;
		
	}
	
	public static IXydraSession getSession(Restless restless, HttpServletRequest req) {
		XID actor = getActor(req);
		IXydraServer server = getXydraServer(restless);
		return new ArmXydraSession(server, actor);
	}
	
	public static XReadableModel getModel(IXydraSession session, String modelIdStr) {
		XID modelId = getId(modelIdStr);
		XReadableModel model = session.getModelSnapshot(modelId);
		if(model == null) {
			throw new RestlessException(RestlessException.Not_found, "no such model " + modelIdStr
			        + " in " + session.getRepositoryAddress());
		}
		return model;
	}
	
	public static XReadableObject getObject(IXydraSession session, String modelIdStr,
	        String objectIdStr) {
		XID objectId = getId(objectIdStr);
		XReadableModel model = getModel(session, modelIdStr);
		XReadableObject object = model.getObject(objectId);
		if(object == null) {
			throw new RestlessException(RestlessException.Not_found, "no such object "
			        + objectIdStr + " in " + model.getAddress());
		}
		return object;
	}
	
	public static XReadableField getField(IXydraSession session, String modelIdStr,
	        String objectIdStr, String fieldIdStr) {
		XID fieldId = getId(fieldIdStr);
		XReadableObject object = getObject(session, modelIdStr, objectIdStr);
		XReadableField field = object.getField(fieldId);
		if(field == null) {
			throw new RestlessException(RestlessException.Not_found, "no field object "
			        + fieldIdStr + " in " + object.getAddress());
		}
		return field;
	}
	
}
