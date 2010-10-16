package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XModelResource {
	
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
		XBaseModel model = XydraRestServer.getModel(session, modelId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, true, true, false);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void setObject(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		String objectXml = XydraRestServer.readPostData(req);
		
		XBaseObject newObject;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement objectElement = parser.parseXml(objectXml);
			
			newObject = XmlModel.toObject(objectElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XObject: " + iae.getMessage());
		}
		
		XAddress modelAddr = XX.toAddress(session.getRepositoryAddress().getRepository(),
		        XydraRestServer.getId(modelId), null, null);
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
