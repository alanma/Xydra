package org.xydra.restless.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;


/**
 * @author xamde
 * 
 */
public class ServletUtils {
	
	public static final String CONTENTTYPE_APPLICATION_XHTML_XML = "application/xhtml+xml";
	
	public static final String CONTENTTYPE_STAR_STAR = "*/*";
	
	public static final String CONTENTTYPE_TEXT_HTML = "text/html";
	
	public static final String HEADER_ACCEPT = "Accept";
	
	private static final String HEADER_REFERER = "Referer";
	
	public static final String CONTENTTYPE_APPLICATION_JSON = "application/json.";
	
	private static Logger log = LoggerFactory.getLogger(ServletUtils.class);
	
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
	 * @param req containing the Accept header
	 * @return a single chosen content-Type.
	 */
	public static String conneg(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		// parse
		Enumeration<String> enu = req.getHeaders(HEADER_ACCEPT);
		assert enu != null : "Container allows no header access";
		Map<String,Double> contentType2q = new HashMap<String,Double>();
		while(enu.hasMoreElements()) {
			String headerValue = enu.nextElement();
			String[] headerValues = headerValue.split(",");
			for(String s : headerValues) {
				parseAcceptHeaderPart(s, contentType2q);
			}
		}
		// process
		if(contentType2q.containsKey(CONTENTTYPE_APPLICATION_XHTML_XML)) {
			Double q = contentType2q.get(CONTENTTYPE_APPLICATION_XHTML_XML);
			if(q == null || q > 0) {
				return CONTENTTYPE_APPLICATION_XHTML_XML;
			}
		}
		
		if(contentType2q.containsKey(CONTENTTYPE_TEXT_HTML)) {
			Double q = contentType2q.get(CONTENTTYPE_TEXT_HTML);
			if(q == null || q > 0) {
				return CONTENTTYPE_TEXT_HTML;
			}
		}
		
		if(contentType2q.containsKey(CONTENTTYPE_STAR_STAR)) {
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
	 * @param req HttpServletRequest, @NeverNull
	 * @return @NeverNull
	 */
	public static Map<String,String> getCookiesAsMap(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		Cookie[] cookies = req.getCookies();
		Map<String,String> cookieMap = new HashMap<String,String>();
		if(cookies != null) {
			
			synchronized(cookies) {
				for(Cookie cookie : cookies) {
					synchronized(cookie) {
						String name = cookie.getName();
						String value = cookie.getValue();
						// ignoring:
						// cookie.getComment()
						// cookie.getDomain()
						// cookie.getMaxAge()
						// cookie.getPath()
						// cookie.getSecure() if true, sent only over HTTPS
						// cookie.getVersion() usually = 1
						if(cookieMap.containsKey(name)) {
							log.info("Found multiple cookies with the name '" + name
							        + "' with values. Using last one. E.g., '"
							        + cookieMap.get(name) + "' is overwritten by '" + value + "'");
						}
						cookieMap.put(name, value);
					}
				}
			}
		}
		return cookieMap;
	}
	
	/**
	 * @param req ..
	 * @return all headers of the given request as map headerName -&gt; values
	 *         (as a list).
	 */
	public static Map<String,List<String>> getHeadersAsMap(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		Enumeration<?> en = req.getHeaderNames();
		while(en.hasMoreElements()) {
			String name = (String)en.nextElement();
			Enumeration<?> valueEn = req.getHeaders(name);
			List<String> valueList = new LinkedList<String>();
			while(valueEn.hasMoreElements()) {
				valueList.add((String)valueEn.nextElement());
			}
			map.put(name, valueList);
		}
		return map;
	}
	
	/**
	 * @param req ...
	 * @return the full request URI from http up to the page name. Does not
	 *         contain any query parameters or hash fragments.
	 */
	public static final String getPageUri(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		return req.getProtocol() + req.getRemoteHost() + req.getRequestURI();
	}
	
	/**
	 * @param queryString a query string in a URL (the part after the '?')
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
	public static Map<String,SortedSet<String>> getQueryStringAsMap(@CanBeNull String queryString) {
		Map<String,SortedSet<String>> map = new HashMap<String,SortedSet<String>>();
		if(queryString == null) {
			return map;
		}
		
		String[] pairs = queryString.split("&");
		for(String pair : pairs) {
			String[] keyvalue = pair.split("=");
			if(keyvalue.length > 2) {
				// invalid pair, give up on unreliable parsing
				throw new IllegalArgumentException("Malformed query string " + queryString);
			} else {
				String encKey = keyvalue[0];
				if(encKey != null && !encKey.equals("")) {
					String key;
					try {
						key = URLDecoder.decode(encKey, Restless.JAVA_ENCODING_UTF8);
						if(key != null && !key.equals("")) {
							SortedSet<String> values = map.get(key);
							if(values == null) {
								values = new TreeSet<String>();
								map.put(key, values);
							}
							if(keyvalue.length == 2) {
								String rawValue = keyvalue[1];
								String value = URLDecoder.decode(rawValue,
								        Restless.JAVA_ENCODING_UTF8);
								values.add(value);
							} else {
								values.add("");
							}
							
						}
					} catch(UnsupportedEncodingException e) {
						throw new RuntimeException("No " + Restless.JAVA_ENCODING_UTF8
						        + " on this system?", e);
					}
					
				}
			}
		}
		return map;
	}
	
	/**
	 * @param req ..
	 * @return the referrer header url or null
	 */
	public static String getReferrerUrl(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		return req.getHeader(HEADER_REFERER);
	}
	
	/**
	 * @param req
	 * @return all get and post parameters as delivered in the servlet API - but
	 *         additionally URL-decoded
	 * @throws IllegalStateException if one of the parameters has more than one
	 *             value
	 */
	public static Map<String,String> getRequestparametersAsMap(HttpServletRequest req)
	        throws IllegalStateException {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		Map<String,String> map = new HashMap<String,String>();
		Enumeration<?> en = req.getParameterNames();
		while(en.hasMoreElements()) {
			String name = (String)en.nextElement();
			String[] values = req.getParameterValues(name);
			String value = null;
			if(values.length > 0) {
				if(values.length > 1) {
					log.warn("param '" + name + "' has more than one value, namely " + values
					        + ". Using last one.");
					value = values[values.length - 1];
				}
				value = values[0];
			}
			map.put(urldecode(name), urldecode(value));
		}
		return map;
	}
	
	public static final String getServerUri(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		return req.getProtocol() + req.getRemoteHost();
	}
	
	public static boolean hasParameter(HttpServletRequest req, String paramName) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		return req.getParameter(paramName) != null;
	}
	
	/**
	 * Sets encoding always to utf-8.
	 * 
	 * @param res ..
	 * @param status if 0 no status code is set.
	 * @param cachingInMinutes if 0 no header is set. If -1, caching is
	 *            explicitly disabled via headers (Cache-Control=no-cache;
	 *            Expires=0). Positive numbers are the time to cache the
	 *            response in minutes from now on.
	 * @param contentType
	 */
	public static void headers(HttpServletResponse res, int status, long cachingInMinutes,
	        String contentType) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		res.setCharacterEncoding(Restless.CONTENT_TYPE_CHARSET_UTF8);
		res.setContentType(contentType);
		if(status > 0) {
			res.setStatus(status);
		}
		if(cachingInMinutes == -1) {
			setNoCacheHeaders(res);
		} else if(cachingInMinutes > 0) {
			long millisSinceEpoch = System.currentTimeMillis() + (cachingInMinutes * 60 * 1000);
			// TODO test header set correctly
			res.setDateHeader("Expires", millisSinceEpoch);
		}
	}
	
