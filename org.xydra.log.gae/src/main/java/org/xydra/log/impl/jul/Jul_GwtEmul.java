package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
// via emulation
public class Jul_GwtEmul {

	public static String getSourceClassName(final LogRecord log) {
		final String clazz = log.getSourceClassName();
		return clazz;
	}

	public static String getSourceMethodName(final LogRecord log) {
		final String method = log.getSourceMethodName();
		return method;
	}

}
