package org.xydra.server.rest.changes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.xml.MiniParserXml;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessException;
import org.xydra.server.IXydraSession;
import org.xydra.server.rest.XydraRestServer;


public class XRepositoryChangesResource {
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod(prefix, "POST", XRepositoryChangesResource.class,
		        "executeRepositoryCommand", false);
	}
	
	public void executeRepositoryCommand(Restless restless, HttpServletRequest req,
	        HttpServletResponse res) {
		IXydraSession session = XydraRestServer.getSession(restless, req);
		
		String commandXml = XydraRestServer.readPostData(req);
		
		XRepositoryCommand command;
		try {
			
			MiniParser parser = new MiniParserXml();
			MiniElement commandElement = parser.parse(commandXml);
			
			XAddress repoAddr = session.getRepositoryAddress();
			command = SerializedCommand.toRepositoryCommand(commandElement, repoAddr);
			// TODO allow other command types?
			
		} catch(IllegalArgumentException iae) {
			throw new RestlessException(RestlessException.Bad_request,
			        "could not parse the provided XRepositoryCommand: " + iae.getMessage());
		}
		
		long result = session.executeCommand(command);
		
		if(result == XCommand.FAILED) {
			res.setStatus(HttpServletResponse.SC_CONFLICT);
		} else if(result == XCommand.NOCHANGE) {
			res.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			res.setStatus(HttpServletResponse.SC_CREATED);
		}
	}
	
}
