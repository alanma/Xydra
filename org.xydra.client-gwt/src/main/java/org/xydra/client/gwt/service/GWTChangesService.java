package org.xydra.client.gwt.service;

import java.util.List;

import org.xydra.client.Callback;
import org.xydra.client.XChangesService;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.XmlOutStringBuffer;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;


/**
 * A GWT implementation of the {@link XChangesService} API that uses a
 * {@link RequestBuilder} to perform the HTTP requests.
 */
public class GWTChangesService extends AbstractGWTHttpService implements XChangesService {
	
	public GWTChangesService(String baseUrl, MiniXMLParser parser) {
		super(baseUrl, parser);
	}
	
	public void executeCommand(final XAddress entity, XCommand command, final long since,
	        final Callback<CommandResult> callback, final XAddress context) {
		
		XAddress target = command.getTarget();
		
		if(since != NONE) {
			if(entity.getModel() == null) {
				throw new IllegalArgumentException(
				        "Cannot get events from the whole repository, target was: " + entity);
			}
		}
		
		if(!XX.equalsOrContains(context, target)) {
			throw new IllegalArgumentException("cannot send command " + command + " to entity "
			        + entity);
		}
		
		String url = getUrl(entity, since, Long.MAX_VALUE);
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, this.baseUrl + "/" + url);
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlCommand.toXml(command, xo, context);
		
		rb.setRequestData(xo.getXml());
		
		rb.setHeader("Content-Type", "application/xml");
		
		rb.setCallback(new RequestCallback() {
			
			public void onResponseReceived(Request req, Response resp) {
				if(resp.getStatusCode() != Response.SC_CONFLICT) {
					if(handleError(resp, callback)) {
						return;
					}
				}
				List<XEvent> events = null;
				long result;
				try {
					if(resp.getStatusCode() == Response.SC_CONFLICT) {
						result = XCommand.FAILED;
					} else if(resp.getStatusCode() == Response.SC_CREATED) {
						result = XCommand.CHANGED;
					} else {
						result = XCommand.NOCHANGE;
					}
					
					if(entity.getModel() != null && (result == XCommand.CHANGED || since != NONE)) {
						
						MiniElement element = GWTChangesService.this.parser
						        .parseXml(resp.getText());
						events = XmlEvent.toEventList(element, context);
						
						// fill in a concrete revision number instead of
						// XCommand.CHANGED if possible
						if(result == XCommand.CHANGED && !events.isEmpty()) {
							XEvent event = events.get(events.size() - 1);
							if(event.getModelRevisionNumber() >= 0) {
								result = event.getModelRevisionNumber();
							}
						}
						
					}
					
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(new CommandResult(result, events));
			}
			
			public void onError(Request req, Throwable t) {
				handleError(t, callback);
			}
			
		});
		
		try {
			rb.send();
		} catch(RequestException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void getEvents(XAddress entity, long since, long until,
	        final Callback<List<XEvent>> callback, final XAddress context) {
		
		if(entity.getModel() == null) {
			throw new IllegalArgumentException("cannot get events for the whole repo, entity was: "
			        + entity);
		}
		
		String url = getUrl(entity, since, until);
		
		getXml(url, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				List<XEvent> events;
				try {
					events = XmlEvent.toEventList(xml, context);
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(events);
			}
		});
		
	}
	
	private String getUrl(final XAddress entity, long since, long until) {
		
		if(entity.getField() != null) {
			throw new IllegalArgumentException("cannot get field events, entity was: " + entity);
		}
		String url = entity.getModel().toString();
		if(entity.getObject() != null) {
			url += "/" + entity.getObject().toString();
		}
		
		if(since >= 0) {
			url += "?since=" + since;
			if(until > 0 && until != Long.MAX_VALUE) {
				url += "&until=" + until;
			}
		} else if(until > 0 && until != Long.MAX_VALUE) {
			url += "?until=" + until;
		}
		
		return url;
	}
	
}
