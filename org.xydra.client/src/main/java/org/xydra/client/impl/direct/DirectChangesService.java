package org.xydra.client.impl.direct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.client.Callback;
import org.xydra.client.NotFoundException;
import org.xydra.client.XChangesService;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.session.XProtectedModel;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.store.AccessException;


public class DirectChangesService implements XChangesService {
	
	private XProtectedRepository repo;
	
	public DirectChangesService(XProtectedRepository repository) {
		this.repo = repository;
	}
	
	@Override
	public void executeCommand(XAddress entity, XCommand command, long since,
	        Callback<CommandResult> callback) {
		
		XAddress target = command.getTarget();
		
		XAddress clientContext;
		if(entity.getModel() == null) {
			if(since != NONE) {
				throw new IllegalArgumentException(
				        "Cannot get events from the whole repository, target was: " + entity);
			}
			if(target.getModel() != null || target.getObject() != null || target.getField() != null) {
				throw new IllegalArgumentException(
				        "can only send XRepositoryCommands to the repository entity");
			}
			clientContext = target;
		} else if(entity.getObject() == null) {
			if(target.getModel() == null) {
				throw new IllegalArgumentException(
				        "cannnot send XRepositoryCommands to model/object entities");
			}
			if(target.getObject() == null) {
				clientContext = target;
			} else {
				clientContext = XX.toAddress(target.getRepository(), target.getModel(), null, null);
			}
		} else {
			if(target.getObject() == null) {
				throw new IllegalArgumentException(
				        "cannnot send model or repository commands to object entities");
			}
			if(target.getField() == null) {
				clientContext = target;
			} else {
				clientContext = target.getParent();
			}
		}
		assert clientContext.equalsOrContains(target);
		XAddress serverContext = XX.resolveObject(this.repo.getAddress(), entity.getModel(), entity
		        .getObject());
		
		XCommand serverCommand = mapCommand(command, clientContext, serverContext);
		
		long result;
		List<XEvent> events = null;
		try {
			result = this.repo.executeCommand(serverCommand);
			
			if(since != NONE || result >= 0) {
				long begin = since >= 0 ? since : result;
				long end = (result >= 0) ? result + 1 : Long.MAX_VALUE;
				// FIXME concurrency: model may already be removed
				events = getEvents(serverContext, begin, end, clientContext);
				if(events == null && result == XCommand.FAILED) {
					callback.onFailure(new NotFoundException(null));
					return;
				}
			}
			
		} catch(AccessException ae) {
			callback.onFailure(ae);
			return;
		}
		
		callback.onSuccess(new CommandResult(result, events));
		
	}
	
	private XCommand mapCommand(XCommand command, XAddress clientContext, XAddress serverContext) {
		
		// IMPROVE do this without XML serialization
		// Map addresses from clientContext to serverContext.
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlCommand.toXml(command, xo, clientContext);
		MiniElement e = new MiniXMLParserImpl().parseXml(xo.getXml());
		return XmlCommand.toCommand(e, serverContext);
		
	}
	
	private XEvent mapEvent(XEvent event, XAddress serverContext, XAddress clientContext) {
		
		// IMPROVE do this without XML serialization
		// Map addresses from serverContext to clientContext.
		XmlOutStringBuffer o = new XmlOutStringBuffer();
		XmlEvent.toXml(event, o, serverContext);
		MiniElement e = new MiniXMLParserImpl().parseXml(o.getXml());
		return XmlEvent.toEvent(e, clientContext);
		
	}
	
	private List<XEvent> getEvents(XAddress serverContext, long begin, long end,
	        XAddress clientContext) {
		
		XProtectedModel model = this.repo.getModel(serverContext.getModel());
		if(model == null) {
			return null;
		}
		XChangeLog log = model.getChangeLog();
		
		List<XEvent> events = new ArrayList<XEvent>();
		
		Iterator<XEvent> it = log.getEventsBetween(begin, end);
		while(it.hasNext()) {
			XEvent event = it.next();
			
			if(serverContext.getObject() != null
			        && !serverContext.getObject().equals(event.getTarget().getObject())) {
				continue;
			}
			
			XEvent clientEvent = mapEvent(event, serverContext, clientContext);
			events.add(clientEvent);
		}
		
		return events;
	}
	
	@Override
	public void getEvents(XAddress entity, long since, long until, Callback<List<XEvent>> callback,
	        XAddress context) {
		
		XAddress serverContext = XX.resolveObject(this.repo.getAddress(), entity.getModel(), entity
		        .getObject());
		
		long begin = since >= 0 ? since : 0;
		long end = until > 0 ? until : Long.MAX_VALUE;
		
		List<XEvent> events;
		try {
			
			events = getEvents(serverContext, begin, end, context);
			if(events == null) {
				callback.onFailure(new NotFoundException(null));
				return;
			}
			
		} catch(AccessException ae) {
			callback.onFailure(ae);
			return;
		}
		
		callback.onSuccess(events);
	}
	
}
