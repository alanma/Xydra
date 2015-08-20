package org.xydra.sharedutils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.xydra.annotations.RunsInGWT;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * A set of the most essential reflection capabilities of Java. The
 * corresponding GWT version in org.xydra.gwt project simply does nothing, but
 * provides the same method signatures.
 *
 * Runs in GWT via supersource
 *
 * @author xamde
 */
@RunsInGWT(false)
public class ReflectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

    public static String getCanonicalName(final Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    /**
     * @param className fully qualified name
     * @return an instance or throw an Exception
     * @throws Exception containing more info
     */
    public static Object createInstanceOfClass(final String className) throws Exception {
        try {
            final Class<?> clazz = Class.forName(className);
            return createInstanceOfClass(clazz);
        } catch(final ClassNotFoundException e) {
            throw new Exception(e);
        }
    }

    /**
     * @param clazz @NeverNull
     * @return an instance or throw an Exception
     * @throws Exception containing more info
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstanceOfClass(final Class<T> clazz) throws Exception {
        if(log.isDebugEnabled()) {
			log.debug("Instantiated. Now casting...");
		}
        try {
            final Object instance = clazz.newInstance();
            return (T)instance;
        } catch(final InstantiationException e) {
            throw new Exception("Found the class with name " + clazz.getCanonicalName()
                    + " but could not instantiate it", e);
        } catch(final IllegalAccessException e) {
            throw new Exception("Found the class with name " + clazz.getCanonicalName()
                    + " but could not instantiate it", e);
        }
    }

    /**
     * @param obj to be estimated in size
     * @return estimated size by serialising to ObjectStream and counting bytes
     */
    public static long sizeOf(final Serializable obj) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            return bos.toByteArray().length;
        } catch(final IOException e) {
            log.warn("Could not estimate size of object with type "
                    + obj.getClass().getCanonicalName());
            return 0;
        }
    }

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
        for(lines = 0; lines < remainingMaxLines && line != null; lines++) {
            buf.append(line + " <br />\n");
            line = br.readLine();
        }
        return lines;
    }

}
