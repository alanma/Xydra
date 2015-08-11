package org.xydra.index;

/**
 * Simple commonly used tools for working with Java enums.
 *
 * @author xamde
 */
public class EnumUtils {

	/**
	 * {@link Enum#valueOf(Class, String)} variant which handles null gracefully.
	 *
	 * @param enumType
	 * @param s
	 * @return @CanBeNull if s is null
	 */
	public static <E extends Enum<E>> E valueOf(final Class<E> enumType, final String s) {
		if (s == null) {
			return null;
		}
		final E enumValue = Enum.valueOf(enumType, s);
		return enumValue;
	}

}
