package org.xydra.restless;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Code copied and pasted from project org.xydra.core (org.xydra.sharedutils.ReflectionUtils) to avoid a dependency
 *
 * @author xamde
 */
public class XydraCoreCopy {

	private static final Logger log = LoggerFactory.getLogger(XydraCoreCopy.class);

	/**
	 * @param t the {@link Throwable} to inspect
	 * @param n number of lines, if larger than available input: no problem
	 * @return the first n lines of the given {@link Throwable} t, separated by new line characters + br tags
	 */
	public static String firstNLines(final Throwable t, final int n) {
		BufferedReader br = toBufferedReader(t);
		int lines = 0;
		try {
			final StringBuffer buf = new StringBuffer();
			lines += append(br, buf, n);
			Throwable cause = t.getCause();
			while (lines < n && cause != null) {
				buf.append("Caused by -------------------------------------\n");
				try {
					br = toBufferedReader(cause);
					lines += append(br, buf, n - lines);
				} catch (final Throwable t2) {
					log.warn("Exception while turnign exception to string");
					log.warn("Exception while turnign exception to string",t2);
				} finally {
					final Throwable subCause = cause.getCause();
					if (cause == subCause) {
						log.warn("Self-referential error object");
						break;
					}
					cause = subCause;
				}
			}
			return buf.toString();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Not even emulated in GWT!
	 *
	 * @param t
	 * @return ...
	 */
	@RunsInGWT(false)
	public static BufferedReader toBufferedReader(final Throwable t) {
		final StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		final StringReader sr = new StringReader(sw.getBuffer().toString());
		final BufferedReader br = new BufferedReader(sr);
		return br;
	}

	/**
	 * @param br
	 * @param buf
	 * @param remainingMaxLines
	 * @return number of output lines generated
	 * @throws IOException
	 */
	private static int append(final BufferedReader br, final StringBuffer buf, final int remainingMaxLines)
			throws IOException {
		String line = br.readLine();
		int lines;
		for (lines = 0; lines < remainingMaxLines && line != null; lines++) {
			buf.append(line + " <br />\n");
			line = br.readLine();
		}
		return lines;
	}
}
