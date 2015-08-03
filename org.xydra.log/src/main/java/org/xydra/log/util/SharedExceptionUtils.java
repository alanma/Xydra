package org.xydra.log.util;

public class SharedExceptionUtils {

	public static String toString(final Throwable throwable) {
		Throwable t = throwable;
		final StringBuffer buf = new StringBuffer();
		while (t != null) {
			final StackTraceElement[] stackTraceElements = t.getStackTrace();
			buf.append(t.toString() + "\n");
			for (int i = 0; i < stackTraceElements.length; i++) {
				buf.append("    at " + stackTraceElements[i] + "\n");
				// if(line == null) {
				// line = "" + stackTraceElements[i].getLineNumber();
				// }
			}
			t = t.getCause();
			if (t != null) {
				buf.append("Caused by: ");
			}
		}
		return buf.toString();
	}

}
