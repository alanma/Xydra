package org.xydra.server.rest;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.session.impl.arm.ArmProtectedRepository;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.IXydraServer;
import org.xydra.server.XydraServerDefaultConfiguration;
import org.xydra.server.rest.changes.XRepositoryChangesResource;
import org.xydra.server.rest.changes.XSynchronizeChangesResource;
import org.xydra.server.rest.data.XFieldResource;
import org.xydra.server.rest.data.XModelResource;
import org.xydra.server.rest.data.XObjectResource;
import org.xydra.server.rest.data.XRepositoryResource;
import org.xydra.server.rest.demo.AddDemoDataResource;
import org.xydra.server.rest.log.LogTestResource;


/**
 * A REST-server exposing a {@link IXydraServer} over HTTP. The
 * {@link IXydraServer} should have been set statically before accessing REST
 * functions.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine
@RunsInJava
public class XydraRestServer {
	
	public static final String INIT_PARAM_XYDRASERVER = "org.xydra.server";
	
	public static final String SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER = "org.xydra.server";
	
	public static IXydraServer getXydraServer(Restless restless) {
		return (IXydraServer)restless.getServletContext().getAttribute(
		        SERVLET_CONTEXT_ATTRIBUTE_XYDRASERVER);
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
	
	public synchronized void initializeServer(Restless restless) {
		
		if(getXydraServer(restless) != null) {
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
	}
	
	public void ping(HttpServletResponse res) throws IOException {
		res.setStatus(200);
		res.setContentType("text/plain");
		res.setCharacterEncoding("utf-8");
		res.getWriter().write("Running. Server time = " + System.currentTimeMillis());
		res.getWriter().flush();
		res.getWriter().close();
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
			res.getWriter().write(xml);
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
	 * @param headers The request headers.
	 * @return The authenticated actor or null if no actor was specified.
	 * @throws WebApplicationException if an actor was specified but could not
	 *             be authenticated
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
	
	public static XProtectedRepository getProtectedRepository(IXydraServer server,
	        HttpServletRequest req) {
		assert server != null;
		XID actor = getActor(req);
		XRepository repo = server.getRepository();
		XAccessManager arm = server.getAccessManager();
		ArmProtectedRepository protectedRepository = new ArmProtectedRepository(repo, arm, actor);
		return protectedRepository;
	}
	
	public static XProtectedModel getProtectedModel(IXydraServer server, HttpServletRequest req,
	        String modelIdStr) {
		assert server != null;
		XID modelId = getId(modelIdStr);
		XProtectedRepository repo = getProtectedRepository(server, req);
		XProtectedModel model = repo.getModel(modelId);
		if(model == null) {
			throw new RestlessException(RestlessException.Not_found, "no such model " + modelIdStr
			        + " in " + repo.getAddress());
		}
		return model;
	}
	
	public static XProtectedObject getProtectedObject(IXydraServer server, HttpServletRequest req,
	        String modelIdStr, String objectIdStr) {
		XID objectId = getId(objectIdStr);
		XProtectedModel model = getProtectedModel(server, req, modelIdStr);
		XProtectedObject object = model.getObject(objectId);
		if(object == null) {
			throw new RestlessException(RestlessException.Not_found, "no such object "
			        + objectIdStr + " in " + model.getAddress());
		}
		return object;
	}
	
	public static XProtectedField getProtectedField(IXydraServer server, HttpServletRequest req,
	        String modelIdStr, String objectIdStr, String fieldIdStr) {
		XID fieldId = getId(fieldIdStr);
		XProtectedObject object = getProtectedObject(server, req, modelIdStr, objectIdStr);
		XProtectedField field = object.getField(fieldId);
		if(field == null) {
			throw new RestlessException(RestlessException.Not_found, "no field object "
			        + fieldIdStr + " in " + object.getAddress());
		}
		return field;
	}
	
}
