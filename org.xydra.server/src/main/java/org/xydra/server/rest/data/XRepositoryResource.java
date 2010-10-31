package org.xydra.server.rest.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XRepositoryResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix + "/", "POST", XRepositoryResource.class, "setModel", false);
	}
	
	public void setModel(Restless restless, HttpServletRequest req, HttpServletResponse res) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		String modelXml = XydraRestServer.readPostData(req);
		
		XBaseModel newModel;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement modelElement = parser.parseXml(modelXml);
			
			newModel = XmlModel.toModel(modelElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XObject: " + iae.getMessage());
		}
		
		boolean hadModel = true;
		XBaseModel oldModel = session.getModelSnapshot(newModel.getID());
		if(oldModel == null) {
			XRepositoryCommand createCommand = MemoryRepositoryCommand.createAddCommand(session
			        .getRepositoryAddress(), false, newModel.getID());
			hadModel = (session.executeCommand(createCommand) == XCommand.NOCHANGE);
			oldModel = session.getModelSnapshot(newModel.getID());
		}
		// FIXME synchronization issues that can only be resolved by moving the
		// create command into the transaction (which isn't currently possible)
		XTransactionBuilder tb = new XTransactionBuilder(oldModel.getAddress());
		tb.changeModel(oldModel, newModel);
		
		if(tb.isEmpty()) {
			if(hadModel) {
				res.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				res.setStatus(HttpServletResponse.SC_CREATED);
			}
			return;
		}
		
		long result = session.executeCommand(tb.buildCommand());
		
		if(result == XCommand.FAILED) {
			throw new RestlessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			        "failed to execute generated transaction");
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
		
	}
	
}
