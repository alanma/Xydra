package org.xydra.server.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.model.XID;
import org.xydra.core.model.session.XProtectedField;
import org.xydra.core.model.session.XProtectedObject;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.XydraServer;


public class XFieldResource {
	
	public void restless(String prefix) {
		
		RestlessParameter modelId = new RestlessParameter("modelId");
		RestlessParameter objectId = new RestlessParameter("objectId");
		RestlessParameter fieldId = new RestlessParameter("fieldId");
		
		String path = prefix + "/{modelId}/{objectId}/{fieldId}";
		
		Restless.addGet(path, this, "get", modelId, objectId, fieldId);
		Restless.addDelete(path, this, "delete", modelId, objectId, fieldId);
		
	}
	
	public void get(HttpServletRequest req, HttpServletResponse res, String modelId,
	        String objectId, String fieldId) {
		
		XProtectedField field = XydraServer.getField(req, modelId, objectId, fieldId);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(field, xo);
		
		XydraServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getXml());
	}
	
	public void delete(HttpServletRequest req, HttpServletResponse res, String modelId,
	        String objectId, String fieldIdStr) {
		
		XProtectedObject object = XydraServer.getObject(req, modelId, objectId);
		XID fieldId = XydraServer.getId(fieldIdStr);
		
		boolean wasThere = object.removeField(fieldId);
		
		if(!wasThere) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
