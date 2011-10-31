package org.xydra.gwt.store;

import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.store.impl.rest.AbstractXydraStoreRestClient;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;


public class GwtXydraStoreRestClient extends AbstractXydraStoreRestClient {
	
	private final String prefix;
	
	private static class RestCallback implements RequestCallback {
		
		protected final Request<?> request;
		
		public RestCallback(Request<?> request) {
			this.request = request;
		}
		
		@Override
		public void onError(com.google.gwt.http.client.Request request, Throwable exception) {
			this.request.onFailure(exception);
		}
		
		@Override
		public void onResponseReceived(com.google.gwt.http.client.Request request, Response response) {
			this.request.onResponse(response.getText(), response.getStatusCode(),
			        response.getStatusText());
		}
		
	}
	
	public GwtXydraStoreRestClient(String apiLocation, XydraSerializer serializer,
	        XydraParser parser) {
		super(serializer, parser);
		this.prefix = apiLocation;
	}
	
	private final RequestBuilder request(RequestBuilder.Method method, String url, Request<?> req) {
		
		String uri = this.prefix + url;
		if(url.contains("?")) {
			uri = uri + "&actorId=";
		} else {
			uri = uri + "?actorId=";
		}
		uri = uri + urlencode(req.actor.toString()) + "&passwordHash=" + urlencode(req.password);
		
		RequestBuilder rb = new RequestBuilder(method, uri);
		
		rb.setHeader(HEADER_ACCEPT, this.parser.getContentType());
		
		return rb;
	}
	
	@Override
	protected void get(String uri, Request<?> req) {
		RequestBuilder rb = request(RequestBuilder.GET, uri, req);
		rb.setRequestData(null);
		send(rb, req);
	}
	
	@Override
	protected void post(String uri, XydraOut data, Request<?> req) {
		RequestBuilder rb = request(RequestBuilder.POST, uri, req);
		rb.setHeader("Content-Type", data.getContentType());
		rb.setRequestData(data.getData());
		send(rb, req);
	}
	
	private void send(RequestBuilder rb, Request<?> req) {
		rb.setCallback(new RestCallback(req));
		try {
			rb.send();
		} catch(com.google.gwt.http.client.RequestException re) {
			req.onFailure(re);
		}
	}
	
}
