package org.xydra.server;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.changes.XRepositoryChangesResource;
import org.xydra.server.changes.XSynchronizeChangesResource;
import org.xydra.server.data.XFieldResource;
import org.xydra.server.data.XModelResource;
import org.xydra.server.data.XObjectResource;
import org.xydra.server.data.XRepositoryResource;


/**
 * Exposed via Restless over HTTP
 * 
 * @author dscharrer, voelkel
 */
public class XydraServer {
	
	private XydraServer() {
		throw new AssertionError("should not be instantiated");
	}
	
	/**
	 * Setup the Xydra REST API. This expects the {@link RepositoryManager} to
	 * be initialized already.
	 */
	public static void restless(String prefix) {
		
		if(!RepositoryManager.isInitialized()) {
			throw new IllegalStateException("repository manager needs to be initialized first");
		}
		
		Restless.addExceptionHandler(new XAccessExceptionHandler());
		
		String dataPrefix = prefix + "/data";
		XRepositoryResource.restless(dataPrefix);
		XModelResource.restless(dataPrefix);
		XObjectResource.restless(dataPrefix);
		XFieldResource.restless(dataPrefix);
		
		String changesPrefix = prefix + "/changes";
		XSynchronizeChangesResource.restless(changesPrefix);
		XRepositoryChangesResource.restless(changesPrefix);
	}
	
	public static XProtectedRepository getRepository(HttpServletRequest req) {
		
		XID actor = XydraServer.getActor(req);
		
		return RepositoryManager.getRepository(actor);
	}
	
	public static XProtectedModel getModel(HttpServletRequest req, String modelIdStr) {
		XID modelId = getId(modelIdStr);
		XProtectedRepository repo = getRepository(req);
		XProtectedModel model = repo.getModel(modelId);
		if(model == null) {
			throw new RestlessException(RestlessException.Not_found, "no such model " + modelIdStr
			        + " in " + repo.getAddress());
		}
		return model;
	}
	
	public static XProtectedObject getObject(HttpServletRequest req, String modelIdStr,
	        String objectIdStr) {
		XID objectId = getId(objectIdStr);
		XProtectedModel model = getModel(req, modelIdStr);
		XProtectedObject object = model.getObject(objectId);
		if(object == null) {
			throw new RestlessException(RestlessException.Not_found, "no such object "
			        + objectIdStr + " in " + model.getAddress());
		}
		return object;
	}
	
	public static XProtectedField getField(HttpServletRequest req, String modelIdStr,
	        String objectIdStr, String fieldIdStr) {
		XID fieldId = getId(fieldIdStr);
		XProtectedObject object = getObject(req, modelIdStr, objectIdStr);
		XProtectedField field = object.getField(fieldId);
		if(field == null) {
			throw new RestlessException(RestlessException.Not_found, "no field object "
			        + fieldIdStr + " in " + object.getAddress());
		}
		return field;
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
			if(cookie.getName().equals(RepositoryManager.COOKIE_ACTOR)) {
				
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
	
}
