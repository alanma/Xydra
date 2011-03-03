package org.xydra.restless.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ServletUtils {
	
	public static void headers(HttpServletResponse res, String contentType) {
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
	
}
