package org.xydra.restless.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class ServletUtils {
	
	private static Logger log = LoggerFactory.getLogger(ServletUtils.class);
	
	/**
	 * Send '200 OK' with given contentType, No caching.
	 * 
	 * @param res where to send to
	 * @param contentType to be sent
	 */
	public static void headers(HttpServletResponse res, String contentType) {
		res.setHeader("Pragma", "no-cache");
		res.setHeader("Expires", "Fri, 01 Jan 1990 00:00:00 GMT");
		res.setContentType(contentType);
		res.setCharacterEncoding("utf-8");
		res.setStatus(200);
	}
	
	/**
	 * @param req ...
	 * @return the full request URI from http up to the page name. Does not
	 *         contain any query parameters or hash fragments.
	 */
	public static final String getPageUri(HttpServletRequest req) {
		return req.getProtocol() + req.getRemoteHost() + req.getRequestURI();
	}
	
	public static final String getServerUri(HttpServletRequest req) {
		return req.getProtocol() + req.getRemoteHost();
	}
	
	/**
	 * Turn all cookies that the request contains into a map, cookie name as
	 * key, cookie value as map value.
	 * 
	 * @param req HttpServletRequest, never null
	 * @return never null
	 */
	public static Map<String,String> getCookiesAsMap(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		Map<String,String> cookieMap = new HashMap<String,String>();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
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
					log.warn("Found multiple cookies with the name '" + name
					        + "' with values, e.g., '" + cookieMap.get(name) + "' or '" + value
					        + "'");
				}
				cookieMap.put(name, value);
			}
		}
		return cookieMap;
	}
	
	/**
	 * @param queryString a query string in a URL (the part after the '?')
	 * @return a Map that contains key=value from the query string. Multiple
	 *         values for the same key are put in order of appearance in the
	 *         list. Duplicate values are omitted.
	 * 
	 *         The members of the {@link SortedSet} may be null if the query
	 *         string was just 'a=&b=foo'.
	 * 
	 *         Encoding UTF-8 is used for URLDecoding the key and value strings.
	 * 
	 *         Keys and values get URL-decoded.
	 */
	public static Map<String,SortedSet<String>> getQueryStringAsMap(String queryString) {
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
				String key;
				try {
					key = URLDecoder.decode(encKey, "utf-8");
					SortedSet<String> values = map.get(key);
					if(values == null) {
						values = new TreeSet<String>();
						map.put(key, values);
					}
					if(keyvalue.length == 2) {
						String rawValue = keyvalue[1];
						String value = URLDecoder.decode(rawValue, "utf-8");
						values.add(value);
					} else {
						values.add(null);
					}
				} catch(UnsupportedEncodingException e) {
					throw new RuntimeException("No utf-8 on this system?", e);
				}
			}
		}
		return map;
	}
	
	/**
	 * @return all get and post parameters as delivered in the servlet API
	 * @throws IllegalStateException if one of the parameters has more than one
	 *             value
	 */
	public static Map<String,String> getRequestparametersAsMap(HttpServletRequest req)
	        throws IllegalStateException {
		Map<String,String> map = new HashMap<String,String>();
		@SuppressWarnings("rawtypes")
		Enumeration en = req.getParameterNames();
		while(en.hasMoreElements()) {
			String name = (String)en.nextElement();
			String[] values = req.getParameterValues(name);
			if(values.length > 1) {
				throw new IllegalStateException("param '" + name
				        + "' has more than one value, namely " + values);
			}
			String value = null;
			if(values.length > 0) {
				value = values[0];
			}
			map.put(name, value);
		}
		return map;
	}
	
}
