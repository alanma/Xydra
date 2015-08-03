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

		public RestCallback(final Request<?> request) {
			this.request = request;
		}

		@Override
		public void onError(final com.google.gwt.http.client.Request request, final Throwable exception) {
			this.request.onFailure(exception);
		}

		@Override
		public void onResponseReceived(final com.google.gwt.http.client.Request request, final Response response) {
			this.request.onResponse(response.getText(), response.getStatusCode(),
					response.getStatusText());
		}

	}

	/**
	 * @param apiLocation
	 *            format: '/store/v1/'
	 * @param serializer
	 * @param parser
	 */
	public GwtXydraStoreRestClient(final String apiLocation, final XydraSerializer serializer,
			final XydraParser parser) {
		super(serializer, parser);
		this.prefix = apiLocation;
	}

	/**
	 * @param method
	 * @param url
	 *            format: no slash at the begin
	 * @param req
	 * @return a configured {@link RequestBuilder}
	 */
	private final RequestBuilder request(final RequestBuilder.Method method, final String url, final Request<?> req) {

		String uri = this.prefix + url;
		if (url.contains("?")) {
			uri = uri + "&actorId=";
		} else {
			uri = uri + "?actorId=";
		}
		uri = uri + urlencode(req.actor.toString()) + "&passwordHash=" + urlencode(req.password);

		final RequestBuilder rb = new RequestBuilder(method, uri);

		rb.setHeader(HEADER_ACCEPT, this.parser.getContentType());

		return rb;
	}

	@Override
	protected void get(final String uri, final Request<?> req) {
		final RequestBuilder rb = request(RequestBuilder.GET, uri, req);
		rb.setRequestData(null);
		send(rb, req);
	}

	@Override
	protected void post(final String uri, final XydraOut data, final Request<?> req) {
		final RequestBuilder rb = request(RequestBuilder.POST, uri, req);
		rb.setHeader("Content-Type", data.getContentType());
		rb.setRequestData(data.getData());
		send(rb, req);
	}

	private static void send(final RequestBuilder rb, final Request<?> req) {
		rb.setCallback(new RestCallback(req));
		try {
			rb.send();
		} catch (final com.google.gwt.http.client.RequestException re) {
			req.onFailure(re);
		}
	}

}
