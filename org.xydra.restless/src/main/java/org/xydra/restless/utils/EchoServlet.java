package org.xydra.restless.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;

/**
 * Loops back the complete incoming request
 *
 * @author xamde
 *
 */

@ThreadSafe
public class EchoServlet extends HttpServlet {

	private static final long serialVersionUID = 4266214485819030466L;

	/*
	 * Requests and responses aren't shared between different threads, so no
	 * synchronization is necessary here.
	 */

	/**
	 *
	 * @param req @NeverNull
	 * @param resp @NeverNull
	 */
	@Override
	public void doGet(@NeverNull final HttpServletRequest req, @NeverNull final HttpServletResponse resp)
			throws IOException {
		echo(req, resp);
	}

	/**
	 *
	 * @param req @NeverNull
	 * @param resp @NeverNull
	 */
	@Override
	public void doPost(@NeverNull final HttpServletRequest req, @NeverNull final HttpServletResponse resp)
			throws IOException {
		echo(req, resp);
	}

	/**
	 *
	 * @param req @NeverNull
	 * @param resp @NeverNull
	 */
	private static void echo(@NeverNull final HttpServletRequest req, @NeverNull final HttpServletResponse resp)
			throws IOException {
		resp.setStatus(200);
		resp.setContentType("text/plain");

		final PrintWriter w = resp.getWriter();
		w.println("Got request.");
		w.println("== Request ==");
		w.println(" * schema: " + req.getScheme()); // HTTP
		w.println(" * protocol: " + req.getProtocol()); // 1.1
		w.println(" * method: " + req.getMethod()); // GET
		w.println(" * encoding: " + req.getCharacterEncoding());
		w.println(" * contentType: " + req.getContentType());
		w.println(" * contentLength: " + req.getContentLength());
		w.println(" * authType: " + req.getAuthType());

		if (req.getCookies() != null) {
			w.println("=== Cookies ===");
			for (final Cookie cookie : req.getCookies()) {
				w.println(cookie.getName());
				w.println(" * version: " + cookie.getVersion());
				w.println(" * domain: " + cookie.getDomain());
				w.println(" * path: " + cookie.getPath());
				w.println(" * maxAge: " + cookie.getMaxAge());
				w.println(" * value: " + cookie.getValue());
				w.println(" * comment: " + cookie.getComment());
			}
		}

		w.println("=== Headers ===");
		final Enumeration<String> headers = req.getHeaderNames();
		while (headers.hasMoreElements()) {
			final String headerName = headers.nextElement();
			w.println(headerName);
			final Enumeration<String> headerValues = req.getHeaders(headerName);
			while (headerValues.hasMoreElements()) {
				final String headerValue = headerValues.nextElement();
				w.println(" * " + headerValue);
			}
		}

		w.println("");
		w.println("== Payload ==");
		w.println("=== Request Parameters ===");
		final Enumeration<String> parameters = req.getParameterNames();
		while (parameters.hasMoreElements()) {
			final String paramName = parameters.nextElement();
			for (final String paramValue : req.getParameterValues(paramName)) {
				w.println(" * " + paramName + " = " + paramValue);
			}
		}
		w.println("=== Path ===");
		w.println(" * requestURI: " + req.getRequestURI());
		w.println(" * requestURL: " + req.getRequestURL());
		w.println(" * contextPath: '" + req.getContextPath() + "'");
		w.println(" * pathInfo: " + req.getPathInfo());
		w.println(" * queryString: " + req.getQueryString());

		w.println(" * remoteAddr: " + req.getRemoteAddr());
		w.println(" * remoteHost: " + req.getRemoteHost());
		w.println(" * remotePort: " + req.getRemotePort());
		w.println(" * remoteUser: " + req.getRemoteUser());

		w.println("== Derrived Information ==");
		w.println(" * client locale: " + req.getLocale());
		w.println(" * pathTranslated: " + req.getPathTranslated());
		w.println(" * sessionId: " + req.getRequestedSessionId());

		w.println("=== Addressing ===");
		w.println(" * localAddr: " + req.getLocalAddr());
		w.println(" * localName: " + req.getLocalName());
		w.println(" * localPort: " + req.getLocalPort());

		w.println("=== Attributes ===");
		final Enumeration<String> attributes = req.getAttributeNames();
		while (attributes.hasMoreElements()) {
			final String attName = attributes.nextElement();
			final Object attValue = req.getAttribute(attName);
			w.println(" * " + attName + " = " + attValue.getClass() + " " + attValue.toString());
		}

		w.println("=== Server ===");
		w.println(" * servletName: " + req.getServerName());
		w.println(" * serverPort: " + req.getServerPort());
		w.println(" * servletPath: " + req.getServletPath());
		if (req.getUserPrincipal() != null) {
			w.println(" userPrincipal: " + req.getUserPrincipal().getName());
		}

		w.flush();

		w.println("=== Request Content ===");
		final BufferedReader r = req.getReader();
		String line = r.readLine();
		while (line != null) {
			w.println("> " + line);
			line = r.readLine();
		}

		w.close();
	}
}
