package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.MiniParserXml;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XModelResource {
	
	static private final XID actorId = XX.toId(XModelResource.class.getName());
	
	public static void restless(Restless restless, String prefix) {
		RestlessParameter modelId = new RestlessParameter("modelId");
		
		String path = prefix + "/{modelId}";
		
		restless.addMethod(path, "GET", XModelResource.class, "get", false, modelId);
		restless.addMethod(path, "POST", XModelResource.class, "setObject", false, modelId);
		restless.addMethod(path, "DELETE", XModelResource.class, "delete", false, modelId);
	}
	
	public void get(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		XReadableModel model = XydraRestServer.getModel(session, modelId);
		
		XydraOut xo = new XmlOut();
		SerializedModel.toXml(model, xo, true, true, false);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getData());
	}
	
	public void setObject(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		String objectXml = XydraRestServer.readPostData(req);
		
		XReadableObject newObject;
		try {
			
			MiniParser parser = new MiniParserXml();
			MiniElement objectElement = parser.parse(objectXml);
			
			newObject = SerializedModel.toObject(actorId, null, objectElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XObject: " + iae.getMessage());
		}
		
		XAddress modelAddr = XX.resolveModel(session.getRepositoryAddress(), XydraRestServer
		        .getId(modelId));
		XTransactionBuilder tb = new XTransactionBuilder(modelAddr);
		tb.setObject(modelAddr, newObject);
		
		long result = session.executeCommand(tb.buildCommand());
		
		if(result == XCommand.FAILED) {
			// containing model doesn't exist
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
		
	}
	
	public void delete(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		XRepositoryCommand removeCommand = MemoryRepositoryCommand.createRemoveCommand(session
		        .getRepositoryAddress(), XCommand.FORCED, XydraRestServer.getId(modelId));
		
		long result = session.executeCommand(removeCommand);
		
		if(result == XCommand.FAILED || result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		
	}
	
}
