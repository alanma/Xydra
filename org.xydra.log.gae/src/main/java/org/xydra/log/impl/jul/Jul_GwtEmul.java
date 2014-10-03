package org.xydra.log.impl.jul;

import java.util.logging.LogRecord;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
// via emulation
public class Jul_GwtEmul {

	public static String getSourceClassName(LogRecord log) {
		String clazz = log.getSourceClassName();
		return clazz;
	}

	public static String getSourceMethodName(LogRecord log) {
		String method = log.getSourceMethodName();
		return method;
	}

}
