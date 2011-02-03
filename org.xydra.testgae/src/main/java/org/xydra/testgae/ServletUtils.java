package org.xydra.testgae;

import javax.servlet.http.HttpServletResponse;


public class ServletUtils {
	
	public static void headers(HttpServletResponse res, String contentType) {
		res.setContentType(contentType);
		res.setCharacterEncoding("utf-8");
		res.setStatus(200);
	}
	
}
