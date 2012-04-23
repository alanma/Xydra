package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class CookieUtils {
	
	private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);
	
	/**
	 * @param req ..
	 * @param name of the cookie
	 * @return the current cookie value with given name in the given request or
	 *         null
	 */
	public static String getCookie(HttpServletRequest req, String name) {
		if(req.getCookies() == null) {
			return null;
		}
		for(Cookie cookie : req.getCookies()) {
			if(cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
	/**
	 * @param req ..
	 * @return a list of (potentially duplicate) cookie names
	 */
	public static List<String> listCookieNames(HttpServletRequest req) {
		List<String> cookieNames = new LinkedList<String>();
		for(Cookie cookie : req.getCookies()) {
			cookieNames.add(cookie.getName());
		}
		return cookieNames;
	}
	
	/**
	 * Dumps cookies as a piece of HTML code
	 * 
	 * @param req where to read cookies
	 * @param w there to dump the cookie information
	 * @throws IOException ...
	 */
	public static void dumpCookies(HttpServletRequest req, Writer w) throws IOException {
		w.write("<style>" +
		
		"td, th { border: 1px solid black; margin: 0px; padding: 2px;}" +
		
		"</style>");
		w.write("<table>"
		
		+ "<tr>" + "<th>Domain</th>" + "<th>Path</th>" + "<th>Secure?</th>" + "<th>Name</th>"
		        + "<th>Value</th>" + "<th>MaxAge</th>" + "<th>Comment</th>" + "<th>Version</th>"
		        + "</tr>");
		for(Cookie cookie : req.getCookies()) {
			
			w.write("<tr>"
			
			+ "<td>" + cookie.getDomain() + "</td>"
			
			+ "<td>" + cookie.getPath() + "</td>"
			
			+ "<td>" + cookie.getSecure() + "</td>"
			
			+ "<td>" + cookie.getName() + "</td>"
			
			+ "<td>" + cookie.getValue() + "</td>"
			
			+ "<td>" + cookie.getMaxAge() + "</td>"
			
			+ "<td>" + cookie.getComment() + "</td>"
			
			+ "<td>" + cookie.getVersion() + "</td>"
			
			+ "</tr>");
		}
		w.write("</table>");
	}
	
	/**
	 * @param req ...
	 * @param name of cookie
	 * @return true if the request contains a cookie with the given name which
	 *         is non-empty
	 */
	public static boolean hasCookie(HttpServletRequest req, String name) {
		if(req.getCookies() == null) {
			return false;
		}
		for(Cookie cookie : req.getCookies()) {
			if(cookie.getName().equals(name)) {
				if(cookie.getValue() == null) {
					log.warn("cookie '" + name + "' is present but contains null");
					return false;
				}
				if(cookie.getValue().equals("")) {
					log.warn("cookie '" + name + "' is present but contains empty string");
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove the cookie with the given name from the users browser, if present.
	 * 
	 * @param req ..
	 * @param res ..
	 * @param name of cookie to be removed
	 * @param domain can be null. Should in any case be the same value as was
	 *            used to set the cookie.
	 */
	public static void removeCookieIfPresent(HttpServletRequest req, HttpServletResponse res,
	        String name, String domain) {
		assert req != null;
		if(hasCookie(req, name)) {
			setCookie(res, name, "", domain, null, 0);
		}
	}
	
	/**
	 * @param res ..
	 * @param name name of the cookie
	 * @param value value of the cookie
	 * @param domain can be null. If non null, RFC 2109 'An explicitly specified
	 *            domain must always start with a dot.' Example:
	 *            '.www.example.com' or '.example.com'
	 * @param comment can be null
	 * @param maxAge in seconds
	 */
	public static void setCookie(HttpServletResponse res, String name, String value, String domain,
	        String comment, int maxAge) {
		if(name == null || name.equals("")) {
			throw new IllegalArgumentException("name is null or empty");
		}
		if(value == null) {
			throw new IllegalArgumentException("value is null");
		}
		Cookie cookie = new Cookie(name, value);
		/*
		 * A session cookie is created when no Expires directive is provided
		 * when the cookie is created. Pos. value: expiry time. Neg value:
		 * session cookie. 0 = delete.
		 */
		cookie.setMaxAge(maxAge);
		if(domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setPath("/");
		if(comment != null) {
			cookie.setComment(comment);
		}
		res.addCookie(cookie);
	}
	
}
