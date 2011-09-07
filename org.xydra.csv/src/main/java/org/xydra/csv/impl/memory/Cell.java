package org.xydra.csv.impl.memory;

import org.xydra.csv.ICell;
import org.xydra.csv.TypeHandler;
import org.xydra.csv.WrongDatatypeException;


class Cell implements ICell {
	
	private String value;
	
	public Cell() {
	}
	
	public Cell(String string) {
		this.value = string;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#appendString(java.lang.String, int)
	 */
	@Override
    public void appendString(String s, int maximalFieldLength) {
		int sLen = this.value == null ? 0 : this.value.length();
		// if we have any space left
		if(sLen < maximalFieldLength) {
			// initialise
			if(this.value == null) {
				this.value = "";
			}
			// append
			this.value += s.substring(0, Math.min(s.length(), maximalFieldLength - sLen));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#getValue()
	 */
	@Override
    public String getValue() {
		return this.value;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#getValueAsDouble()
	 */
	@Override
    public double getValueAsDouble() throws WrongDatatypeException {
		return TypeHandler.asDouble(this.value);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#getValueAsLong()
	 */
	@Override
    public long getValueAsLong() throws WrongDatatypeException {
		return TypeHandler.asLong(this.value);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#incrementValue(int)
	 */
	@Override
    public void incrementValue(int increment) throws WrongDatatypeException {
		long l = getValueAsLong();
		l = l + increment;
		this.value = "" + l;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.csv.impl.memory.ICell#setValue(java.lang.String, boolean)
	 */
	@Override
    public void setValue(String value, boolean initial) {
		if(initial && this.value != null) {
			throw new IllegalStateException("Value was not null but '" + this.value
			        + "' so could not set to '" + value + "'");
		}
		this.value = value;
	}
	
}