	/**
	 * Set UTF8, given contentType, No caching. Send '200 OK'.
	 * 
	 * See
	 * {@link #headersXhtmlViaConneg(HttpServletRequest, HttpServletResponse, int, long)}
	 * for a variant with content-negotiation.
	 * 
	 * @param res where to send to
	 * @param contentType to be sent
	 */
	public static void headers(HttpServletResponse res, String contentType) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		// TODO this gives an error on GAE in context of URLFetch & GA
		res.setCharacterEncoding(Restless.CONTENT_TYPE_CHARSET_UTF8);
		res.setContentType(contentType);
		res.setStatus(200);
		
		setNoCacheHeaders(res);
	}
	
	public static void setNoCacheHeaders(HttpServletResponse res) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
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
		res.setHeader("Cache-Control", "no-cache");
		/* "Fri, 01 Jan 1990 00:00:00 GMT" */
		res.setDateHeader("Expires", 0);
		res.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
	}
	
	/**
	 * Compute best header to send for XHTML content.
	 * 
	 * @param req ..
	 * @param res ..
	 * @param status if 0 no header is set.
	 * @param cachingInMinutes if 0 no header is set. If -1, caching is
	 *            explicitly disabled via headers. Positive numbers are the time
	 *            to cache the response in minutes.
	 */
	public static void headersXhtmlViaConneg(HttpServletRequest req, HttpServletResponse res,
	        int status, long cachingInMinutes) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		String chosenContentType = conneg(req);
		headers(res, status, cachingInMinutes, chosenContentType);
	}
	
	/**
	 * @param req
	 * @return true if request is just an http://-request and not an https://
	 *         request.
	 */
	public static boolean isInsecureHttpRequest(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		return req.getScheme().equals("http");
	}
	
	/**
	 * @param req
	 * @return true if request is to root URL '/', may also have query
	 *         parameters
	 */
	public static boolean isRequestToRoot(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		String path = req.getPathInfo();
		return path == null || path.equals("") || path.equals("/");
	}
	
	/**
	 * @param req
	 * @return true if request is a secure https://-request
	 */
	public static boolean isSecureHttpsRequest(HttpServletRequest req) {
		/*
		 * TODO is the request already thread-safe or not?
		 */
		
		return req.getScheme().equals("https");
	}
	
	/**
	 * @param paramValue can be null
	 * @return true if value is neither null nor an empty string
	 */
	public static boolean isSet(String paramValue) {
		return paramValue != null && !paramValue.equals("");
	}
	
	private static void parseAcceptHeaderPart(String headerValue, Map<String,Double> contentType2q) {
		String[] parts = headerValue.split(";");
		String contentDef = parts[0];
		if(parts.length > 1) {
			String qs = parts[1].trim();
			if(!qs.startsWith("q=")) {
				log.warn("q-value '" + qs + "' wrong in Accept header '" + headerValue + "'");
			} else {
				qs = qs.substring(2);
				try {
					double q = Double.parseDouble(qs);
					contentType2q.put(contentDef, q);
				} catch(NumberFormatException e) {
					log.warn("q-value '" + qs + "' not parsable as double in Accept header '"
					        + headerValue + "'");
				}
			}
		} else {
			contentType2q.put(contentDef, null);
		}
	}
	
	public static Map<String,String> parseQueryString(String q) {
		Map<String,String> map = new HashMap<String,String>();
		String[] pairs = q.split("\\&");
		
		for(String s : pairs) {
			String[] parts = s.split("=");
			String key = parts[0];
			String value = parts.length > 1 ? parts[1] : null;
			map.put(key, value);
		}
		return map;
	}
	
	public static String urldecode(String encoded) {
		try {
			return URLDecoder.decode(encoded, "utf-8");
		} catch(UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * @param b
	 * @return true if b is (after trim and lowecasing) one of 'true','yes' or
	 *         'on'
	 */
	public static boolean toBoolean(String b) {
		if(!isSet(b)) {
			return false;
		}
		String c = b.trim().toLowerCase();
		if(c.equals("true") || c.equals("yes") || c.equals("on")) {
			return true;
		}
		return false;
	}
	
}
