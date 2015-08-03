package org.xydra.store.impl.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.XydraStore;


/**
 * {@link XydraStore} implementation that connects to a xydra store REST server
 * using synchronous network operations.
 *
 * @author dscharrer
 *
 */
@RunsInGWT(false)
public class XydraStoreRestClient extends AbstractXydraStoreRestClient {

	private final URI prefix;

	/**
	 * @param apiLocation absolute url of Xydra REST endpoint
	 * @param serializer ..
	 * @param parser ..
	 */
	public XydraStoreRestClient(final URI apiLocation, final XydraSerializer serializer, final XydraParser parser) {
		super(serializer, parser);
		XyAssert.xyAssert(apiLocation != null); assert apiLocation != null;
		this.prefix = apiLocation;
	}

	private static String readAll(final InputStream stream) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final char[] buf = new char[4096];
		final Reader reader = new InputStreamReader(stream, "UTF-8");
		int nRead;
		while((nRead = reader.read(buf)) != -1) {
			sb.append(buf, 0, nRead);
		}
		return sb.toString();
	}

	private HttpURLConnection connect(final String uri, final Request<?> req) throws IOException {

		URL url;
		try {
			url = this.prefix.resolve(uri).toURL();
		} catch(final MalformedURLException e1) {
			throw new RuntimeException(e1);
		}

		final HttpURLConnection con = (HttpURLConnection)url.openConnection();

		con.setRequestProperty(HEADER_COOKIE, encodeLoginCookie(req.actor, req.password));

		con.setRequestProperty(HEADER_ACCEPT, this.parser.getContentType());

		return con;
	}

	private static void request(final HttpURLConnection con, final Request<?> req) throws IOException {

		con.connect();

		final String content = readAll((InputStream)con.getContent());

		req.onResponse(content, con.getResponseCode(), con.getResponseMessage());
	}

	@Override
	protected void get(final String uri, final Request<?> req) {

		try {
			request(connect(uri, req), req);
		} catch(final IOException e) {
			req.onFailure(e);
		}
	}

	@Override
	protected void post(final String uri, final XydraOut data, final Request<?> req) {

		try {

			final HttpURLConnection con = connect(uri, req);

			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty(HAEDER_CONTENT_TYPE, data.getContentType());
			final Writer w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			w.write(data.getData());
			w.flush();

			request(con, req);

		} catch(final IOException e) {
			req.onFailure(e);
		}
	}

}
