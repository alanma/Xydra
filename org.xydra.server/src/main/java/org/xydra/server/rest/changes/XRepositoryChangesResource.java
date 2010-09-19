package org.xydra.server.rest.changes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.IXydraServer;
import org.xydra.server.rest.XydraRestServer;


public class XRepositoryChangesResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix, "POST", XRepositoryChangesResource.class,
		        "executeRepositoryCommand", false);
	}
	
	public void executeRepositoryCommand(Restless restless, HttpServletRequest req,
	        HttpServletResponse res) {
		IXydraServer server = XydraRestServer.getXydraServer(restless);
		XProtectedRepository repo = XydraRestServer.getProtectedRepository(server, req);
		
		String commandXml = XydraRestServer.readPostData(req);
		
		XRepositoryCommand command;
		try {
			
			MiniXMLParser parser = new MiniXMLParserImpl();
			MiniElement commandElement = parser.parseXml(commandXml);
			
			command = XmlCommand.toRepositoryCommand(commandElement, repo.getAddress());
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XRepositoryCommand: " + iae.getMessage());
		}
		
		long result = repo.executeRepositoryCommand(command);
		
		if(result == XCommand.FAILED) {
			res.setStatus(HttpServletResponse.SC_CONFLICT);
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
	}
}
