package org.xydra.csv.impl.memory;

import org.xydra.csv.WrongDatatypeException;


class Cell {
	
	private String s;
	
	public void appendString(String s, int maximalFieldLength) {
		int sLen = this.s == null ? 0 : this.s.length();
		// if we have any space left
		if(sLen < maximalFieldLength) {
			// initialise
			if(this.s == null) {
				this.s = "";
			}
			// append
			this.s += s.substring(0, Math.min(s.length(), maximalFieldLength - sLen));
		}
	}
	
	public String getValue() {
		return this.s;
	}
	
	public double getValueAsDouble() {
		if(this.s == null) {
			return 0;
		} else {
			try {
				return Double.parseDouble(this.s);
			} catch(NumberFormatException e) {
				// retry with ',' as '.'
				String usVersion = this.s.replace(",", ".");
				try {
					return Double.parseDouble(usVersion);
				} catch(NumberFormatException e2) {
					throw new WrongDatatypeException("Content was '" + this.s
					        + "'. Could not parse as double. Even tried to parse as '" + usVersion
					        + "'", e);
				}
			}
		}
	}
	
	public long getValueAsLong() {
		if(this.s == null) {
			return 0;
		} else {
			try {
				return Long.parseLong(this.s);
			} catch(NumberFormatException e) {
				throw new WrongDatatypeException("Content was '" + this.s
				        + "'. Could not parse as long.", e);
			}
		}
	}
	
	public void incrementValue(int increment) throws WrongDatatypeException {
		long l = getValueAsLong();
		l = l + increment;
		this.s = "" + l;
	}
	
	/**
	 * @param value may be null, to store a null.
	 * @param initial if true, throws an {@link IllegalStateException} if there
	 *            was already a value
	 * @throws IllegalStateException if there was already a value
	 */
	public void setValue(String value, boolean initial) {
		if(initial && this.s != null) {
			throw new IllegalStateException("Value was not null but '" + this.s
			        + "' so could not set to '" + value + "'");
		}
		this.s = value;
	}
	
}
