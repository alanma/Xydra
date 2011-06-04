package org.xydra.store.impl.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
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
	
	public XydraStoreRestClient(URI apiLocation, XydraSerializer serializer, XydraParser parser) {
		super(serializer, parser);
		this.prefix = apiLocation;
	}
	
	private static String readAll(InputStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		Reader reader = new InputStreamReader(stream, "UTF-8");
		int nRead;
		while((nRead = reader.read(buf)) != -1)
			sb.append(buf, 0, nRead);
		return sb.toString();
	}
	
	private HttpURLConnection connect(String uri, Request<?> req) throws IOException {
		
		URL url;
		try {
			url = this.prefix.resolve(uri).toURL();
		} catch(MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		
		con.setRequestProperty(HEADER_COOKIE, encodeLoginCookie(req.actor, req.password));
		
		con.setRequestProperty(HEADER_ACCEPT, this.parser.getContentType());
		
		return con;
	}
	
	private void request(HttpURLConnection con, Request<?> req) throws IOException {
		
		con.connect();
		
		String content = readAll((InputStream)con.getContent());
		
		req.onResponse(content, con.getResponseCode(), con.getResponseMessage());
	}
	
	@Override
	protected void get(String uri, Request<?> req) {
		
		try {
			request(connect(uri, req), req);
		} catch(IOException e) {
			req.onFailure(e);
		}
	}
	
	@Override
	protected void post(String uri, XydraOut data, Request<?> req) {
		
		try {
			
			HttpURLConnection con = connect(uri, req);
			
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty(HAEDER_CONTENT_TYPE, data.getContentType());
			Writer w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			w.write(data.getData());
			w.flush();
			
			request(con, req);
			
		} catch(IOException e) {
			req.onFailure(e);
		}
	}
	
	@Override
	protected String urlencode(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
