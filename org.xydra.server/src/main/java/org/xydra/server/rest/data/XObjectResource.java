package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XObjectResource {
	
	static private final XID actorId = XX.toId(XObjectResource.class.getName());
	
	public static void restless(Restless restless, String prefix) {
		RestlessParameter modelId = new RestlessParameter("modelId");
		RestlessParameter objectId = new RestlessParameter("objectId");
		
		String path = prefix + "/{modelId}/{objectId}";
		
		restless.addMethod(path, "GET", XObjectResource.class, "get", false, modelId, objectId);
		restless.addMethod(path, "POST", XObjectResource.class, "setField", false, modelId,
		        objectId);
		restless.addMethod(path, "DELETE", XObjectResource.class, "delete", false, modelId,
		        objectId);
	}
	
	public void get(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		XReadableObject object = XydraRestServer.getObject(session, modelId, objectId);
		
		XydraOut xo = new XmlOut();
		SerializedModel.serialize(object, xo, true, true, false);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getData());
	}
	
	public void setField(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		String fieldXml = XydraRestServer.readPostData(req);
		
		XReadableField newField;
		try {
			
			XydraParser parser = new XmlParser();
			XydraElement fieldElement = parser.parse(fieldXml);
			
			newField = SerializedModel.toField(actorId, fieldElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XField: " + iae.getMessage());
		}
		
		XAddress objectAddr = XX.resolveObject(session.getRepositoryAddress(), XydraRestServer
		        .getId(modelId), XydraRestServer.getId(objectId));
		XTransactionBuilder tb = new XTransactionBuilder(objectAddr);
		tb.setField(objectAddr, newField);
		
		long result = session.executeCommand(tb.buildCommand());
		
		if(result == XCommand.FAILED) {
			// containing model or object doesn't exist
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
		
	}
	
	public void delete(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		XAddress target = XX.resolveModel(session.getRepositoryAddress(), XydraRestServer
		        .getId(modelId));
		XModelCommand removeCommand = MemoryModelCommand.createRemoveCommand(target,
		        XCommand.FORCED, XydraRestServer.getId(objectId));
		
		long result = session.executeCommand(removeCommand);
		
		if(result == XCommand.FAILED || result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		
	}
	
}
