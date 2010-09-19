package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedObject;
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


public class XObjectResource {
	
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
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedObject object = XydraRestServer
		        .getProtectedObject(server, req, modelId, objectId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(object, xo, true, true, false);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void setField(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedObject object = XydraRestServer
		        .getProtectedObject(server, req, modelId, objectId);
		
		String fieldXml = XydraRestServer.readPostData(req);
		
		XBaseField newField;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement fieldElement = parser.parseXml(fieldXml);
			
			newField = XmlModel.toField(fieldElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XField: " + iae.getMessage());
		}
		
		long result;
		synchronized(object.getChangeLog()) {
			
			XTransactionBuilder tb = new XTransactionBuilder(object.getAddress());
			tb.setField(object, newField);
			
			if(tb.isEmpty()) {
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
				return;
			}
			
			XTransaction trans = tb.build();
			
			result = object.executeTransaction(trans);
			
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
	        String modelId, String objectIdStr) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedModel model = XydraRestServer.getProtectedModel(server, req, modelId);
		XID objectId = XydraRestServer.getId(objectIdStr);
		
		boolean wasThere = model.removeObject(objectId);
		
		if(!wasThere) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
