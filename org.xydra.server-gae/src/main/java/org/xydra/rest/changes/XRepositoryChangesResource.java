package org.xydra.rest.changes;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.rest.RepositoryManager;
import org.xydra.rest.XIDParam;



// The Java class will be hosted at the URI path "/changes"
/**
 * Entry point for GETting {@link XEvent}s and PUTting {@link XCommand}s.
 */
@Path("changes")
public class XRepositoryChangesResource {
	
	@Path("{modelId}")
	public XModelChangesResource getModelChanges(@Context HttpHeaders headers,
	        @PathParam("modelId") XIDParam modelId) {
		
		XID actor = RepositoryManager.getActor(headers);
		
		XProtectedRepository repo = RepositoryManager.getRepository(actor);
		
		XProtectedModel model = repo.getModel(modelId.getId());
		
		if(model == null) {
			XAddress address = XX.resolveModel(repo.getAddress(), modelId.getId());
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
			        address.toString()).build());
		}
		
		return new XModelChangesResource(model);
	}
	
	@POST
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response executeRepositoryCommand(@Context HttpHeaders headers, String commandXml) {
		
		XID actor = RepositoryManager.getActor(headers);
		
		XProtectedRepository repo = RepositoryManager.getRepository(actor);
		
		XRepositoryCommand command;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement commandElement = parser.parseXml(commandXml);
			
			command = XmlCommand.toRepositoryCommand(commandElement, repo.getAddress());
			
		} catch(IllegalArgumentException iae) {
			return Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XRepositoryCommand: " + iae.getMessage()).build();
		}
		
		long result = repo.executeRepositoryCommand(command);
		
		if(result == XCommand.FAILED) {
			return Response.status(Status.CONFLICT).entity("").build();
		} else if(result == XCommand.NOCHANGE) {
			return Response.noContent().build();
		} else {
			return Response.status(Status.CREATED).entity("").build();
		}
		
	}
	
}
