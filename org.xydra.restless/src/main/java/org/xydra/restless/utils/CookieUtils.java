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
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class CookieUtils {

	private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);

	/**
	 * A long value that is tolerated by most (all?) browsers
	 */
	public static final int MANY_DAYS = 300 * 24 * 60 * 60;

	/**
	 * Dumps cookies as a piece of HTML code
	 *
	 * @param req
	 *            where to read cookies @NeverNull
	 * @param w
	 *            there to dump the cookie information @NeverNull
	 * @throws IOException
	 *             ...
	 */
	public static void dumpCookies(@NeverNull final HttpServletRequest req, @NeverNull final Writer w)
			throws IOException {
		w.write("<style>" +

		"td, th { border: 1px solid black; margin: 0px; padding: 2px;}" +

		"</style>");
		w.write("<table>"

		+ "<tr>" + "<th>Domain</th>" + "<th>Path</th>" + "<th>Secure?</th>" + "<th>Name</th>"
				+ "<th>Value</th>" + "<th>MaxAge</th>" + "<th>Comment</th>" + "<th>Version</th>"
				+ "</tr>");

		final Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (final Cookie cookie : cookies) {

				if (cookie != null) {
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

		w.write("</table>");
	}

	/**
	 * @param req
	 *            .. @NeverNull
	 * @param name
	 *            of the cookie @NeverNull
	 * @return the current cookie value with given name in the given request or
	 *         null
	 */
	public static String getCookie(@NeverNull final HttpServletRequest req, @NeverNull final String name) {
		if (req.getCookies() == null) {
			return null;
		}

		final Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (final Cookie cookie : cookies) {

				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * @param req
	 * @param cookieName
	 * @return the {@link Cookie} object for the given cookie name
	 */
	public static Cookie getCookieObject(final HttpServletRequest req, final String cookieName) {
		if (req.getCookies() == null) {
			return null;
		}
		for (final Cookie cookie : req.getCookies()) {
			if (cookie.getName().equals(cookieName)) {
				return cookie;
			}
		}
		return null;
	}

	/**
	 * @param req
	 *            ... @NeverNull
	 * @param name
	 *            of cookie @NeverNull
	 * @return true if the request contains a cookie with the given name which
	 *         is non-empty
	 */
	public static boolean hasCookie(@NeverNull final HttpServletRequest req, @NeverNull final String name) {
		if (req.getCookies() == null) {
			return false;
		}

		final Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (final Cookie cookie : cookies) {

				if (cookie != null) {

					if (cookie.getName().equals(name)) {
						if (cookie.getValue() == null) {
							log.warn("cookie '" + name + "' is present but contains null");
							return false;
						}
						if (cookie.getValue().equals("")) {
							log.warn("cookie '" + name + "' is present but contains empty string");
							return false;
						}
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * @param req
	 *            .. @NeverNull
	 * @return a list of (potentially duplicate) cookie names
	 */
	public static List<String> listCookieNames(@NeverNull final HttpServletRequest req) {

		final List<String> cookieNames = new LinkedList<String>();

		final Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			for (final Cookie cookie : cookies) {

				assert cookie != null;
				cookieNames.add(cookie.getName());

			}
		}
		return cookieNames;
	}

	/**
	 * Remove the cookie with the given name from the users browser, if present.
	 *
	 * @param req
	 *            .. @NeverNull
	 * @param res
	 *            .. @NeverNull
	 * @param name
	 *            of cookie to be removed @NeverNull
	 * @param domain
	 *            can be null. Should in any case be the same value as was used
	 *            to set the cookie. @CanBeNull
	 */
	public static void removeCookieIfPresent(@NeverNull final HttpServletRequest req,
			@NeverNull final HttpServletResponse res, @NeverNull final String name, @CanBeNull final String domain) {
		assert req != null;

		final Cookie[] cookies = req.getCookies();

		if (cookies != null) {
			if (hasCookie(req, name)) {
				setCookie(res, name, "", domain, null, 0);
			}
		}

	}

	/**
	 * @param res
	 *            ..
	 * @param name
	 *            name of the cookie
	 * @param value
	 *            value of the cookie
	 * @param domain
	 *            can be null. If non null, RFC 2109 'An explicitly specified
	 *            domain must always start with a dot.' Example:
	 *            '.app.calpano.com' or '.calpano.com'
	 * @param comment
	 *            can be null
	 * @param persist
	 *            if true, creates a long-living cookie that expires after
	 *            {@value #MANY_DAYS}. If false, creates a session cookie, which
	 *            is deleted when the user closes the browser.
	 */
	public static void setCookie(final HttpServletResponse res, final String name, final String value, final String domain,
			final String comment, final boolean persist) {
		setCookie(res, name, value, domain, comment, persist ? MANY_DAYS : -1);
	}

	/**
	 * @param res
	 *            .. @NeverNull
	 * @param name
	 *            name of the cookie @NeverNull
	 * @param value
	 *            value of the cookie @NeverNull
	 * @param domain
	 *            can be null. If non null, RFC 2109 'An explicitly specified
	 *            domain must always start with a dot.' Example:
	 *            '.www.example.com' or '.example.com' @CanBeNull
	 * @param comment
	 *            can be null @CanBeNull
	 * @param maxAge
	 *            in seconds @NeverNull
	 */
	public static void setCookie(@NeverNull final HttpServletResponse res, @NeverNull final String name,
			@NeverNull final String value, @CanBeNull final String domain, @CanBeNull final String comment, final int maxAge) {
		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("name is null or empty");
		}
		if (value == null) {
			throw new IllegalArgumentException("value is null");
		}
		final Cookie cookie = new Cookie(name, value);
		/*
		 * A session cookie is created when no Expires directive is provided
		 * when the cookie is created. Pos. value: expiry time. Neg value:
		 * session cookie. 0 = delete.
		 */
		cookie.setMaxAge(maxAge);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		cookie.setPath("/");
		if (comment != null) {
			cookie.setComment(comment);
		}

		res.addCookie(cookie);
	}

	/**
	 * Set the cookie only if not present yet
	 *
	 * @param req
	 *            ..
	 * @param res
	 *            ..
	 * @param name
	 *            value of the cookie
	 * @param value
	 *            value of the cookie
	 * @param domain
	 *            can be null. If non null, RFC 2109 'An explicitly specified
	 *            domain must always start with a dot.' Example:
	 *            '.www.example.com' or '.example.com'
	 * @param comment
	 *            can be null
	 */
	public static void setCookieIfNecessary(final HttpServletRequest req, final HttpServletResponse res,
			final String name, final String value, final String domain, final String comment) {
		final String currentCuid = getCookie(req, name);
		if (currentCuid == null || !currentCuid.equals(value)) {
			setCookie(res, name, value, domain, comment, true);
		}
	}

}
