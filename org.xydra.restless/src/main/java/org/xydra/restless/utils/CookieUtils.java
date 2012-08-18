package org.xydra.restless.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


public class CookieUtils {
	
	private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);
	
	/*
	 * TODO A lock on req.getCookies() might be appropriate even if req is only
	 * used once, for example if the same cookies are used in multiple requests.
	 * 
	 * The important questions are: Are cookie instances shared between
	 * different requests? Are whole arrays of cookies shared between different
	 * requests?
	 */
	
	/**
	 * @param req .. @NeverNull
	 * @param name of the cookie @NeverNull
	 * @return the current cookie value with given name in the given request or
	 *         null
	 */
	public static String getCookie(@NeverNull HttpServletRequest req, @NeverNull String name) {
		if(req.getCookies() == null) {
			return null;
		}
		
		Cookie[] cookies = req.getCookies();
		if(cookies != null) {
			synchronized(cookies) {
				for(Cookie cookie : cookies) {
					
					if(cookie != null) {
						synchronized(cookie) {
							if(cookie.getName().equals(name)) {
								return cookie.getValue();
							}
						}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @param req .. @NeverNull
	 * @return a list of (potentially duplicate) cookie names
	 */
	public static List<String> listCookieNames(@NeverNull HttpServletRequest req) {
		
		List<String> cookieNames = new LinkedList<String>();
		
		Cookie[] cookies = req.getCookies();
		
		/*
		 * no synchronization on cookies is necessary here (even if they're
		 * shared), since we're only reading the cookies name, which cannot be
		 * changed after its creation.
		 */
		
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				
				assert cookie != null;
				cookieNames.add(cookie.getName());
				
			}
		}
		return cookieNames;
	}
	
	/**
	 * Dumps cookies as a piece of HTML code
	 * 
	 * @param req where to read cookies @NeverNull
	 * @param w there to dump the cookie information @NeverNull
	 * @throws IOException ...
	 */
	public static void dumpCookies(@NeverNull HttpServletRequest req, @NeverNull Writer w)
	        throws IOException {
		w.write("<style>" +
		
		"td, th { border: 1px solid black; margin: 0px; padding: 2px;}" +
		
		"</style>");
		w.write("<table>"
		
		+ "<tr>" + "<th>Domain</th>" + "<th>Path</th>" + "<th>Secure?</th>" + "<th>Name</th>"
		        + "<th>Value</th>" + "<th>MaxAge</th>" + "<th>Comment</th>" + "<th>Version</th>"
		        + "</tr>");
		
		Cookie[] cookies = req.getCookies();
		
		if(cookies != null) {
			synchronized(cookies) {
				for(Cookie cookie : cookies) {
					
					if(cookie != null) {
						synchronized(cookie) {
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
					}
				}
			}
		}
		w.write("</table>");
	}
	
	/**
	 * @param req ... @NeverNull
	 * @param name of cookie @NeverNull
	 * @return true if the request contains a cookie with the given name which
	 *         is non-empty
	 */
	public static boolean hasCookie(@NeverNull HttpServletRequest req, @NeverNull String name) {
		if(req.getCookies() == null) {
			return false;
		}
		
		Cookie[] cookies = req.getCookies();
		synchronized(cookies) {
			
			for(Cookie cookie : req.getCookies()) {
				
				if(cookie != null) {
					
					synchronized(cookie) {
						if(cookie.getName().equals(name)) {
							if(cookie.getValue() == null) {
								log.warn("cookie '" + name + "' is present but contains null");
								return false;
							}
							if(cookie.getValue().equals("")) {
								log.warn("cookie '" + name
								        + "' is present but contains empty string");
								return false;
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Remove the cookie with the given name from the users browser, if present.
	 * 
	 * @param req .. @NeverNull
	 * @param res .. @NeverNull
	 * @param name of cookie to be removed @NeverNull
	 * @param domain can be null. Should in any case be the same value as was
	 *            used to set the cookie. @CanBeNull
	 */
	public static void removeCookieIfPresent(@NeverNull HttpServletRequest req,
	        @NeverNull HttpServletResponse res, @NeverNull String name, @CanBeNull String domain) {
		assert req != null;
		
		Cookie[] cookies = req.getCookies();
		
		if(cookies != null) {
			synchronized(cookies) {
				/*
				 * although the following methods are technically already
				 * synchronized on cookies, we need to make sure that the
				 * cookies are not changed in between the methods. This is the
				 * reason for this synchronized-block.
				 */
				
				if(hasCookie(req, name)) {
					setCookie(res, name, "", domain, null, 0);
				}
			}
		}
	}
	
	/**
	 * @param res .. @NeverNull
	 * @param name name of the cookie @NeverNull
	 * @param value value of the cookie @NeverNull
	 * @param domain can be null. If non null, RFC 2109 'An explicitly specified
	 *            domain must always start with a dot.' Example:
	 *            '.www.example.com' or '.example.com' @CanBeNull
	 * @param comment can be null @CanBeNull
	 * @param maxAge in seconds @NeverNull
	 */
	public static void setCookie(@NeverNull HttpServletResponse res, @NeverNull String name,
	        @NeverNull String value, @CanBeNull String domain, @CanBeNull String comment,
	        @NeverNull int maxAge) {
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
