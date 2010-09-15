package org.xydra.server.data;

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
import org.xydra.server.XydraServer;


public class XModelResource {
	
	public static void restless(String prefix) {
		RestlessParameter modelId = new RestlessParameter("modelId");
		
		String path = prefix + "/{modelId}";
		
		Restless.addGenericStatic(path, "GET", XModelResource.class, "get", false, modelId);
		Restless.addGenericStatic(path, "POST", XModelResource.class, "setObject", false, modelId);
		Restless.addGenericStatic(path, "DELETE", XModelResource.class, "delete", false, modelId);
	}
	
	public void get(HttpServletRequest req, HttpServletResponse res, String modelId) {
		
		XProtectedModel model = XydraServer.getModel(req, modelId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, true, true, false);
		
		XydraServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void setObject(HttpServletRequest req, HttpServletResponse res, String modelId) {
		
		XProtectedModel model = XydraServer.getModel(req, modelId);
		
		String objectXml = XydraServer.readPostData(req);
		
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
	
	public void delete(HttpServletRequest req, HttpServletResponse res, String modelIdStr) {
		
		XProtectedRepository repo = XydraServer.getRepository(req);
		XID modelId = XydraServer.getId(modelIdStr);
		
		boolean wasThere = repo.removeModel(modelId);
		
		if(!wasThere) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
