package org.xydra.log.util;

import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.core.util.DebugUtils;


/**
 * Can render exceptions a nicely readable HTML.
 * 
 * Can raise some classic exceptions to test how your app handles them.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SharedExceptionUtils_GwtEmul {
	
	/**
	 * See also {@link DebugUtils#dumpStacktrace()}
	 * 
	 * @return
	 */
	public static StringBuffer getStacktraceAsString() {
		return new StringBuffer("getStacktraceAsString not available in GWT");
	}
	
	/**
	 * Assume a and b come form the same program and have a common caller
	 * hierarchy. Find it and remove it.
	 * 
	 * @param a a stacktrace with newlines in it
	 * @param b another stacktrace with newlines in it
	 * @param linesToSkip TODO
	 */
	public static void dumpWhereStacktracesAreDifferent(String aname, String a, String bname,
	        String b, int linesToSkip) {
		// do nothing
	}
}
