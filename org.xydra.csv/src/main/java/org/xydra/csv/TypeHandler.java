package org.xydra.csv;

/**
 * Handle Strings, booleans, integer numbers and floating point numbers.
 * 
 * TODO handle boolean, floating point, string
 * 
 * @author voelkel
 * 
 */
public class TypeHandler {
	
	public static class ERROR implements Type {
		
		@Override
		public String toValueString() {
			return "##";
		}
	}
	
	public static class LONG implements Type {
		
		private long value;
		
		public LONG(long l) {
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
	
	public static double asDouble(String value) {
		if(value == null) {
			return 0;
		} else {
			try {
				return Double.parseDouble(value);
			} catch(NumberFormatException e) {
				// retry with ',' as '.'
				String usVersion = value.replace(",", ".");
				try {
					return Double.parseDouble(usVersion);
				} catch(NumberFormatException e2) {
					throw new WrongDatatypeException("Content was '" + value
					        + "'. Could not parse as double. Even tried to parse as '" + usVersion
					        + "'", e);
				}
			}
		}
	}
	
	public static long asLong(String value) {
		if(value == null) {
			return 0;
		} else {
			try {
				return Long.parseLong(value);
			} catch(NumberFormatException e) {
				throw new WrongDatatypeException("Content was '" + value
				        + "'. Could not parse as long.", e);
			}
		}
	}
	
	/**
	 * @param s
	 * @return true if parsing was successful
	 */
	Type fromString(String s) {
		try {
			long l = Long.parseLong(s);
			return new LONG(l);
		} catch(NumberFormatException e) {
			return new ERROR();
		}
	}
	
}
