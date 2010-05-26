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
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.json.XJson;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.rest.XIDParam;



public class XObjectResource {
	
	private final XProtectedModel model;
	private final XProtectedObject object;
	
	public XObjectResource(XProtectedModel model, XProtectedObject object) {
		this.model = model;
		this.object = object;
	}
	
	@Path("{fieldId}")
	public XFieldResource getField(@PathParam("fieldId") XIDParam fieldId) {
		
		XProtectedField field = this.object.getField(fieldId.getId());
		
		if(field == null) {
			XAddress address = XX.resolveField(this.object.getAddress(), fieldId.getId());
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
			        address.toString()).build());
		}
		
		return new XFieldResource(this.object, field);
	}
	
	@GET
	@Produces( { "application/xml", "application/json" })
	public Response get(@Context Request req, @QueryParam("format") MediaType format,
	        @QueryParam("callback") String callback) {
		
		MediaType mt = XRepositoryResource.selectFormat(req, format);
		
		String result;
		
		if(MediaType.APPLICATION_XML_TYPE.equals(mt)) {
			XmlOutStringBuffer xo = new XmlOutStringBuffer();
			XmlModel.toXml(this.object, xo);
			result = xo.getXml();
		} else if(MediaType.APPLICATION_JSON_TYPE.equals(mt)) {
			XRepositoryResource.verifyCallback(callback);
			result = XJson.asJsonString(this.object);
			if(callback != null)
				result = callback + "(" + result + ");";
		} else
			throw new WebApplicationException(Response.notAcceptable(
			        XRepositoryResource.getVariants()).build());
		
		return Response.ok(result, new Variant(mt, null, "UTF-8")).build();
	}
	
	@POST
	@Consumes("application/xml")
	public Response setField(String fieldXml) {
		
		XField newField;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement fieldElement = parser.parseXml(fieldXml);
			
			newField = XmlModel.toField(fieldElement);
			
		} catch(IllegalArgumentException iae) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XField: " + iae.getMessage()).build());
		}
		
		synchronized(this.model) {
			
			XTransactionBuilder tb = new XTransactionBuilder(this.object.getAddress());
			tb.setField(this.object, newField);
			
			if(tb.isEmpty()) {
				return Response.noContent().build();
			}
			
			XTransaction trans = tb.build();
			
			long result = this.object.executeTransaction(trans);
			
			if(result == XCommand.FAILED)
				throw new WebApplicationException(Response.serverError().entity(
				        "failed to execute generated transaction").build());
			else if(result == XCommand.NOCHANGE)
				return Response.noContent().build();
			else
				return Response.status(Status.CREATED).build();
			
		}
		
	}
	
	@DELETE
	public Response delete() {
		
		if(!this.model.removeObject(this.object.getID())) {
			return Response.serverError().entity("removing object from model failed").build();
		}
		
		return Response.noContent().build();
	}
	
}
