package org.xydra.rest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.rest.RepositoryManager;
import org.xydra.rest.XIDParam;



// The Java class will be hosted at the URI path "/data"
/**
 * Entry point for GETting and PUTting {@link XModel}s.
 */
@Path("data")
public class XRepositoryResource {
	
	private static final Pattern CALLBACK_REGEX = Pattern.compile("[a-zA-Z]\\w*");
	
	@Path("{modelId}")
	public XModelResource getModel(@Context HttpHeaders headers,
	        @PathParam("modelId") XIDParam modelId) {
		
		XID actor = RepositoryManager.getActor(headers);
		
		XProtectedRepository repo = RepositoryManager.getRepository(actor);
		
		XProtectedModel model = repo.getModel(modelId.getId());
		
		if(model == null) {
			XAddress address = XX.resolveModel(repo.getAddress(), modelId.getId());
			throw new WebApplicationException(Response.status(Status.NOT_FOUND).entity(
			        address.toString()).build());
		}
		
		return new XModelResource(repo, model);
	}
	
	@POST
	@Consumes("application/xml")
	public Response setModel(@Context HttpHeaders headers, String modelXml) {
		
		XModel newModel;
		
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement modelElement = parser.parseXml(modelXml);
			
			newModel = XmlModel.toModel(modelElement);
			
		} catch(IllegalArgumentException iae) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "could not parse the provided XModel: " + iae.getMessage()).build());
		}
		
		XID actor = RepositoryManager.getActor(headers);
		
		XProtectedRepository repo = RepositoryManager.getRepository(actor);
		
		XProtectedModel model = repo.createModel(newModel.getID());
		
		synchronized(model) {
			
			XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
			tb.changeModel(model, newModel);
			
			if(tb.isEmpty()) {
				return Response.noContent().build();
			}
			
			XTransaction trans = tb.build();
			
			long result = model.executeTransaction(trans);
			
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
	
	protected static MediaType selectFormat(Request req, MediaType format) {
		
		if(format != null)
			return format;
		
		Variant v = req.selectVariant(getVariants());
		if(v == null)
			return null;
		return v.getMediaType();
	}
	
	protected static List<Variant> getVariants() {
		List<Variant> vl = new ArrayList<Variant>();
		vl.add(new Variant(MediaType.APPLICATION_XML_TYPE, null, null));
		vl.add(new Variant(MediaType.APPLICATION_JSON_TYPE, null, null));
		return vl;
	}
	
	protected static void verifyCallback(String callback) {
		if(callback == null)
			return;
		if(!CALLBACK_REGEX.matcher(callback).matches())
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "invalid callback name").build());
	}
	
}
