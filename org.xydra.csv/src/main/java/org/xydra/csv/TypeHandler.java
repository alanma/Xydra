package org.xydra.csv;

/**
 * Handle Strings, booleans, integer numbers and floating point numbers.
 * 
 * TODO handle boolean, floating point, string
 * 
 * TODO use this class
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
