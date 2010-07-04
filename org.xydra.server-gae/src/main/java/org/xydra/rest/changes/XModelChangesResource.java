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

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


public class XModelChangesResource {
	
	private final XProtectedModel model;
	
	public XModelChangesResource(XProtectedModel model) {
		this.model = model;
	}
	
	@GET
	@Produces("application/xml")
	public Response getEvents(@QueryParam("since") Long since, @QueryParam("until") Long until) {
		
		long begin = since != null ? since : 0;
		long end = until != null ? until : Long.MAX_VALUE;
		
		Iterator<XEvent> events = this.model.getChangeLog().getEventsBetween(begin, end);
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlEvent.toXml(events, out, this.model.getAddress());
		
		return Response.ok().entity(out.getXml()).build();
	}
	
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response executeCommand(String commandXml, @QueryParam("since") Long since) {
		
		XCommand command;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement commandElement = parser.parseXml(commandXml);
			
			command = XmlCommand.toCommand(commandElement, this.model.getAddress());
			
		} catch(IllegalArgumentException iae) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XCommand: " + iae.getMessage()).build());
		}
		
		long result = this.model.executeCommand(command);
		
		ResponseBuilder resp;
		if(result == XCommand.FAILED) {
			resp = Response.status(Status.CONFLICT);
		} else if(result == XCommand.NOCHANGE) {
			resp = Response.ok();
		} else {
			resp = Response.status(Status.CREATED);
		}
		
		if(since != null || result >= 0) {
			XmlOutStringBuffer out = new XmlOutStringBuffer();
			long begin = since != null ? since : result;
			long end = (result >= 0) ? result + 1 : Long.MAX_VALUE;
			Iterator<XEvent> events = this.model.getChangeLog().getEventsBetween(begin, end);
			XmlEvent.toXml(events, out, this.model.getAddress());
			resp = resp.entity(out.getXml());
		} else {
			resp.entity("");
		}
		
		return resp.build();
	}
}
