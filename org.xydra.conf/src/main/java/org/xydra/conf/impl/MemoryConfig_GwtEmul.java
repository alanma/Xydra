package org.xydra.conf.impl;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
// via super-source
public class MemoryConfig_GwtEmul {

	/**
	 * @param clazz
	 *            @NeverNull
	 * @return an instance
	 * @throws IllegalArgumentException
	 *             if instance could not be created
	 */
	public static <T> T newInstance(final Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not create instance of " + clazz.getName(), e);
		}
	}

	/**
	 * @param className
	 * @return @CanBeNull
	 * @throws IllegalArgumentException
	 *             if class could not be found
	 */
	public static Class<?> classForName(final String className) {
		try {
			return Class.forName(className);
		} catch (final ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not find class '" + className + "'", e);
		}
	}

}
