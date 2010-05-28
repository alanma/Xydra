package org.xydra.rest.data;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;


public class XFieldResource {
	
	private final XProtectedObject object;
	private final XProtectedField field;
	
	public XFieldResource(XProtectedObject object, XProtectedField field) {
		this.object = object;
		this.field = field;
	}
	
	@GET
	@Produces("application/xml")
	public Response get(@Context Request req, @QueryParam("format") MediaType format,
	        @QueryParam("callback") String callback) {
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(this.field, xo, true);
		
		return Response.ok(xo.getXml()).build();
	}
	
	@DELETE
	public Response delete() {
		
		if(!this.object.removeField(this.field.getID())) {
			return Response.serverError().entity("removing field from object failed").build();
		}
		
		return Response.noContent().build();
	}
	
}
