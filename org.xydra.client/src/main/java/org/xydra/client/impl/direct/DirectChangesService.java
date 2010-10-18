package org.xydra.client.impl.direct;

import java.util.List;

import org.xydra.client.Callback;
import org.xydra.client.XChangesService;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;


public class DirectChangesService implements XChangesService {
	
	private static final XID ACTOR = X.getIDProvider().fromString(
	        DirectDataService.class.toString());
	
	private XRepository repo;
	
	public DirectChangesService(XRepository repository) {
		this.repo = repository;
	}
	
	@Override
	public void executeCommand(XAddress entity, XCommand command, long since,
	        Callback<CommandResult> callback, XAddress context) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void getEvents(XAddress entity, long since, long until, Callback<List<XEvent>> callback,
	        XAddress context) {
		
		// TODO Auto-generated method stub
		
	}
	
}
