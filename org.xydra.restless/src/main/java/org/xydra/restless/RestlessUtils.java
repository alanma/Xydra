package org.xydra.restless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xydra.annotations.NeverNull;

/**
 * Publicly exposed utility methods.
 * 
 * @author xamde
 */
public class RestlessUtils {

	/**
	 * @param req
	 *            @NeverNull
	 * @param pathTemplate
	 *            @NeverNull
	 * @return a single map of key-value pairs extracted from the path-part of
	 *         the request-URI
	 */
	public static Map<String, String> getUrlParametersAsMap(@NeverNull HttpServletRequest req,
			@NeverNull PathTemplate pathTemplate) {
		Map<String, String> urlParameter = new HashMap<String, String>();
		String urlPath = req.getPathInfo();
		if (urlPath != null) {
			List<String> variablesFromUrlPath = pathTemplate.extractVariables(urlPath);
			synchronized (variablesFromUrlPath) {
				for (int i = 0; i < pathTemplate.getVariableNames().size(); i++) {
					urlParameter.put(pathTemplate.getVariableNames().get(i),
							variablesFromUrlPath.get(i));
				}
			}
		}
		return urlParameter;
	}

}
