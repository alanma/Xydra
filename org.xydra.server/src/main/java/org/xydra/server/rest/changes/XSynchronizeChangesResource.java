package org.xydra.server.rest.changes;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedEvent;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.MiniParserXml;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XSynchronizeChangesResource {
	
	public static void restless(Restless restless, String prefix) {
		
		RestlessParameter modelId = new RestlessParameter("modelId");
		RestlessParameter objectId = new RestlessParameter("objectId");
		
		String modelPath = prefix + "/{modelId}";
		restless.addMethod(modelPath, "GET", XSynchronizeChangesResource.class, "getEventsModel",
		        false, modelId);
		restless.addMethod(modelPath, "POST", XSynchronizeChangesResource.class,
		        "executeCommandModel", false, modelId);
		
		String objectPath = modelPath + "/{objectId}";
		restless.addMethod(objectPath, "GET", XSynchronizeChangesResource.class, "getEventsObject",
		        false, modelId, objectId);
		restless.addMethod(objectPath, "POST", XSynchronizeChangesResource.class,
		        "executeCommandObject", false, modelId, objectId);
	}
	
	public void getEventsModel(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		getEvents(restless, req, res, XydraRestServer.getId(modelId), null);
	}
	
	public void getEventsObject(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		getEvents(restless, req, res, XydraRestServer.getId(modelId), XydraRestServer
		        .getId(objectId));
	}
	
	public void getEvents(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        XID modelId, XID objectId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		XAddress addr = XX.resolveObject(session.getRepositoryAddress(), modelId, objectId);
		
		Long since = XydraRestServer.getLongParameter(req, "since");
		Long until = XydraRestServer.getLongParameter(req, "until");
		
		long begin = since != null ? since : 0;
		long end = until != null ? until : Long.MAX_VALUE;
		
		if(begin < 0 || end < begin) {
			throw new RestlessException(RestlessException.Bad_request,
			        "invalid since/until combination: " + since + "/" + until);
		}
		
		String changes = getEventsAsXml(session.getChangeLog(modelId), addr, begin, end);
		if(changes == null) {
			// ...
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, changes);
		}
	}
	
	public void executeCommandModel(Restless restless, HttpServletRequest req,
	        HttpServletResponse res, String modelId) {
		executeCommand(restless, req, res, XydraRestServer.getId(modelId), null);
	}
	
	public void executeCommandObject(Restless restless, HttpServletRequest req,
	        HttpServletResponse res, String modelId, String objectId) {
		executeCommand(restless, req, res, XydraRestServer.getId(modelId), XydraRestServer
		        .getId(objectId));
	}
	
	public void executeCommand(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        XID modelId, XID objectId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		XAddress addr = XX.resolveObject(session.getRepositoryAddress(), modelId, objectId);
		
		Long since = XydraRestServer.getLongParameter(req, "since");
		
		if(since != null && since < 0) {
			throw new RestlessException(RestlessException.Bad_request, "invalid since: " + since);
		}
		
		String commandXml = XydraRestServer.readPostData(req);
		
		XCommand command;
		try {
			
			MiniParser parser = new MiniParserXml();
			MiniElement commandElement = parser.parse(commandXml);
			
			command = SerializedCommand.toCommand(commandElement, addr);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XCommand: " + iae.getMessage());
		}
		
		long result = session.executeCommand(command);
		
		int sc;
		if(result == XCommand.FAILED) {
			sc = HttpServletResponse.SC_CONFLICT;
		} else if(result == XCommand.NOCHANGE) {
			sc = HttpServletResponse.SC_OK;
		} else {
			sc = HttpServletResponse.SC_CREATED;
		}
		
		if(since != null || result >= 0) {
			long begin = since != null ? since : result;
			long end = (result >= 0) ? result + 1 : Long.MAX_VALUE;
			String changes = getEventsAsXml(session.getChangeLog(modelId), addr, begin, end);
			if(changes == null) {
				// TODO what to do here?
				res.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				XydraRestServer.xmlResponse(res, sc, changes);
			}
		} else {
			res.setStatus(sc);
		}
	}
	
	private String getEventsAsXml(XChangeLog log, final XAddress addr, long begin, long end) {
		
		if(log == null) {
			return null;
		}
		
		Iterator<XEvent> events = log.getEventsBetween(begin, end);
		
		if(addr.getObject() != null) {
			events = new AbstractTransformingIterator<XEvent,XEvent>(events) {
				@Override
				public XEvent transform(XEvent in) {
					// TODO transform transaction events
					if(!addr.equalsOrContains(in.getTarget())) {
						return null;
					}
					return in;
				}
			};
		}
		
		XydraOut out = new XmlOut();
		SerializedEvent.toXml(events, out, addr);
		return out.getData();
	}
	
}
