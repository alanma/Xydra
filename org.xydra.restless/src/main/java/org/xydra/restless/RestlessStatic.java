package org.xydra.restless;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.common.NanoClock;

class RestlessStatic {

	/* Don't use a logger here */

	/**
	 * @param clock
	 * @param staticMethodName
	 * @param instance
	 * @NeverNull
	 * @param parameterTypes
	 *            length n, excluding the type of instance
	 * @param parameters
	 *            length n, excluding the instance
	 */
	static void invokeStaticMethodOnInstance(NanoClock clock, Object instance,
			String staticMethodName, Class<?>[] parameterTypes, Object[] parameters) {
		clock.start();
		Class<?> clazz = instance.getClass();
		String className = clazz.getCanonicalName();
		try {
			Class<?>[] fullParameterTypes = new Class[parameterTypes.length + 1];
			System.arraycopy(parameterTypes, 0, fullParameterTypes, 1, parameterTypes.length);
			fullParameterTypes[0] = clazz;

			Method staticMethod = clazz.getMethod(staticMethodName, parameterTypes);
			clock.stop(className + "-get-method-" + staticMethodName);
			// invoke
			try {
				clock.start();
				staticMethod.invoke(instance, parameters);
				clock.stop(className + "-invoke-method-" + staticMethodName);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Class '" + className + "." + staticMethodName
						+ "(...)' failed with IllegalArgumentException", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Class '" + className + "." + staticMethodName
						+ "(...)' failed with IllegalAccessException", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Class '" + className + "." + staticMethodName
						+ "(...)' failed with InvocationTargetException", e);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Class '" + className + "' has no " + staticMethodName
					+ "( ... ) method.");
		}
	}

	/**
	 * @param clock
	 * @param className
	 * @param staticMethodName
	 * @param parameterTypes
	 *            length n
	 * @param parameters
	 *            length n
	 */
	static void invokeStaticMethod(NanoClock clock, String className, String staticMethodName,
			Class<?>[] parameterTypes, Object[] parameters) {
		clock.start();
		try {
			Class<?> clazz = Class.forName(className);
			try {
				try {
					try {
						// create instance
						Constructor<?> cons = clazz.getConstructor();
						try {
							Object instance = cons.newInstance();
							clock.stop(className + "-newinstance");
							// invoke
							invokeStaticMethodOnInstance(clock, instance, staticMethodName,
									parameterTypes, parameters);
						} catch (IllegalArgumentException e) {
							throw new RuntimeException("new '" + className + "() failed with "
									+ e.getClass() + ":" + e.getMessage(), e);
						} catch (InstantiationException e) {
							throw new RuntimeException("new '" + className + "() failed with "
									+ e.getClass() + ":" + e.getMessage(), e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("new '"
									+ className
									+ "() failed with "
									+ e.getClass()
									+ ":"
									+ e.getMessage()
									+ " caused by "
									+ (e.getCause() == null ? "--" : e.getCause().getClass() + ":"
											+ e.getCause().getMessage()), e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException("new '" + className + "() failed with "
									+ e.getClass() + ":" + e.getMessage(), e);
						}
					} catch (NoSuchMethodException e) {
						throw new RuntimeException("Class '" + className + "' has no "
								+ staticMethodName + "( ... ) method.");
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("new '" + className + "() failed with "
							+ e.getClass() + ":" + e.getMessage(), e);
				}
			} catch (SecurityException e) {
				throw new RuntimeException("Class '" + className + " failed to get constructor", e);
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class '" + className + "' not found");
		}
	}

	static void invokeStaticMethod(String className, String staticMethodName,
			Class<?>[] parameterTypes, Object[] parameters) {
		NanoClock clock = new NanoClock();
		invokeStaticMethod(clock, className, staticMethodName, parameterTypes, parameters);
	}

	/**
	 * @param req
	 *            HttpServletRequest, @NeverNull
	 * @return "/foo/" for a request uri of "/foo/bar" with a pathInfo of "bar"
	 */
	public static String getServletPath(@NeverNull HttpServletRequest req) {
		String uri = req.getRequestURI();
		String path = req.getPathInfo();
		String servletPath = uri.substring(0, uri.length() - path.length());
		Restless.log.debug("uri=" + uri + "\npath=" + path + "->" + servletPath);
		return servletPath;
	}

	/**
	 * 
	 * @param instanceOrClass
	 * @NeverNull
	 * @return
	 */
	protected static final String instanceOrClass_className(@NeverNull Object instanceOrClass) {
		if (instanceOrClass instanceof Class<?>) {
			return ((Class<?>) instanceOrClass).getCanonicalName();
		} else {
			return instanceOrClass.getClass().getName();
		}
	}

	/**
	 * @param clazz
	 *            Class from which to get the method reference @NeverNull
	 * @param methodName
	 *            Name of Java method to get @NeverNull
	 * @return a java.lang.reflect.{@link Method} from a Class with a given
	 *         methodName
	 */
	public static Method methodByName(@NeverNull Class<?> clazz, @NeverNull String methodName) {
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * @param instanceOrClass
	 *            an instance or class in which to search methodName @NeverNull
	 * @param methodName
	 *            e.g. 'getName' @NeverNull
	 * @return a java.lang.reflect.{@link Method} from a String
	 */
	public static Method methodByName(@NeverNull Object instanceOrClass,
			@NeverNull String methodName) {
		return methodByName(toClass(instanceOrClass), methodName);
	}

	/**
	 * @param commaSeparatedClassnames
	 * @CanBeNull
	 * @return a list of classnames in order of appearance
	 */
	static List<String> parseToList(@CanBeNull String commaSeparatedClassnames) {
		List<String> list = new ArrayList<String>();

		if (commaSeparatedClassnames == null) {
			return list;
		}

		// '\\s' = whitespace
		String[] parts = commaSeparatedClassnames.split("[\\s\\n]*,[\\s\\n]*");
		for (int i = 0; i < parts.length; i++) {
			String classname = parts[i].trim();
			assert !classname.contains(",");
			list.add(classname);
		}
		return list;
	}

	static boolean requestIsViaAdminUrl(@NeverNull HttpServletRequest req) {
		return req.getRequestURI().startsWith(Restless.ADMIN_ONLY_URL_PREFIX);
	}

	/**
	 * 
	 * @param instanceOrClass
	 * @NeverNull
	 * @return the given class, if it was a class. If it was an object, return
	 *         the class of it.
	 */
	public static Class<?> toClass(@NeverNull Object instanceOrClass) {
		if (instanceOrClass instanceof Class<?>) {
			return (Class<?>) instanceOrClass;
		} else {
			return instanceOrClass.getClass();
		}
	}

}
