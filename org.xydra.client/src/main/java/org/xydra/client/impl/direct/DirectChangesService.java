package org.xydra.client.impl.direct;

import java.util.List;

import org.xydra.client.Callback;
import org.xydra.client.XChangesService;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.session.XProtectedRepository;


public class DirectChangesService implements XChangesService {
	
	private XProtectedRepository repo;
	
	public DirectChangesService(XProtectedRepository repository) {
		this.repo = repository;
	}
	
	@Override
	public void executeCommand(XAddress entity, XCommand command, long since,
	        Callback<CommandResult> callback, XAddress context) {
		
		this.repo.executeCommand(command);
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void getEvents(XAddress entity, long since, long until, Callback<List<XEvent>> callback,
	        XAddress context) {
		
		// TODO Auto-generated method stub
		
	}
	
}
