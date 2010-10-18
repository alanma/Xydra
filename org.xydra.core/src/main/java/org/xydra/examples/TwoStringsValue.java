package org.xydra.examples;

import org.xydra.core.value.XValue;

/**
 * An example XValue used demonstrating how to write a simple XValue in the Xydra documentary. 
 * @author Kaidel
 */

public class TwoStringsValue implements XValue{

	/*
	 * The serialVersionUID for the Serializable interface
	 */
	private static final long serialVersionUID = 3111709866748393277L;
	
	
	/*
	 * The variables storing our two Strings 
	 */
	private String string1, string2;
	
	public TwoStringsValue(String string1, String string2) {
		/*
		 * Do NOT use "this.string1 = string1" here, since this would make it possible to
		 * change the content after the creation of a new TwoStringsValue.
		 * 
		 * Remember: Java uses pass-by-reference when you're working with Strings!S
		 */
		this.string1 = new String(string1); //copying the passed string
		this.string2 = new String(string2);
	}
	
	/*
	 * Since we want to be able to work with the content of this value, we also need to provide methods
	 * to access it.
	 */

	public String getString1() {
		return this.string1;
	}
	
	public String getString2() {
		return this.string2;
	}
}
