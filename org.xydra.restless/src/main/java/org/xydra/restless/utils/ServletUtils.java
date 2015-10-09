package org.xydra.restless.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.ThreadSafe;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;

/**
 * @author xamde
 *
 */

@ThreadSafe
public class ServletUtils {

	public static final String CONTENTTYPE_APPLICATION_XHTML_XML = "application/xhtml+xml";

	public static final String CONTENTTYPE_STAR_STAR = "*/*";

	public static final String CONTENTTYPE_TEXT_HTML = "text/html";

	public static final String HEADER_ACCEPT = "Accept";

	private static final String HEADER_REFERER = "Referer";

	public static final String CONTENTTYPE_APPLICATION_JSON = "application/json.";

	private static Logger log = LoggerFactory.getThreadSafeLogger(ServletUtils.class);

	/**
	 * If the Accept header explicitly contains application/xhtml+xml (with
	 * either no "q" parameter or a positive "q" value) deliver the document
	 * using that media type.
	 *
	 * If the Accept header explicitly contains text/html (with either no "q"
	 * parameter or a positive "q" value) deliver the document using that media
	 * type.
	 *
	 * If the accept header contains "star/star" (a convention some user agents
	 * use to indicate that they will accept anything), deliver the document
	 * using text/html.
	 *
	 * @param req
	 *            containing the Accept header @NeverNull
	 * @return a single chosen content-Type.
	 */
	public static String conneg(@NeverNull final HttpServletRequest req) {
		// parse
		final Enumeration<String> enu = req.getHeaders(HEADER_ACCEPT);
		assert enu != null : "Container allows no header access";
		final Map<String, Double> contentType2q = new HashMap<String, Double>();
		while (enu.hasMoreElements()) {
			final String headerValue = enu.nextElement();
			final String[] headerValues = headerValue.split(",");
			for (final String s : headerValues) {
				parseAcceptHeaderPart(s, contentType2q);
			}
		}
		// process
		if (contentType2q.containsKey(CONTENTTYPE_APPLICATION_XHTML_XML)) {
			final Double q = contentType2q.get(CONTENTTYPE_APPLICATION_XHTML_XML);
			if (q == null || q > 0) {
				return CONTENTTYPE_APPLICATION_XHTML_XML;
			}
		}

		if (contentType2q.containsKey(CONTENTTYPE_TEXT_HTML)) {
			final Double q = contentType2q.get(CONTENTTYPE_TEXT_HTML);
			if (q == null || q > 0) {
				return CONTENTTYPE_TEXT_HTML;
			}
		}

		if (contentType2q.containsKey(CONTENTTYPE_STAR_STAR)) {
			return CONTENTTYPE_TEXT_HTML;
		}

		// evil fall-back:
		log.warn("Could not extract meaningful wish from accept header, using oldschool html");
		return CONTENTTYPE_TEXT_HTML;
	}

	/**
	 * Turn all cookies that the request contains into a map, cookie name as
	 * key, cookie value as map value.
	 *
	 * @param req
	 *            HttpServletRequest, @NeverNull
	 * @return @NeverNull
	 */
	public static Map<String, String> getCookiesAsMap(@NeverNull final HttpServletRequest req) {
		final Cookie[] cookies = req.getCookies();
		final Map<String, String> cookieMap = new HashMap<String, String>();
		if (cookies != null) {

			for (final Cookie cookie : cookies) {
				final String name = cookie.getName();
				final String value = cookie.getValue();
				// ignoring:
				// cookie.getComment()
				// cookie.getDomain()
				// cookie.getMaxAge()
				// cookie.getPath()
				// cookie.getSecure() if true, sent only over HTTPS
				// cookie.getVersion() usually = 1
				if (cookieMap.containsKey(name)) {
					log.info("Found multiple cookies with the name '" + name
							+ "' with values. Using last one. E.g., '" + cookieMap.get(name)
							+ "' is overwritten by '" + value + "'");
				}
				cookieMap.put(name, value);

			}
		}
		return cookieMap;
	}

