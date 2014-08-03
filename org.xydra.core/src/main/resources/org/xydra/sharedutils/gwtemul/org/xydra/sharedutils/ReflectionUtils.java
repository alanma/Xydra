package org.xydra.sharedutils;

import java.io.Serializable;


public class ReflectionUtils {
	
	public static String getCanonicalName(Class<?> clazz) {
		return clazz.getName();
	}
	
	/**
	 * @param className fully qualified name
	 * @return an instance or throw an Exception
	 * @throws Exception containing
	 */
	public static Object createInstanceOfClass(String className) throws Exception {
		throw new Exception("Dynamic class loading not possible in GWT");
	}
	
    public static <T> T createInstanceOfClass(Class<T> clazz) throws Exception {
        throw new Exception("Dynamic class loading not possible in GWT");
    }

	
	/**
	 * @param obj to be estimated in size
	 * @return estimated size by serialising to ObjectStream and counting bytes
	 */
	public static long sizeOf(Serializable obj) {
		return -1;
	}
	
	public static String firstNLines(Throwable t, int n) {
		return t.getMessage()+" (no stack trace available in GWT)";
	}

}
