package org.xydra.rest.data;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XObject;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.rest.XIDParam;


public class XModelResource {
	
	private final XProtectedRepository repository;
	private final XProtectedModel model;
	
	public XModelResource(XProtectedRepository repository, XProtectedModel model) {
		this.repository = repository;
		this.model = model;
	}
	
	@Path("{objectId}")
	public XObjectResource getObject(@PathParam("objectId") XIDParam objectId) {
		
		XProtectedObject object = this.model.getObject(objectId.getId());
		
		if(object == null) {
			XAddress address = XX.resolveObject(this.model.getAddress(), objectId.getId());
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
			        address.toString()).build());
		}
		
		return new XObjectResource(this.model, object);
	}
	
	/**
	 * @response.representation.200.qname {http://www.example.com}item
	 * 
	 * @example.tag This is some example doc that will be processed by the
	 *              {@link ExampleDocProcessor} and the
	 *              {@link ExampleWadlGenerator}.
	 * 
	 * @param req
	 * @param format
	 * @param callback
	 * @return
	 */
	@GET
	@Produces( { "application/xml", "application/json" })
	public Response get(@Context Request req, @QueryParam("format") MediaType format,
	        @QueryParam("callback") String callback) {
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(this.model, xo);
		
		return Response.ok(xo.getXml()).build();
	}
	
	@POST
	@Consumes("application/xml")
	public Response setObject(String objectXml) {
		
		XObject newObject;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement objectElement = parser.parseXml(objectXml);
			
			newObject = XmlModel.toObject(objectElement);
			
		} catch(IllegalArgumentException iae) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XObject: " + iae.getMessage()).build());
		}
		
		synchronized(this.model) {
			
			XTransactionBuilder tb = new XTransactionBuilder(this.model.getAddress());
			tb.setObject(this.model, newObject);
			
			if(tb.isEmpty()) {
				return Response.noContent().build();
			}
			
			XTransaction trans = tb.build();
			
			long result = this.model.executeTransaction(trans);
			
			if(result == XCommand.FAILED) {
				return Response.serverError().entity("failed to execute generated transaction")
				        .build();
			} else if(result == XCommand.NOCHANGE) {
				return Response.noContent().build();
			} else {
				return Response.status(Status.CREATED).build();
			}
			
		}
		
	}
	
	@DELETE
	public Response delete() {
		
		if(!this.repository.removeModel(this.model.getID())) {
			return Response.serverError().entity("removing model from repository failed").build();
		}
		
		return Response.noContent().build();
	}
	
}
