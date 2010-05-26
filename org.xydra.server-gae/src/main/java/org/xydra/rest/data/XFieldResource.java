package org.xydra.rest.data;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.xydra.core.json.XJson;
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
	@Produces( { "application/xml", "application/json" })
	public Response get(@Context Request req, @QueryParam("format") MediaType format,
	        @QueryParam("callback") String callback) {
		
		MediaType mt = XRepositoryResource.selectFormat(req, format);
		
		String result;
		
		if(MediaType.APPLICATION_XML_TYPE.equals(mt)) {
			XmlOutStringBuffer xo = new XmlOutStringBuffer();
			XmlModel.toXml(this.field, xo, true);
			result = xo.getXml();
		} else if(MediaType.APPLICATION_JSON_TYPE.equals(mt)) {
			XRepositoryResource.verifyCallback(callback);
			result = XJson.asJsonString(this.field);
			if(callback != null)
				result = callback + "(" + result + ");";
		} else
			throw new WebApplicationException(Response.notAcceptable(
			        XRepositoryResource.getVariants()).build());
		
		return Response.ok(result, new Variant(mt, null, "UTF-8")).build();
	}
	
	@DELETE
	public Response delete() {
		
		if(!this.object.removeField(this.field.getID())) {
			return Response.serverError().entity("removing field from object failed").build();
		}
		
		return Response.noContent().build();
	}
	
}