	/**
	 * @param req
	 *            .. @NeverNull
	 * @return all headers of the given request as map headerName -&gt; values
	 *         (as a list).
	 */
	public static Map<String, List<String>> getHeadersAsMap(@NeverNull final HttpServletRequest req) {
		final Map<String, List<String>> map = new HashMap<String, List<String>>();
		final Enumeration<?> en = req.getHeaderNames();
		while (en.hasMoreElements()) {
			final String name = (String) en.nextElement();
			final Enumeration<?> valueEn = req.getHeaders(name);
			final List<String> valueList = new LinkedList<String>();
			while (valueEn.hasMoreElements()) {
				valueList.add((String) valueEn.nextElement());
			}
			map.put(name, valueList);
		}
		return map;
	}

	/**
	 * @param req
	 *            ... @NeverNull
	 * @return the full request URI from http up to the page name. Does not
	 *         contain any query parameters or hash fragments.
	 */
	public static final String getPageUri(@NeverNull final HttpServletRequest req) {
		return req.getProtocol() + req.getRemoteHost() + req.getRequestURI();
	}

	/**
	 * @param queryString
	 *            a query string in a URL (the part after the '?') @CanBeNull
	 * @return a Map that contains key=value from the query string. Multiple
	 *         values for the same key are put in order of appearance in the
	 *         list. Duplicate values are omitted.
	 *
	 *         The members of the {@link SortedSet} may be the empty string if
	 *         the query string was just 'a=&b=foo'.
	 *
	 *         Encoding UTF-8 is used for URLDecoding the key and value strings.
	 *
	 *         Keys and values get URL-decoded.
	 */
	public static Map<String, SortedSet<String>> getQueryStringAsMap(@CanBeNull final String queryString) {
		final Map<String, SortedSet<String>> map = new HashMap<String, SortedSet<String>>();
		if (queryString == null) {
			return map;
		}

		final String[] pairs = queryString.split("[&;]");
		for (final String pair : pairs) {
			final int equalSignIndex = pair.indexOf("=");
			if (equalSignIndex > 0) {
				// parse as key-value
				final String first = pair.substring(0, equalSignIndex);
				final String second = pair.substring(equalSignIndex + 1, pair.length());
				addRawKeyValue(map, first, second);
			} else {
				// parse as key-only
				addRawKeyValue(map, pair, null);
			}
		}
		return map;
	}

	/**
	 * @param map
	 * @param rawKey
	 * @param rawValue
	 *            TODO can contain commas
	 */
	private static void addRawKeyValue(final Map<String, SortedSet<String>> map, final String rawKey,
			final String rawValue) {
		assert rawKey != null;
		assert !rawKey.equals("");

		String key;
		try {
			key = URLDecoder.decode(rawKey, Restless.JAVA_ENCODING_UTF8);
			if (key != null && !key.equals("")) {
				SortedSet<String> values = map.get(key);
				if (values == null) {
					values = new TreeSet<String>();
					map.put(key, values);
				}
				if (rawValue != null) {
					final String value = URLDecoder.decode(rawValue, Restless.JAVA_ENCODING_UTF8);
					values.add(value);
				} else {
					values.add("");
				}

			}
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("No " + Restless.JAVA_ENCODING_UTF8 + " on this system?", e);
		}
	}

	/**
	 * @param req
	 *            .. @NeverNull
	 * @return the referrer header url or null
	 */
	public static String getReferrerUrl(@NeverNull final HttpServletRequest req) {
		return req.getHeader(HEADER_REFERER);
	}

