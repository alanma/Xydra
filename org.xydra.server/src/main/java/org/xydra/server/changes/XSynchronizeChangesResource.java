package org.xydra.server.changes;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.session.XProtectedSynchronizesChanges;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.XydraServer;


public class XSynchronizeChangesResource {
	
	public static void restless(String prefix) {
		
		RestlessParameter modelId = new RestlessParameter("modelId");
		RestlessParameter objectId = new RestlessParameter("objectId");
		
		String modelPath = prefix + "/{modelId}";
		Restless.addGenericStatic(modelPath, "GET", XSynchronizeChangesResource.class,
		        "getEventsModel", false, modelId);
		Restless.addGenericStatic(modelPath, "POST", XSynchronizeChangesResource.class,
		        "executeCommandModel", false, modelId);
		
		String objectPath = modelPath + "/{objectId}";
		Restless.addGenericStatic(objectPath, "GET", XSynchronizeChangesResource.class,
		        "getEventsObject", false, modelId, objectId);
		Restless.addGenericStatic(objectPath, "POST", XSynchronizeChangesResource.class,
		        "executeCommandObject", false, modelId, objectId);
	}
	
	public void getEventsModel(HttpServletRequest req, HttpServletResponse res, String modelId) {
		getEvents(req, res, XydraServer.getModel(req, modelId));
	}
	
	public void getEventsObject(HttpServletRequest req, HttpServletResponse res, String modelId,
	        String objectId) {
		getEvents(req, res, XydraServer.getObject(req, modelId, objectId));
	}
	
	public void getEvents(HttpServletRequest req, HttpServletResponse res,
	        XProtectedSynchronizesChanges entity) {
		
		Long since = XydraServer.getLongParameter(req, "since");
		Long until = XydraServer.getLongParameter(req, "until");
		
		long begin = since != null ? since : 0;
		long end = until != null ? until : Long.MAX_VALUE;
		
		if(begin < 0 || end < begin) {
			throw new RestlessException(RestlessException.Bad_request,
			        "invalid since/until combination: " + since + "/" + until);
		}
		
		XydraServer.xmlResponse(res, HttpServletResponse.SC_OK, getEventsAsXml(entity, begin, end));
	}
	
	public void executeCommandModel(HttpServletRequest req, HttpServletResponse res, String modelId) {
		executeCommand(req, res, XydraServer.getModel(req, modelId));
	}
	
	public void executeCommandObject(HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		executeCommand(req, res, XydraServer.getObject(req, modelId, objectId));
	}
	
	public void executeCommand(HttpServletRequest req, HttpServletResponse res,
	        XProtectedSynchronizesChanges entity) {
		
		Long since = XydraServer.getLongParameter(req, "since");
		
		if(since != null && since < 0) {
			throw new RestlessException(RestlessException.Bad_request, "invalid since: " + since);
		}
		
		String commandXml = XydraServer.readPostData(req);
		
		XCommand command;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement commandElement = parser.parseXml(commandXml);
			
			command = XmlCommand.toCommand(commandElement, entity.getAddress());
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XCommand: " + iae.getMessage());
		}
		
		long result = entity.executeCommand(command);
		
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
			XydraServer.xmlResponse(res, sc, getEventsAsXml(entity, begin, end));
		} else {
			res.setStatus(sc);
		}
	}
	
	private String getEventsAsXml(XProtectedSynchronizesChanges entity, long begin, long end) {
		
		Iterator<XEvent> events = entity.getChangeLog().getEventsBetween(begin, end);
		
		final XAddress addr = entity.getAddress();
		
		if(addr.getObject() != null) {
			events = new AbstractTransformingIterator<XEvent,XEvent>(events) {
				@Override
				public XEvent transform(XEvent in) {
					if(!addr.equalsOrContains(in.getTarget())) {
						return null;
					}
					return in;
				}
			};
		}
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlEvent.toXml(events, out, addr);
		return out.getXml();
	}
	
}
