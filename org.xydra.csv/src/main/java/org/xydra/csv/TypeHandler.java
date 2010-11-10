package org.xydra.csv;

/**
 * Handle Strings, booleans, integer numbers and floating point numbers.
 * 
 * @author voelkel
 * 
 */
public class TypeHandler {

	public interface Type {
		/**
		 * @return a String representation of the value
		 */
		public String toValueString();
	}

	/**
	 * @param s
	 * @return true if parsing was successful
	 */
	Type fromString(String s) {
		try {
			long l = Long.parseLong(s);
			return new LONG(l);
		} catch (NumberFormatException e) {
			return new ERROR();
		}
	}

	public static class ERROR implements Type {

		public String toValueString() {
			return "##";
		}
	}

	public static class LONG implements Type {

		private long value;

		public LONG(long l) {
			this.value = l;
		}

		public String toValueString() {
			return "" + this.value;
		}

	}

}