	/**
	 * @param req @NeverNull
	 * @return all get and post parameters as delivered in the servlet API - but
	 *         additionally URL-decoded
	 * @throws IllegalStateException
	 *             if one of the parameters has more than one value
	 */
	public static Map<String, String> getRequestparametersAsMap(@NeverNull final HttpServletRequest req)
			throws IllegalStateException {

		final Map<String, String> map = new HashMap<String, String>();
		final Enumeration<?> en = req.getParameterNames();
		while (en.hasMoreElements()) {
			final String name = (String) en.nextElement();
			final String[] values = req.getParameterValues(name);
			String value = null;
			if (values.length > 0) {
				if (values.length > 1) {
					log.warn("param '" + name + "' has more than one value, namely "
							+ Arrays.toString(values) + ". Using last one.");
					value = values[values.length - 1];
				}
				value = values[0];
			}
			map.put(urldecode(name), urldecode(value));
		}
		return map;
	}

	/**
	 * @param req @NeverNull
	 * @return complete server URI (protocol+host), not port
	 */
	public static final String getServerUri(@NeverNull final HttpServletRequest req) {
		return req.getProtocol() + req.getRemoteHost();
	}

	/**
	 * @param req @NeverNull
	 * @param paramName
	 * @CanBeNull
	 * @return true if parameter name not null and value defined
	 */
	public static boolean hasParameter(@NeverNull final HttpServletRequest req,
			@CanBeNull final String paramName) {
		if (paramName == null) {
			return false;
		}
		return req.getParameter(paramName) != null;
	}

	/**
	 * Sets encoding always to utf-8.
	 *
	 * @param res
	 *            .. @NeverNull
	 * @param status
	 *            if 0 no status code is set. @NeverNull
	 * @param cachingInMinutes
	 *            if 0 no header is set. If -1, caching is explicitly disabled
	 *            via headers (Cache-Control=no-cache; Expires=0). Positive
	 *            numbers are the time to cache the response in minutes from now
	 *            on.
	 * @param contentType @NeverNull
	 */
	public static void headers(@NeverNull final HttpServletResponse res, final int status,
			final long cachingInMinutes, @CanBeNull final String contentType) {
		res.setCharacterEncoding(Restless.CONTENT_TYPE_CHARSET_UTF8);
		res.setContentType(contentType);
		if (status > 0) {
			res.setStatus(status);
		}
		if (cachingInMinutes == -1) {
			setNoCacheHeaders(res);
		} else if (cachingInMinutes > 0) {
			final long millisSinceEpoch = System.currentTimeMillis() + cachingInMinutes * 60 * 1000;
			// TODO test header set correctly
			res.setDateHeader("Expires", millisSinceEpoch);
			// HTTP 1.1
			res.setHeader("Cache-Control", "private");
		}
	}

	/**
	 * Set UTF8, given contentType, No caching. Send '200 OK'.
	 *
	 * See
	 * {@link #headersXhtmlViaConneg(HttpServletRequest, HttpServletResponse, int, long)}
	 * for a variant with content-negotiation.
	 *
	 * @param res
	 *            where to send to @NeverNull
	 * @param contentType
	 *            to be sent @CanBeNull
	 */
	public static void headers(@NeverNull final HttpServletResponse res, @CanBeNull final String contentType) {
		// TODO this gives an error on GAE in context of URLFetch & GA
		res.setCharacterEncoding(Restless.CONTENT_TYPE_CHARSET_UTF8);
		res.setContentType(contentType);
		res.setStatus(200);

		setNoCacheHeaders(res);
	}

	public static void setNoCacheHeaders(@NeverNull final HttpServletResponse res) {

		/*
		 * Pragma:
		 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.32
		 *
		 * When the no-cache directive is present in a request message, an
		 * application SHOULD forward the request toward the origin server even
		 * if it has a cached copy of what is being requested. This pragma
		 * directive has the same semantics as the no-cache cache-directive (see
		 * section 14.9) and is defined here for backward compatibility with
		 * HTTP/1.0. Clients SHOULD include both header fields when a no-cache
		 * request is sent to a server not known to be HTTP/1.1 compliant.
		 */
		res.setHeader("Pragma", "no-cache");

		// HTTP 1.1
		res.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		/* "Fri, 01 Jan 1990 00:00:00 GMT" */
		res.setDateHeader("Expires", 0);
		res.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
	}

