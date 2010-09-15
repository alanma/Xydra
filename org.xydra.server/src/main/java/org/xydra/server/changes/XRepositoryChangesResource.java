package org.xydra.server.changes;

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
import org.xydra.server.XydraServer;


public class XRepositoryChangesResource {
	
	public static void restless(String prefix) {
		Restless.addGenericStatic(prefix, "POST", XRepositoryChangesResource.class,
		        "executeRepositoryCommand", false);
	}
	
	public void executeRepositoryCommand(HttpServletRequest req, HttpServletResponse res) {
		
		XProtectedRepository repo = XydraServer.getRepository(req);
		
		String commandXml = XydraServer.readPostData(req);
		
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
