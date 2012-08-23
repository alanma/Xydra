package org.xydra.restless.utils;

import org.xydra.annotations.NeverNull;

public class XmlUtils {
	
	/**
	 * @param raw unencoded string @NeverNull
	 * @return the input string with XML escaping
	 */
	public static final String xmlEncode(@NeverNull String raw) {
		String safe = raw;
		safe = safe.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quot;");
		return safe;
	}
	
}
