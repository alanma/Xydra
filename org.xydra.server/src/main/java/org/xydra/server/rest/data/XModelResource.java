package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraServer;
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
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedModel model = XydraRestServer.getProtectedModel(server, req, modelId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, true, true, false);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void setObject(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedModel model = XydraRestServer.getProtectedModel(server, req, modelId);
		String objectXml = XydraRestServer.readPostData(req);
		
		XBaseObject newObject;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement objectElement = parser.parseXml(objectXml);
			
			newObject = XmlModel.toObject(objectElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XField: " + iae.getMessage());
		}
		
		long result;
		synchronized(model.getChangeLog()) {
			
			XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
			tb.setObject(model, newObject);
			
			if(tb.isEmpty()) {
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
			
			XTransaction trans = tb.build();
			
			result = model.executeTransaction(trans);
			
		}
		
		if(result == XCommand.FAILED) {
			throw new RestlessException(500, "failed to execute generated transaction");
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
	}
	
	public void delete(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelIdStr) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedRepository repo = XydraRestServer.getProtectedRepository(server, req);
		XID modelId = XydraRestServer.getId(modelIdStr);
		
		boolean wasThere = repo.removeModel(modelId);
		
		if(!wasThere) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
