package org.xydra.client.gwt.service;

import org.xydra.client.Callback;
import org.xydra.client.NotFoundException;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraParser;
import org.xydra.gwt.store.GwtXydraStoreRestClient;
import org.xydra.store.ConnectionException;
import org.xydra.store.InternalStoreException;
import org.xydra.store.RequestException;
import org.xydra.store.TimeoutException;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;


/**
 * Abstract base class for GWT-based X*Service implementations.
 * 
 * Deprecated, use {@link GwtXydraStoreRestClient} instead.
 * 
 * @author dscharrer
 * 
 */
@Deprecated
public abstract class AbstractGWTHttpService {
	
	protected final String baseUrl;
	protected final XydraParser parser;
	
	public AbstractGWTHttpService(String baseUrl, XydraParser parser) {
		this.baseUrl = baseUrl;
		this.parser = parser;
	}
	
	protected static <T> boolean handleError(Response resp, Callback<T> callback) {
		int sc = resp.getStatusCode();
		if(sc >= 500) {
			callback.onFailure(new InternalStoreException("HTTP " + sc + ": " + resp.getText()));
		} else if(sc >= 400) {
			if(resp.getStatusCode() == Response.SC_NOT_FOUND) {
				callback.onFailure(new NotFoundException(resp.getText()));
			} else {
				callback.onFailure(new RequestException("HTTP " + sc + ": " + resp.getText()));
			}
			return true;
		} else if(sc == 0) {
			callback.onFailure(new ConnectionException("unable to reach server"));
			return true;
		}
		return false;
	}
	
	protected static <T> void handleError(Throwable t, Callback<T> callback) {
		if(t instanceof RequestTimeoutException) {
			int timeout = ((RequestTimeoutException)t).getTimeoutMillis();
			callback.onFailure(new TimeoutException(timeout));
		}
	}
	
	public void getXml(String address, final Callback<XydraElement> callback) {
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.baseUrl + "/" + address);
		
		if(callback == null)
			throw new NullPointerException("callback may not be null");
		
		rb.setCallback(new RequestCallback() {
			
			public void onResponseReceived(Request req, Response resp) {
				if(handleError(resp, callback)) {
					return;
				}
				XydraElement element;
				try {
					element = AbstractGWTHttpService.this.parser.parse(resp.getText());
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(element);
			}
			
			public void onError(Request req, Throwable t) {
				handleError(t, callback);
			}
			
		});
		
		try {
			rb.send();
		} catch(com.google.gwt.http.client.RequestException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
