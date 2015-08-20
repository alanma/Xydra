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
	 * Also ignores casing of string s.
	 *
	 * @param enumType
	 * @param s
	 * @return @CanBeNull if s is null or if no matching enum constant was found
	 */
	public static <E extends Enum<E>> E valueOf(final Class<E> enumType, final String s) {
		if (s == null) {
			return null;
		}

		for (final E enumValue : enumType.getEnumConstants()) {
	        if (enumValue.name().equalsIgnoreCase(s)) {
	            return enumValue;
	        }
	    }

		return null;
	}

}
