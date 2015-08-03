package org.xydra.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple proxy servlet that forwards all requests to a different server.
 *
 * @author dscharrer
 *
 *         TODO Max: switch to proper logging
 *
 *         TODO Max: adapt to AppEngine (Max has some helper classes for
 *         this...)
 */
public class ProxyServlet extends HttpServlet {

	private final URI remoteService;

	public ProxyServlet() {
		try {
			this.remoteService = new URI("http://localhost:8080/xydra/");
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private static final long serialVersionUID = -5107058075707773775L;

	@Override
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) {

		HttpURLConnection con;

		int code;

		try {

			String path = req.getPathInfo();
			final String query = req.getQueryString();
			if (query != null) {
				path += "?" + query;
			}

			System.out.println(req.getMethod() + " " + path);

			while (path.length() > 0 && path.charAt(0) == '/') {
				path = path.substring(1);
			}

			final URL url = this.remoteService.resolve(path).toURL();

			System.out.println("connecting to " + url);

			con = (HttpURLConnection) url.openConnection();

			final String method = req.getMethod();
			con.setRequestMethod(method);

			final Enumeration<String> hn = req.getHeaderNames();
			while (hn.hasMoreElements()) {
				final String name = hn.nextElement();
				final String value = req.getHeader(name);
				if (name.equalsIgnoreCase("Host")) {
					continue;
				}
				if (name.equalsIgnoreCase("Connection")) {
					continue;
				}
				if (name.equalsIgnoreCase("Keep-Alive")) {
					continue;
				}
				System.out.println("req: " + name + ": " + value);
				con.setRequestProperty(name, value);
			}

			if (!req.getMethod().equalsIgnoreCase("GET") && req.getContentLength() != 0) {
				con.setDoOutput(true);
				System.out.println("request body:");
				copy(req.getInputStream(), con.getOutputStream());
				System.out.println();
			}

			code = con.getResponseCode();

		} catch (final IOException e) {
			System.out.println("cannot connect to server");
			resp.setStatus(503);
			try {
				resp.getWriter().write("cannot connect to xydra server: " + e.getMessage());
			} catch (final IOException e1) {
				throw new RuntimeException(e1);
			}
			return;
		}
		System.out.println("response code: " + code);
		resp.setStatus(code);

		final Map<String, List<String>> headers = con.getHeaderFields();
		for (final Map.Entry<String, List<String>> header : headers.entrySet()) {
			final String name = header.getKey();
			if (name == null) {
				continue;
			}
			for (final String value : header.getValue()) {
				System.out.println("resp: " + name + ": " + value);
				resp.addHeader(name, value);
			}
		}

		if (con.getContentLength() != 0) {
			try {
				copy(con.getInputStream(), resp.getOutputStream());
				con.disconnect();
			} catch (final IOException e) {
				final InputStream err = con.getErrorStream();
				if (err != null) {
					try {
						copy(err, resp.getOutputStream());
					} catch (final IOException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			con.setDoInput(false);
			con.disconnect();
		}

	}

	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}

}
