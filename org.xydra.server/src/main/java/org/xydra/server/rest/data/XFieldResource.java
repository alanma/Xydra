package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.server.IXydraSession;
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
		IXydraSession session = XydraRestServer.getSession(restless, req);
		XReadableField field = XydraRestServer.getField(session, modelId, objectId, fieldId);
		
		XydraOut xo = new XmlOut();
		SerializedModel.serialize(field, xo, true);
		
		XydraRestServer.xmlResponse(res, HttpServletResponse.SC_OK, xo.getData());
	}
	
	public void delete(Restless restless, HttpServletRequest req, HttpServletResponse res,
	        String modelId, String objectId, String fieldId) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		XAddress target = XX.resolveObject(session.getRepositoryAddress(), XydraRestServer
		        .getId(modelId), XydraRestServer.getId(objectId));
		XObjectCommand removeCommand = MemoryObjectCommand.createRemoveCommand(target,
		        XCommand.FORCED, XydraRestServer.getId(fieldId));
		
		long result = session.executeCommand(removeCommand);
		
		if(result == XCommand.FAILED || result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
	}
	
}
