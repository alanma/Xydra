package org.xydra.client.gwt.service;

import java.util.List;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.ServiceException;
import org.xydra.client.gwt.XChangesService;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.XmlOutStringBuffer;

import com.allen_sauer.gwt.log.client.Log;
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
	
	public void executeCommand(XCommand command, final long since,
	        final Callback<CommandResult> callback) {
		Log.info("changes service: executing command " + command + ", since=" + since);
		
		final XAddress target = command.getTarget();
		
		if(since != NONE) {
			if(target.getModel() == null) {
				throw new IllegalArgumentException(
				        "Cannot get events from the whole repository, target was: " + target);
			}
		}
		
		if(target.getModel() == null && (target.getObject() != null || target.getField() != null)) {
			throw new IllegalArgumentException("target must be relative to repository, was: "
			        + target);
		}
		
		String url = this.baseUrl + "/";
		
		if(target.getModel() != null) {
			url += target.getModel();
		}
		
		if(since >= 0) {
			url += "?since=" + since;
		}
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url);
		
		final XAddress context = X.getIDProvider().fromComponents(target.getRepository(),
		        target.getModel(), null, null);
		
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
					
					if(target.getModel() != null && (result == XCommand.CHANGED || since != NONE)) {
						
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
					Log.info("changes service: executing command failed", e);
					callback.onFailure(e);
					return;
				}
				Log.info("changes service: command executed, "
				        + (events == null ? "no" : events.size()) + " events loaded");
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
	
	public void getEvents(final XID repoId, final XID modelId, long since, long until,
	        final Callback<List<XEvent>> callback) {
		Log.info("changes service: getting events between " + since + " and " + until
		        + " for model " + modelId.toString());
		
		String url = modelId.toString();
		
		if(since >= 0) {
			url += "?since=" + since;
			if(until > 0 && until != Long.MAX_VALUE) {
				url += "&until=" + until;
			}
		} else if(until > 0 && until != Long.MAX_VALUE) {
			url += "?until=" + until;
		}
		
		getXml(url, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				if(error instanceof ServiceException) {
					Log.info("changes service: getting events failed: " + error.getMessage());
				} else {
					Log.info("changes service: getting events failed", error);
				}
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				List<XEvent> events;
				XAddress context = X.getIDProvider().fromComponents(repoId, modelId, null, null);
				try {
					events = XmlEvent.toEventList(xml, context);
				} catch(Exception e) {
					Log.info("changes service: getting events failed", e);
					callback.onFailure(e);
					return;
				}
				Log.info("changes service: " + events.size() + " events loaded");
				callback.onSuccess(events);
			}
		});
		
	}
	
}
