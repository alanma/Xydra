package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


public class XFieldResource {
	
	public static void restless(Restless restless, String prefix) {
		RestlessParameter modelId = new RestlessParameter("modelId");
		RestlessParameter objectId = new RestlessParameter("objectId");
		RestlessParameter fieldId = new RestlessParameter("fieldId");
		
		String path = prefix + "/{modelId}/{objectId}/{fieldId}";
		
		restless.addMethod(path, "GET", XFieldResource.class, "get", false, modelId, objectId,
		        fieldId);
		restless.addMethod(path, "DELETE", XFieldResource.class, "delete", false, modelId,
		        objectId, fieldId);
	}
	
	public void get(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId, String fieldId) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedField field = XydraRestServer.getProtectedField(server, req, modelId, objectId,
		        fieldId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(field, xo, true);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void delete(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId, String fieldIdStr) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedObject object = XydraRestServer
		        .getProtectedObject(server, req, modelId, objectId);
		XID fieldId = XydraRestServer.getId(fieldIdStr);
		
		boolean wasThere = object.removeField(fieldId);
		
		if(!wasThere) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
