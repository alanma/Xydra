package org.xydra.client.gwt.service;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.HttpException;
import org.xydra.client.gwt.NotFoundException;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;


/**
 * Abstract base class for GWT-based X*Service implementations.
 * 
 * @author dscharrer
 * 
 */
public abstract class AbstractGWTHttpService {
	
	protected final String baseUrl;
	protected final MiniXMLParser parser;
	
	public AbstractGWTHttpService(String baseUrl, MiniXMLParser parser) {
		this.baseUrl = baseUrl;
		this.parser = parser;
	}
	
	protected static <T> boolean handleError(Response resp, Callback<T> callback) {
		if(resp.getStatusCode() >= 400) {
			Log.info("data service: HTTP " + resp.getStatusCode() + ": " + resp.getText());
			if(resp.getStatusCode() == Response.SC_NOT_FOUND) {
				callback.onFailure(new NotFoundException(resp.getText()));
			} else {
				callback.onFailure(new HttpException(resp.getStatusCode(), resp.getText()));
			}
			return true;
		}
		return false;
	}
	
	public void getXml(String address, final Callback<MiniElement> callback) {
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, this.baseUrl + "/" + address);
		
		if(callback == null)
			throw new NullPointerException("callback may not be null");
		
		rb.setCallback(new RequestCallback() {
			
			public void onResponseReceived(Request req, Response resp) {
				if(handleError(resp, callback)) {
					return;
				}
				MiniElement element;
				try {
					element = AbstractGWTHttpService.this.parser.parseXml(resp.getText());
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(element);
			}
			
			public void onError(Request req, Throwable t) {
				callback.onFailure(t);
			}
			
		});
		
		try {
			rb.send();
		} catch(RequestException e) {
			throw new RuntimeException(e);
		}
	}
	
}