	/**
	 * Compute best header to send for XHTML content.
	 *
	 * @param req
	 *            .. @NeverNull
	 * @param res
	 *            ..@NeverNull
	 * @param status
	 *            if 0 no header is set. @NeverNull
	 * @param cachingInMinutes
	 *            if 0 no header is set. If -1, caching is explicitly disabled
	 *            via headers. Positive numbers are the time to cache the
	 *            response in minutes. @NeverNull
	 */
	public static void headersXhtmlViaConneg(@NeverNull final HttpServletRequest req,
			@NeverNull final HttpServletResponse res, final int status, final long cachingInMinutes) {
		final String chosenContentType = conneg(req);
		headers(res, status, cachingInMinutes, chosenContentType);
	}

	/**
	 * @param req @NeverNull
	 * @return true if request is just an http://-request and not an https://
	 *         request.
	 */
	public static boolean isInsecureHttpRequest(@NeverNull final HttpServletRequest req) {

		return req.getScheme().equals("http");
	}

	/**
	 * @param req @NeverNull
	 * @return true if request is to root URL '/', may also have query
	 *         parameters
	 */
	public static boolean isRequestToRoot(@NeverNull final HttpServletRequest req) {

		final String path = req.getPathInfo();
		return path == null || path.equals("") || path.equals("/");
	}

	/**
	 * @param req @NeverNull
	 * @return true if request is a secure https://-request
	 */
	public static boolean isSecureHttpsRequest(@NeverNull final HttpServletRequest req) {

		return req.getScheme().equals("https");
	}

	/**
	 * @param paramValue
	 * @CanBeNull
	 * @return true if value is neither null nor an empty string
	 */
	public static boolean isSet(@CanBeNull final String paramValue) {
		return paramValue != null && !paramValue.equals("");
	}

	/**
	 *
	 * @param headerValue @NeverNull
	 * @param contentType2q @NeverNull
	 */
	private static void parseAcceptHeaderPart(@NeverNull final String headerValue,
			@NeverNull final Map<String, Double> contentType2q) {
		final String[] parts = headerValue.split(";");
		final String contentDef = parts[0];
		if (parts.length > 1) {
			String qs = parts[1].trim();
			if (!qs.startsWith("q=")) {
				log.warn("q-value '" + qs + "' wrong in Accept header '" + headerValue + "'");
			} else {
				qs = qs.substring(2);
				try {
					final double q = Double.parseDouble(qs);
					contentType2q.put(contentDef, q);
				} catch (final NumberFormatException e) {
					log.warn("q-value '" + qs + "' not parsable as double in Accept header '"
							+ headerValue + "'");
				}
			}
		} else {
			contentType2q.put(contentDef, null);
		}
	}

	/**
	 *
	 * @param q @NeverNull
	 * @return the query string as a map: parameter name maps to parameter
	 *         value. If the URL contains the same key twice, later values
	 *         override earlier values.
	 */
	public static Map<String, String> parseQueryString(@NeverNull final String q) {
		final Map<String, String> map = new HashMap<String, String>();
		final String[] pairs = q.split("\\&");

		for (final String s : pairs) {
			final String[] parts = s.split("=");
			final String key = parts[0];
			final String value = parts.length > 1 ? parts[1] : null;
			map.put(key, value);
		}
		return map;
	}

	/**
	 *
	 * @param encoded @NeverNull
	 * @return url decoded string
	 */
	@RunsInGWT(false)
	public static String urldecode(@NeverNull final String encoded) {
		try {
			return URLDecoder.decode(encoded, "utf-8");
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * @param b
	 * @CanBeNull
	 * @return true if b is (after trim and lower-casing) one of 'true','yes' or
	 *         'on'
	 */
	public static boolean toBoolean(@CanBeNull final String b) {
		if (!isSet(b)) {
			return false;
		}
		assert b != null;
		final String c = b.trim().toLowerCase();
		if (c.equals("true") || c.equals("yes") || c.equals("on")) {
			return true;
		}
		return false;
	}

}
