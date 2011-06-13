package org.xydra.client.gwt.service;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.client.Callback;
import org.xydra.client.XChangesService;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedEvent;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.store.impl.gwt.GwtXydraStoreRestClient;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;


/**
 * A GWT implementation of the {@link XChangesService} API that uses a
 * {@link RequestBuilder} to perform the HTTP requests.
 * 
 * Deprecated, use {@link GwtXydraStoreRestClient} instead.
 */
@Deprecated
public class GWTChangesService extends AbstractGWTHttpService implements XChangesService {
	
	public GWTChangesService(String baseUrl, XydraParser parser) {
		super(baseUrl, parser);
	}
	
	public void executeCommand(final XAddress entity, XCommand command, final long since,
	        final Callback<CommandResult> callback) {
		
		XAddress target = command.getTarget();
		
		if(since != NONE) {
			if(entity.getModel() == null) {
				throw new IllegalArgumentException(
				        "Cannot get events from the whole repository, target was: " + entity);
			}
		}
		
		final XAddress context;
		if(entity.getModel() == null) {
			if(since != NONE) {
				throw new IllegalArgumentException(
				        "Cannot get events from the whole repository, target was: " + entity);
			}
			if(target.getModel() != null || target.getObject() != null || target.getField() != null) {
				throw new IllegalArgumentException(
				        "can only send XRepositoryCommands to the repository entity");
			}
			context = target;
		} else if(entity.getObject() == null) {
			if(target.getModel() == null) {
				throw new IllegalArgumentException(
				        "cannnot send XRepositoryCommands to model/object entities");
			}
			if(target.getObject() == null) {
				context = target;
			} else {
				context = XX.toAddress(target.getRepository(), target.getModel(), null, null);
			}
		} else {
			if(target.getObject() == null) {
				throw new IllegalArgumentException(
				        "cannnot send model or repository commands to object entities");
			}
			if(target.getField() == null) {
				context = target;
			} else {
				context = target.getParent();
			}
		}
		assert context.equalsOrContains(target);
		
		String url = getUrl(entity, since, Long.MAX_VALUE);
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, this.baseUrl + "/" + url);
		
		XydraOut xo = new XmlOut();
		SerializedCommand.serialize(command, xo, context);
		
		rb.setRequestData(xo.getData());
		
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
						result = 0;
					} else {
						result = XCommand.NOCHANGE;
					}
					
					if(entity.getModel() != null && (result == 0 || since != NONE)) {
						
						XydraElement element = GWTChangesService.this.parser.parse(resp.getText());
						events = SerializedEvent.toEventList(element, context);
						
						// fill in a concrete revision number instead of
						// XCommand.CHANGED if possible
						if(result == 0 && !events.isEmpty()) {
							XEvent event = events.get(events.size() - 1);
							if(event.getOldModelRevision() >= 0) {
								result = event.getOldModelRevision();
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
		
		getXml(url, new Callback<XydraElement>() {
			
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
			
			public void onSuccess(XydraElement xml) {
				List<XEvent> events;
				try {
					events = SerializedEvent.toEventList(xml, context);
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
