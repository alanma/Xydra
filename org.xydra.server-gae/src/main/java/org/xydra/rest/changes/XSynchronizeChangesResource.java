package org.xydra.rest.changes;

import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.XX;
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


public class XSynchronizeChangesResource {
	
	private final XProtectedSynchronizesChanges entity;
	
	public XSynchronizeChangesResource(XProtectedSynchronizesChanges entity) {
		this.entity = entity;
	}
	
	@GET
	@Produces("application/xml")
	public Response getEvents(@QueryParam("since") Long since, @QueryParam("until") Long until) {
		
		long begin = since != null ? since : 0;
		long end = until != null ? until : Long.MAX_VALUE;
		
		if(begin < 0 || end < begin) {
			return Response.status(Status.BAD_REQUEST).entity("invalid since/until combination: " + since + "/" + until)
			        .build();
		}
		
		return Response.ok().entity(getEventsAsXml(begin, end)).build();
	}
	
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response executeCommand(String commandXml, @QueryParam("since") Long since) {
		
		if(since != null && since < 0) {
			return Response.status(Status.BAD_REQUEST).entity("invalid since: " + since).build();
		}
		
		XCommand command;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement commandElement = parser.parseXml(commandXml);
			
			command = XmlCommand.toCommand(commandElement, this.entity.getAddress());
			
		} catch(IllegalArgumentException iae) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XCommand: " + iae.getMessage()).build());
		}
		
		long result = this.entity.executeCommand(command);
		
		ResponseBuilder resp;
		if(result == XCommand.FAILED) {
			resp = Response.status(Status.CONFLICT);
		} else if(result == XCommand.NOCHANGE) {
			resp = Response.ok();
		} else {
			resp = Response.status(Status.CREATED);
		}
		
		if(since != null || result >= 0) {
			long begin = since != null ? since : result;
			long end = (result >= 0) ? result + 1 : Long.MAX_VALUE;
			resp = resp.entity(getEventsAsXml(begin, end));
		} else {
			resp.entity("");
		}
		
		return resp.build();
	}
	
	private String getEventsAsXml(long begin, long end) {
		
		Iterator<XEvent> events = this.entity.getChangeLog().getEventsBetween(begin, end);
		
		final XAddress addr = this.entity.getAddress();
		
		if(addr.getObject() != null) {
			events = new AbstractTransformingIterator<XEvent,XEvent>(events) {
				@Override
				public XEvent transform(XEvent in) {
					if(!XX.equalsOrContains(addr, in.getTarget())) {
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
