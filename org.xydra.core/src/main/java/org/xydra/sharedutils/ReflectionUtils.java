package org.xydra.sharedutils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * A set of the most essential reflection capabilities of Java. The
 * corresponding GWT version in org.xydra.gwt project simply does nothing, but
 * provides the same method signatures.
 * 
 * @author xamde
 * 
 */
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
			log.info("Instantiated. Now casting...");
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
	 * @param n number of lines
	 * @return the first n lines of the given {@link Throwable} t, separated by
	 *         new line characters + br tags
	 */
	public static String firstNLines(Throwable t, int n) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		StringReader sr = new StringReader(sw.getBuffer().toString());
		BufferedReader br = new BufferedReader(sr);
		String line;
		try {
			// skip first 4 lines
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			StringBuffer buf = new StringBuffer();
			for(int i = 0; i < n; i++) {
				buf.append(line + " <br />\n");
				line = br.readLine();
			}
			return buf.toString();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
