package org.xydra.server.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.XydraServer;


public class XRepositoryResource {
	
	public void restless(String prefix) {
		
		Restless.addPost(prefix, this, "setModel");
		
	}
	
	public void setModel(HttpServletRequest req, HttpServletResponse res) {
		
		XProtectedRepository repo = XydraServer.getRepository(req);
		
		String modelXml = XydraServer.readPostData(req);
		
		XBaseModel newModel;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement modelElement = parser.parseXml(modelXml);
			
			newModel = XmlModel.toModel(modelElement);
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XField: " + iae.getMessage());
		}
		
		boolean hadModel = repo.hasModel(newModel.getID());
		// FIXME race condition
		XProtectedModel model = repo.createModel(newModel.getID());
		
		long result;
		synchronized(model.getChangeLog()) {
			
			XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
			tb.changeModel(model, newModel);
			
			if(tb.isEmpty()) {
				if(hadModel) {
					res.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					res.setStatus(HttpServletResponse.SC_CREATED);
				}
				return;
			}
			
			XTransaction trans = tb.build();
			
			result = model.executeTransaction(trans);
			
		}
		
		if(result == XCommand.FAILED) {
			throw new RestlessException(500, "failed to execute generated transaction");
		} else if(hadModel && result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
	}
	
}
