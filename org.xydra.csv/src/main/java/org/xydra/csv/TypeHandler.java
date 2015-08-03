package org.xydra.csv;

/**
 * Handle Strings, booleans, integer numbers and floating point numbers.
 *
 * TODO handle boolean, floating point, string
 *
 * @author xamde
 */
public class TypeHandler {

	public static class ERROR implements Type {

		@Override
		public String toValueString() {
			return "##";
		}
	}

	public static class LONG implements Type {

		private final long value;

		public LONG(final long l) {
			this.value = l;
		}

		@Override
		public String toValueString() {
			return "" + this.value;
		}

	}

	public interface Type {
		/**
		 * @return a String representation of the value
		 */
		public String toValueString();
	}

	public static double asDouble(final String value) {
		if (value == null) {
			return 0;
		} else {
			try {
				return Double.parseDouble(value);
			} catch (final NumberFormatException e) {
				// retry with ',' as '.'
				final String usVersion = value.replace(",", ".");
				try {
					return Double.parseDouble(usVersion);
				} catch (final NumberFormatException e2) {
					throw new WrongDatatypeException("Content was '" + value
							+ "'. Could not parse as double. Even tried to parse as '" + usVersion
							+ "'", e);
				}
			}
		}
	}

	public static long asLong(final String value) {
		if (value == null) {
			return 0;
		} else {
			try {
				return Long.parseLong(value);
			} catch (final NumberFormatException e) {
				throw new WrongDatatypeException("Content was '" + value
						+ "'. Could not parse as long.", e);
			}
		}
	}

	/**
	 * @param s
	 * @return true if parsing was successful
	 */
	Type fromString(final String s) {
		try {
			final long l = Long.parseLong(s);
			return new LONG(l);
		} catch (final NumberFormatException e) {
			return new ERROR();
		}
	}

}
