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
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


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
	
	public static String getCanonicalName(Class<?> clazz) {
		return clazz.getCanonicalName();
	}
	
	/**
	 * @param className fully qualified name
	 * @return an instance or throw an Exception
	 * @throws Exception containing
	 */
	public static Object createInstanceOfClass(String className) throws Exception {
		try {
			Class<?> clazz = Class.forName(className);
			log.debug("Instantiated. Now casting...");
			try {
				Object instance = clazz.newInstance();
				return instance;
			} catch(InstantiationException e) {
				throw new Exception("Found the class with name " + className
				        + " but could not instantiate it", e);
			} catch(IllegalAccessException e) {
				throw new Exception("Found the class with name " + className
				        + " but could not instantiate it", e);
			}
		} catch(ClassNotFoundException e) {
			throw new Exception(e);
		}
	}
	
	/**
	 * @param obj to be estimated in size
	 * @return estimated size by serialising to ObjectStream and counting bytes
	 */
	public static long sizeOf(Serializable obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.close();
			return bos.toByteArray().length;
		} catch(IOException e) {
			log.warn("Could not estimate size of object with type "
			        + obj.getClass().getCanonicalName());
			return 0;
		}
	}
	
	/**
	 * @param t the {@link Throwable} to inspect
	 * @param n number of lines, if larger than available input: no problem
	 * @return the first n lines of the given {@link Throwable} t, separated by
	 *         new line characters + br tags
	 */
	public static String firstNLines(Throwable t, int n) {
		BufferedReader br = toBufferedReader(t);
		int lines = 0;
		try {
			StringBuffer buf = new StringBuffer();
			lines += append(br, buf, n);
			Throwable cause = t.getCause();
			while(lines < n && cause != null) {
				buf.append("Caused by\n");
				br = toBufferedReader(cause);
				lines += append(br, buf, n - lines);
				cause = t.getCause();
			}
			return buf.toString();
		} catch(IOException e) {
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
	public static BufferedReader toBufferedReader(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		StringReader sr = new StringReader(sw.getBuffer().toString());
		BufferedReader br = new BufferedReader(sr);
		return br;
	}
	
	/**
	 * @param br
	 * @param buf
	 * @param remainingMaxLines
	 * @return number of output lines generated
	 * @throws IOException
	 */
	private static int append(BufferedReader br, StringBuffer buf, int remainingMaxLines)
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
